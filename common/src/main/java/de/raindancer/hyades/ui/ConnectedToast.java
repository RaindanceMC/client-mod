package de.raindancer.hyades.ui;

import de.raindancer.client.protocol.Theme;
import de.raindancer.hyades.link.ClientLink;

/**
 * "This server speaks to your mod", said once in the top left — the top right is where the game stacks its own
 * toasts, and the two would sit on each other.
 *
 * <p>Its time starts when it is first drawn, not when the welcome came: a server's resource pack loads right
 * after joining, and a plate that spent its four seconds behind the loading screen was never seen. Whoever
 * draws this leaves it out while the screen is covered.
 */
public final class ConnectedToast {

    public static final long IN = Motion.SHORT;
    public static final long HOLD = 4000;
    public static final long OUT = Motion.SHORT;

    public static final int MARGIN = 6;
    public static final int TRAVEL = 4;

    private static final int PAD_X = 6;
    private static final int PAD_Y = 4;
    private static final int GAP = 2;

    private ClientLink.Connection announcing;
    private long startedAt;

    public void draw(Canvas canvas, ClientLink.Connection connection, long now, String label) {
        // By identity: every welcome makes a new connection, and each one is announced once.
        if (connection != announcing) {
            announcing = connection;
            startedAt = now;
        }
        long age = now - startedAt;
        if (age >= IN + HOLD + OUT) {
            return;
        }
        float alpha = age < IN ? Motion.easeOut(age / (float) IN)
                : age < IN + HOLD ? 1 : 1 - Motion.easeOut((age - IN - HOLD) / (float) OUT);
        if (alpha <= 0) {
            return;
        }
        Theme theme = connection.theme();
        int room = canvas.width() - 2 * MARGIN - 2 * PAD_X - 2;
        String title = fitted(canvas, connection.serverName(), room);
        String line = fitted(canvas, label, room);

        int width = Math.max(canvas.textWidth(title), canvas.textWidth(line)) + 2 * PAD_X + 2;
        int height = 2 * canvas.lineHeight() + GAP + 2 * PAD_Y + 2;
        int x = MARGIN;
        int y = MARGIN - Math.round((1 - alpha) * TRAVEL);

        Plate.draw(canvas, x, y, width, height, theme, alpha);
        text(canvas, title, x + 1 + PAD_X, y + 1 + PAD_Y, Colours.fade(theme.sky(), alpha));
        text(canvas, line, x + 1 + PAD_X, y + 1 + PAD_Y + canvas.lineHeight() + GAP, Colours.fade(theme.muted(), alpha));
    }

    private static void text(Canvas canvas, String text, int x, int y, int argb) {
        if (argb >>> 24 >= Colours.FAINTEST_TEXT) {
            canvas.text(text, x, y, argb);
        }
    }

    private static String fitted(Canvas canvas, String text, int room) {
        if (canvas.textWidth(text) <= room) {
            return text;
        }
        String cut = text;
        while (!cut.isEmpty() && canvas.textWidth(cut + "…") > room) {
            cut = cut.substring(0, cut.length() - 1);
        }
        return cut + "…";
    }
}
