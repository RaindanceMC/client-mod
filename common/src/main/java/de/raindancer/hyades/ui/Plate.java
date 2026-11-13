package de.raindancer.hyades.ui;

import de.raindancer.client.protocol.Theme;

/**
 * The one shape everything sits on: navy, a one-pixel night outline with the corners taken off, and a solid
 * edge underneath — hyadesmc.net's button, not a blurred shadow.
 */
public final class Plate {

    public static final float FILL_OPACITY = 0.9f;
    public static final int EXTRUSION = 2;

    private Plate() {
    }

    /** @param height without the edge underneath, which is drawn below it */
    public static void draw(Canvas canvas, int x, int y, int width, int height, Theme theme, float alpha) {
        int night = Colours.fade(theme.night(), alpha);
        canvas.fill(x + 1, y, width - 2, 1, night);
        canvas.fill(x, y + 1, 1, height - 2 + EXTRUSION, night);
        canvas.fill(x + width - 1, y + 1, 1, height - 2 + EXTRUSION, night);
        canvas.fill(x + 1, y + height - 1, width - 2, 1 + EXTRUSION, night);
        // The night goes around the fill, not under it: the plate lets a little of the world through.
        canvas.fill(x + 1, y + 1, width - 2, height - 2, Colours.fade(theme.navy(), FILL_OPACITY * alpha));
    }
}
