package de.raindancer.hyades.fabric;

import de.raindancer.hyades.link.ClientLink;
import de.raindancer.hyades.ui.ConnectedToast;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;

/**
 * The mod on this game version: the channel, and where things are drawn. What is said on the channel and what
 * is drawn are in the shared build, the same for every version.
 */
public final class HyadesMod implements ClientModInitializer {

	public static final String ID = "hyades";

	private static HyadesMod instance;

	private ClientLink link;

	public static HyadesMod get() {
		return instance;
	}

	public ClientLink link() {
		return link;
	}

	@Override
	public void onInitializeClient() {
		link = new ClientLink(
				// The channel events arrive on the network thread; sending belongs to the client's own.
				frame -> Minecraft.getInstance().execute(() -> {
					if (ClientPlayNetworking.canSend(RainsPayload.TYPE)) {
						ClientPlayNetworking.send(new RainsPayload(frame));
					}
				}),
				System::currentTimeMillis, version(ID), version("minecraft"));

		PayloadTypeRegistry.playS2C().register(RainsPayload.TYPE, RainsPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RainsPayload.TYPE, RainsPayload.CODEC);
		ClientPlayNetworking.registerGlobalReceiver(RainsPayload.TYPE, (payload, context) -> link.received(payload.frame()));

		// A server names the channels it reads when a player arrives. One that names ours gets the hello;
		// any other server never hears from this mod.
		C2SPlayChannelEvents.REGISTER.register((handler, sender, client, channels) -> {
			if (channels.contains(RainsPayload.TYPE.id())) {
				client.execute(() -> greet(client));
			}
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (ClientPlayNetworking.canSend(RainsPayload.TYPE)) {
				greet(client);
			}
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> link.disconnected());

		ConnectedToast connected = new ConnectedToast();
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(ID, "connected"), (graphics, delta) -> {
			Minecraft client = Minecraft.getInstance();
			// Not behind a screen or the pack loading over it: the plate's time starts when it can be seen.
			if (client.screen != null || client.getOverlay() != null) {
				return;
			}
			link.connection().ifPresent(connection -> connected.draw(new FabricCanvas(graphics, client.font),
					connection, System.currentTimeMillis(), I18n.get("hyades.connected")));
		});

		instance = this;
	}

	private void greet(Minecraft client) {
		link.serverListens(client.getWindow().getGuiScale(), client.getLanguageManager().getSelected());
	}

	private static String version(String mod) {
		return FabricLoader.getInstance().getModContainer(mod)
				.map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("unknown");
	}
}
