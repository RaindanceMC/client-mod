package de.raindancer.hyades.ui;

public final class Colours {

    /**
     * The game draws text whose alpha is nearly zero as fully solid instead, so anything fainter than this is
     * left out rather than flashed.
     */
    public static final int FAINTEST_TEXT = 8;

    private Colours() {
    }

    /** The same colour, its alpha scaled. */
    public static int fade(int argb, float alpha) {
        int scaled = Math.round((argb >>> 24) * Math.clamp(alpha, 0f, 1f));
        return scaled << 24 | argb & 0x00FFFFFF;
    }
}
