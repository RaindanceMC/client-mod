package de.raindancer.hyades.ui;

public final class Motion {

    /** How long anything takes to arrive or leave. */
    public static final long SHORT = 140;

    private Motion() {
    }

    /** Fast first, settling at the end; anything outside 0..1 is held at the ends. */
    public static float easeOut(float progress) {
        float left = 1 - Math.clamp(progress, 0f, 1f);
        return 1 - left * left * left;
    }
}
