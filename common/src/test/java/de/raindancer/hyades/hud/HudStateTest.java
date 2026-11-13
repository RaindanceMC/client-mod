package de.raindancer.hyades.hud;

import de.raindancer.client.protocol.HudPanel;
import de.raindancer.client.protocol.HudPanelClear;
import de.raindancer.client.protocol.HudStatus;
import de.raindancer.client.protocol.Text;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HudStateTest {

    private final HudState hud = new HudState();

    @Test
    void nothingIsShownUntilTheServerSendsSomething() {
        assertThat(hud.panel()).isEmpty();
    }

    @Test
    void aPanelStaysAndKeepsTheMomentItFirstAppearedWhileItsLinesChange() {
        hud.accept(new HudPanel(Text.of("Hyades"), List.of(Text.of("Drachma 12"))), 1000);
        hud.accept(new HudPanel(Text.of("Hyades"), List.of(Text.of("Drachma 13"))), 5000);

        HudState.Panel panel = hud.panel().orElseThrow();
        assertThat(panel.lines()).containsExactly(Text.of("Drachma 13"));
        assertThat(panel.since()).as("it does not arrive again every time a number changes").isEqualTo(1000);
    }

    @Test
    void aClearedPanelIsGoneAndTheNextOneArrivesAfresh() {
        hud.accept(new HudPanel(Text.of("Hyades"), List.of()), 1000);
        hud.accept(new HudPanelClear(), 2000);
        assertThat(hud.panel()).isEmpty();

        hud.accept(new HudPanel(Text.of("Hyades"), List.of()), 9000);
        assertThat(hud.panel().orElseThrow().since()).isEqualTo(9000);
    }

    @Test
    void aStatusLineFromTheServerIsNotThisModsToDraw() {
        hud.accept(new HudStatus(Text.of("In combat"), 3000), 1000);
        assertThat(hud.panel()).isEmpty();
    }

    @Test
    void leavingTheServerEmptiesIt() {
        hud.accept(new HudPanel(Text.of("Hyades"), List.of()), 1000);
        hud.reset();
        assertThat(hud.panel()).isEmpty();
    }
}
