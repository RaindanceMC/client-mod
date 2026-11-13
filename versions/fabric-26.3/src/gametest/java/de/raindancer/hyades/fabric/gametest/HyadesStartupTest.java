package de.raindancer.hyades.fabric.gametest;

import de.raindancer.client.protocol.ClientFeature;
import de.raindancer.hyades.fabric.HyadesMod;
import de.raindancer.hyades.link.ClientLink;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

/**
 * Boots a real client and fails the build if the mod does not come up.
 *
 * <p>In a singleplayer world nothing announces the channel, so the mod has to stay silent. Started with
 * {@code -Phyades.e2e.server=host:port} the client then joins that server — a Paper with RainsCore — and has
 * to be welcomed; the plate it shows is photographed into {@code build/run/clientGameTest/screenshots}.
 */
public class HyadesStartupTest implements FabricClientGameTest {

	private static final int TWENTY_SECONDS = 400;

	@Override
	public void runTest(ClientGameTestContext context) {
		context.waitTick();
		context.runOnClient(client -> {
			if (HyadesMod.get() == null || HyadesMod.get().link() == null) {
				throw new AssertionError("The client initialiser did not run");
			}
		});

		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			context.waitFor(client -> client.player != null && client.gui.screen() == null, TWENTY_SECONDS);
			context.waitTicks(20);
			if (HyadesMod.get().link().connection().isPresent()) {
				throw new AssertionError("Connected in a world that never announced the channel");
			}
			context.takeScreenshot("hyades_singleplayer_silent");
		}

		String server = System.getProperty("hyades.e2e.server");
		if (server == null || server.isBlank()) {
			return;
		}
		// A server with RainsCore offers its resource pack; a player who said yes once is not asked again.
		ServerData entry = new ServerData("e2e", server, ServerData.Type.OTHER);
		entry.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
		context.runOnClient(client -> ConnectScreen.startConnecting(new TitleScreen(), client,
				ServerAddress.parseString(server), entry, false, null));
		context.waitFor(client -> HyadesMod.get().link().connection().isPresent(), TWENTY_SECONDS);

		ClientLink.Connection connection = HyadesMod.get().link().connection().orElseThrow();
		String expected = System.getProperty("hyades.e2e.name");
		if (expected != null && !expected.equals(connection.serverName())) {
			throw new AssertionError("Welcomed by '" + connection.serverName() + "', expected '" + expected + "'");
		}
		if (connection.agreed().getOrDefault(ClientFeature.SESSION, 0) != 1) {
			throw new AssertionError("The session feature was not agreed: " + connection.agreed());
		}
		// The pack is fetched and loaded right after joining; the plate waits for that to be over.
		context.waitTicks(60);
		context.waitFor(client -> client.player != null && client.gui.screen() == null
				&& client.gui.overlay() == null, 3 * TWENTY_SECONDS);
		context.waitTicks(10);
		context.takeScreenshot("hyades_connected");

		context.runOnClient(client -> client.disconnectFromWorld(net.minecraft.network.chat.Component.empty()));
		context.waitFor(client -> HyadesMod.get().link().connection().isEmpty(), TWENTY_SECONDS);
		context.setScreen(TitleScreen::new);
	}
}
