package de.raindancer.hyades.ui;

import de.raindancer.client.protocol.Theme;

/**
 * What HUD text sits on: one see-through navy shape with the corners taken off, and nothing else. No outline
 * and no edge — over the game a plate has to hold text readable and otherwise stay out of the way.
 */
public final class Plate {

    /** Little more than the game gives its own sidebar, so the world stays visible through it. */
    public static final float FILL_OPACITY = 0.45f;

    private Plate() {
    }

    public static void draw(Canvas canvas, int x, int y, int width, int height, Theme theme, float alpha) {
        int navy = Colours.fade(theme.navy(), FILL_OPACITY * alpha);
        // Three bands that do not overlap: where see-through fills overlap, they show as a darker patch.
        canvas.fill(x + 1, y, width - 2, 1, navy);
        canvas.fill(x, y + 1, width, height - 2, navy);
        canvas.fill(x + 1, y + height - 1, width - 2, 1, navy);
    }
}
