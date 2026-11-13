package de.raindancer.hyades.hud;

import de.raindancer.client.protocol.ClientboundPacket;
import de.raindancer.client.protocol.HudPanel;
import de.raindancer.client.protocol.HudPanelClear;
import de.raindancer.client.protocol.Text;

import java.util.List;
import java.util.Optional;

/**
 * What the server last said the HUD shows. The server sends state, never animation: when something first
 * appeared is kept here, so a number that changes every second does not make its panel arrive every second.
 */
public final class HudState {

    /** @param since when a panel first appeared, however often its lines changed afterwards */
    public record Panel(Text title, List<Text> lines, long since) {
    }

    private Panel panel;

    public synchronized void accept(ClientboundPacket packet, long now) {
        switch (packet) {
            case HudPanel shown -> panel = new Panel(shown.title(), shown.lines(), panel == null ? now : panel.since());
            case HudPanelClear cleared -> panel = null;
            // Anything else of the HUD is not drawn by this mod: the action bar stays the game's own.
            default -> {
            }
        }
    }

    public synchronized void reset() {
        panel = null;
    }

    public synchronized Optional<Panel> panel() {
        return Optional.ofNullable(panel);
    }
}
