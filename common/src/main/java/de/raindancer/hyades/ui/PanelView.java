package de.raindancer.hyades.ui;

import de.raindancer.client.protocol.Text;
import de.raindancer.client.protocol.Theme;
import de.raindancer.hyades.hud.HudState;

/**
 * The panel: what a sidebar would have shown, on a plate at the right edge where the sidebar was and no wider
 * than its text. The title in sky over a faint line, the lines under it; an empty line is a gap, not a row.
 */
public final class PanelView {

    public static final int MARGIN = 6;
    private static final int TRAVEL = 4;
    private static final int PAD_X = 5;
    private static final int PAD_Y = 4;
    private static final int LEADING = 2;
    private static final int RULE_GAP = 3;
    private static final int EMPTY_LINE = 5;
    /** The line under the title: there to be found, not to be looked at. */
    public static final float RULE_OPACITY = 0.5f;

    private PanelView() {
    }

    public static void draw(Canvas canvas, Theme theme, HudState.Panel panel, long now) {
        float alpha = Motion.easeOut((now - panel.since()) / (float) Motion.SHORT);
        if (alpha <= 0) {
            return;
        }
        int line = canvas.lineHeight();
        int inner = canvas.textWidth(panel.title());
        int below = 0;
        for (Text each : panel.lines()) {
            inner = Math.max(inner, canvas.textWidth(each));
            below += each.isEmpty() ? EMPTY_LINE : line + LEADING;
        }
        int width = inner + 2 * PAD_X;
        int height = 2 * PAD_Y + line + (panel.lines().isEmpty() ? 0 : 2 * RULE_GAP + 1 + below - LEADING);
        int x = canvas.width() - MARGIN - width + Math.round((1 - alpha) * TRAVEL);
        int y = (canvas.height() - height) / 2;

        Plate.draw(canvas, x, y, width, height, theme, alpha);
        int left = x + PAD_X;
        int at = y + PAD_Y;
        text(canvas, panel.title(), left, at, Colours.fade(theme.sky(), alpha));
        if (panel.lines().isEmpty()) {
            return;
        }
        at += line + RULE_GAP;
        canvas.fill(left, at, width - 2 * PAD_X, 1, Colours.fade(theme.harbor(), RULE_OPACITY * alpha));
        at += 1 + RULE_GAP;
        for (Text each : panel.lines()) {
            if (each.isEmpty()) {
                at += EMPTY_LINE;
            } else {
                text(canvas, each, left, at, Colours.fade(theme.text(), alpha));
                at += line + LEADING;
            }
        }
    }

    private static void text(Canvas canvas, Text text, int x, int y, int argb) {
        if (argb >>> 24 >= Colours.FAINTEST_TEXT) {
            canvas.text(text, x, y, argb);
        }
    }
}
