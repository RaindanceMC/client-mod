package de.raindancer.hyades.ui;

import de.raindancer.client.protocol.Text;

import java.util.ArrayList;
import java.util.List;

/** A screen that only writes down what was drawn on it. Every letter is six wide, a line nine high. */
final class RecordingCanvas implements Canvas {

    record Fill(int x, int y, int width, int height, int argb) {
    }

    /** @param argb the colour text without one of its own was to be drawn in */
    record Drawn(String text, int x, int y, int argb, int width) {
    }

    final List<Fill> fills = new ArrayList<>();
    final List<Drawn> texts = new ArrayList<>();
    private final int width;
    private final int height;

    RecordingCanvas(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public void fill(int x, int y, int width, int height, int argb) {
        fills.add(new Fill(x, y, width, height, argb));
    }

    @Override
    public int textWidth(Text text) {
        return text.plain().length() * 6;
    }

    @Override
    public int lineHeight() {
        return 9;
    }

    @Override
    public void text(Text text, int x, int y, int argb) {
        texts.add(new Drawn(text.plain(), x, y, argb, textWidth(text)));
    }
}
