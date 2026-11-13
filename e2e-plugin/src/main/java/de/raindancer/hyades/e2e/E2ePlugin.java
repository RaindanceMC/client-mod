package de.raindancer.hyades.e2e;

import de.raindancer.core.RainsCore;
import de.raindancer.core.ui.scoreboard.ScoreboardPriority;
import de.raindancer.core.ui.scoreboard.Sidebar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * What a module does, and nothing about the mod: a sidebar through RainsCore, on join. A player without the
 * mod sees exactly that; one with it gets it in the mod's HUD when the mod says hello.
 */
public final class E2ePlugin extends JavaPlugin implements Listener {

	private static final MiniMessage MM = MiniMessage.miniMessage();

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		UUID player = event.getPlayer().getUniqueId();
		RainsCore core = RainsCore.get();
		core.scoreboards().show(player, "e2e", Sidebar.of(
				MM.deserialize("<gradient:#6EC1EA:#F6B83D>Hyades</gradient>"),
				line("Drachma", "<#F6B83D>₯ 12,480"),
				line("Fishing", "<#F6B83D>Lv 14"),
				line("Job", "<#F6B83D>Mason"),
				Component.empty(),
				line("Claim", "<#6EC1EA>Yours"),
				line("Online", "<white>37")), ScoreboardPriority.NORMAL);
	}

	private static Component line(String label, String value) {
		return MM.deserialize("<#A9B9CF>" + label + "</#A9B9CF>  " + value);
	}
}
