package de.raindancer.hyades.ui;

import de.raindancer.client.protocol.Text;

/**
 * The screen, in GUI pixels, as much of it as the mod's own drawing needs. Each game version implements
 * this once; everything that is drawn is written against it, which is why it looks the same in all of them.
 */
public interface Canvas {

    int width();

    int height();

    void fill(int x, int y, int width, int height, int argb);

    int textWidth(Text text);

    int lineHeight();

    /**
     * @param argb the colour of every run that has none of its own; its alpha applies to all of them
     */
    void text(Text text, int x, int y, int argb);

    default int textWidth(String text) {
        return textWidth(Text.of(text));
    }

    default void text(String text, int x, int y, int argb) {
        text(Text.of(text), x, y, argb);
    }
}
