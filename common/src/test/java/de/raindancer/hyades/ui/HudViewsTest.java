package de.raindancer.hyades.ui;

import de.raindancer.client.protocol.Span;
import de.raindancer.client.protocol.Text;
import de.raindancer.client.protocol.Theme;
import de.raindancer.hyades.hud.HudState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HudViewsTest {

    private static final Theme THEME = Theme.HARBOR_SUNSET;

    private static int left(RecordingCanvas canvas) {
        return canvas.fills.stream().mapToInt(RecordingCanvas.Fill::x).min().orElseThrow();
    }

    private static int right(RecordingCanvas canvas) {
        return canvas.fills.stream().mapToInt(fill -> fill.x() + fill.width()).max().orElseThrow();
    }

    private static int top(RecordingCanvas canvas) {
        return canvas.fills.stream().mapToInt(RecordingCanvas.Fill::y).min().orElseThrow();
    }

    private static int bottom(RecordingCanvas canvas) {
        return canvas.fills.stream().mapToInt(fill -> fill.y() + fill.height()).max().orElseThrow();
    }

    private static final HudState.Panel PANEL = new HudState.Panel(Text.of("Hyades"), List.of(
            new Text(List.of(new Span("Drachma ", 0, 0, ""), new Span("12,480", 0xFFF6B83D, 0, ""))),
            Text.EMPTY, Text.of("Job Mason")), 10_000);

    private static RecordingCanvas panelAt(long age) {
        RecordingCanvas canvas = new RecordingCanvas(480, 270);
        PanelView.draw(canvas, THEME, PANEL, 10_000 + age);
        return canvas;
    }

    @Test
    void thePanelSitsAtTheRightEdgeHalfwayDownWhereTheSidebarWas() {
        RecordingCanvas canvas = panelAt(1000);
        assertThat(right(canvas)).isEqualTo(480 - PanelView.MARGIN);
        int middle = (top(canvas) + bottom(canvas)) / 2;
        assertThat(middle).isBetween(270 / 2 - 4, 270 / 2 + 4);
    }

    @Test
    void theTitleIsSkyTheLinesAreTextAndAFaintLineSeparatesThem() {
        RecordingCanvas canvas = panelAt(1000);
        assertThat(canvas.texts).extracting(RecordingCanvas.Drawn::text).containsExactly("Hyades", "Drachma 12,480", "Job Mason");
        assertThat(canvas.texts.get(0).argb()).isEqualTo(THEME.sky());
        assertThat(canvas.texts.get(1).argb()).isEqualTo(THEME.text());

        RecordingCanvas.Fill hairline = canvas.fills.stream().filter(fill -> fill.argb() == Colours.fade(THEME.harbor(), PanelView.RULE_OPACITY)).findFirst().orElseThrow();
        assertThat(hairline.height()).isEqualTo(1);
        assertThat(hairline.y()).isGreaterThan(canvas.texts.get(0).y() + canvas.lineHeight() - 1)
                .isLessThan(canvas.texts.get(1).y());
    }

    @Test
    void everyLineIsInsideThePlateAndAnEmptyLineIsOnlyAGap() {
        RecordingCanvas canvas = panelAt(1000);
        for (RecordingCanvas.Drawn text : canvas.texts) {
            assertThat(text.x()).isGreaterThan(left(canvas));
            assertThat(text.x() + text.width()).isLessThan(right(canvas));
            assertThat(text.y()).isGreaterThan(top(canvas));
            assertThat(text.y() + canvas.lineHeight()).isLessThan(bottom(canvas));
        }
        int gapWithEmptyLine = canvas.texts.get(2).y() - canvas.texts.get(1).y();
        assertThat(gapWithEmptyLine).isGreaterThan(canvas.lineHeight() + 2).isLessThan(2 * (canvas.lineHeight() + 2));
    }

    @Test
    void thePanelIsNoWiderThanItsTextNeeds() {
        RecordingCanvas canvas = panelAt(1000);
        int widest = canvas.texts.stream().mapToInt(RecordingCanvas.Drawn::width).max().orElseThrow();
        assertThat(right(canvas) - left(canvas)).isLessThanOrEqualTo(widest + 12);
    }

    @Test
    void thePanelArrivesFromTheRightWhileFadingIn() {
        assertThat(panelAt(0).fills).isEmpty();
        RecordingCanvas arriving = panelAt(40);
        assertThat(right(arriving)).isGreaterThan(480 - PanelView.MARGIN);
        assertThat(arriving.texts.get(0).argb() >>> 24).isBetween(1, 254);
        assertThat(panelAt(Motion.SHORT).texts.get(0).argb() >>> 24).isEqualTo(255);
    }
}
