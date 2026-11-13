package de.raindancer.hyades.ui;

import de.raindancer.client.protocol.ClientFeature;
import de.raindancer.client.protocol.Theme;
import de.raindancer.hyades.link.ClientLink;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectedToastTest {

    private static final Theme THEME = Theme.HARBOR_SUNSET;
    private static final long SINCE = 10_000;

    private static ClientLink.Connection connection(String server) {
        return new ClientLink.Connection(server, THEME, Map.of(ClientFeature.SESSION, 1), SINCE);
    }

    private static final long FIRST_SEEN = SINCE + 60_000;

    /** What is on the screen so long after the plate was first drawn. */
    private static RecordingCanvas drawn(long age, String server, int width) {
        ConnectedToast toast = new ConnectedToast();
        ClientLink.Connection connection = connection(server);
        toast.draw(new RecordingCanvas(width, 270), connection, FIRST_SEEN, "Client mod connected");
        RecordingCanvas canvas = new RecordingCanvas(width, 270);
        toast.draw(canvas, connection, FIRST_SEEN + age, "Client mod connected");
        return canvas;
    }

    @Test
    void itsTimeStartsWhenItIsFirstSeenNotWhenTheWelcomeCame() {
        assertThat(drawn(2000, "Hyades", 480).texts).isNotEmpty();
    }

    @Test
    void theNextServerIsAnnouncedAgain() {
        ConnectedToast toast = new ConnectedToast();
        ClientLink.Connection first = connection("Hyades");
        toast.draw(new RecordingCanvas(480, 270), first, 0, "x");
        RecordingCanvas longAfter = new RecordingCanvas(480, 270);
        toast.draw(longAfter, first, 60_000, "x");
        assertThat(longAfter.fills).as("the first announcement is over").isEmpty();

        ClientLink.Connection second = connection("Hyades");
        toast.draw(new RecordingCanvas(480, 270), second, 60_000, "x");
        RecordingCanvas again = new RecordingCanvas(480, 270);
        toast.draw(again, second, 62_000, "x");
        assertThat(again.texts).as("a new welcome, even from the same server").isNotEmpty();
    }

    private static int alpha(int argb) {
        return argb >>> 24;
    }

    @Test
    void atRestItIsAPlateInTheTopLeftWithTheServerInSkyAndTheLabelMuted() {
        RecordingCanvas canvas = drawn(2000, "Hyades", 480);

        assertThat(canvas.texts).extracting(RecordingCanvas.Text::text).containsExactly("Hyades", "Client mod connected");
        assertThat(canvas.texts.get(0).argb()).isEqualTo(THEME.sky());
        assertThat(canvas.texts.get(1).argb()).isEqualTo(THEME.muted());

        int left = canvas.fills.stream().mapToInt(RecordingCanvas.Fill::x).min().orElseThrow();
        int right = canvas.fills.stream().mapToInt(fill -> fill.x() + fill.width()).max().orElseThrow();
        int top = canvas.fills.stream().mapToInt(RecordingCanvas.Fill::y).min().orElseThrow();
        int bottom = canvas.fills.stream().mapToInt(fill -> fill.y() + fill.height()).max().orElseThrow();
        assertThat(left).isEqualTo(ConnectedToast.MARGIN);
        assertThat(right).as("clear of the game's own toasts, 160 wide in the top right").isLessThan(480 - 160);
        assertThat(top).isEqualTo(ConnectedToast.MARGIN);
        for (RecordingCanvas.Text text : canvas.texts) {
            assertThat(text.x()).isGreaterThan(left);
            assertThat(text.x() + canvas.textWidth(text.text())).isLessThan(right);
            assertThat(text.y()).isGreaterThan(top);
            assertThat(text.y() + canvas.lineHeight()).isLessThan(bottom);
        }
    }

    @Test
    void thePlateIsNavyOnNightWithASolidEdgeUnderIt() {
        RecordingCanvas canvas = drawn(2000, "Hyades", 480);
        assertThat(canvas.fills).extracting(RecordingCanvas.Fill::argb)
                .containsOnly(THEME.night(), Colours.fade(THEME.navy(), Plate.FILL_OPACITY));

        RecordingCanvas.Fill navy = canvas.fills.stream().filter(fill -> fill.argb() != THEME.night()).findFirst().orElseThrow();
        int bottom = canvas.fills.stream().mapToInt(fill -> fill.y() + fill.height()).max().orElseThrow();
        int top = canvas.fills.stream().mapToInt(RecordingCanvas.Fill::y).min().orElseThrow();
        assertThat(navy.y() - top).as("outline above").isEqualTo(1);
        assertThat(bottom - (navy.y() + navy.height())).as("outline and extrusion below").isEqualTo(1 + Plate.EXTRUSION);
    }

    @Test
    void itArrivesFromAboveWhileFadingInAndLeavesTheSameWay() {
        assertThat(drawn(0, "Hyades", 480).fills).isEmpty();

        RecordingCanvas arriving = drawn(40, "Hyades", 480);
        RecordingCanvas resting = drawn(ConnectedToast.IN, "Hyades", 480);
        int restingTop = resting.fills.stream().mapToInt(RecordingCanvas.Fill::y).min().orElseThrow();
        int arrivingTop = arriving.fills.stream().mapToInt(RecordingCanvas.Fill::y).min().orElseThrow();
        assertThat(arrivingTop).isLessThan(restingTop).isGreaterThanOrEqualTo(restingTop - ConnectedToast.TRAVEL);
        assertThat(alpha(arriving.texts.get(0).argb())).isBetween(1, 254);
        assertThat(alpha(resting.texts.get(0).argb())).isEqualTo(255);

        RecordingCanvas leaving = drawn(ConnectedToast.IN + ConnectedToast.HOLD + 70, "Hyades", 480);
        assertThat(alpha(leaving.texts.get(0).argb())).isBetween(1, 254);

        assertThat(drawn(ConnectedToast.IN + ConnectedToast.HOLD + ConnectedToast.OUT, "Hyades", 480).fills).isEmpty();
    }

    @Test
    void textIsNeverDrawnSoFaintThatTheGameMakesItSolid() {
        for (long age = 0; age <= ConnectedToast.IN + ConnectedToast.HOLD + ConnectedToast.OUT; age++) {
            for (RecordingCanvas.Text text : drawn(age, "Hyades", 480).texts) {
                assertThat(alpha(text.argb())).as("at %d ms", age).isGreaterThanOrEqualTo(Colours.FAINTEST_TEXT);
            }
        }
    }

    @Test
    void aServerNameTooLongForTheScreenIsCutNotSpilled() {
        RecordingCanvas canvas = drawn(2000, "A".repeat(64), 200);
        int right = canvas.fills.stream().mapToInt(fill -> fill.x() + fill.width()).max().orElseThrow();
        assertThat(right).isLessThanOrEqualTo(200 - ConnectedToast.MARGIN);
        assertThat(canvas.texts.get(0).text()).endsWith("…").hasSizeLessThan(64);
        assertThat(canvas.texts.get(0).x() + canvas.textWidth(canvas.texts.get(0).text())).isLessThan(200 - ConnectedToast.MARGIN);
    }
}
