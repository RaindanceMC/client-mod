package de.raindancer.hyades.ui;

import java.util.ArrayList;
import java.util.List;

/** A screen that only writes down what was drawn on it. Every letter is six wide, a line nine high. */
final class RecordingCanvas implements Canvas {

    record Fill(int x, int y, int width, int height, int argb) {
    }

    record Text(String text, int x, int y, int argb) {
    }

    final List<Fill> fills = new ArrayList<>();
    final List<Text> texts = new ArrayList<>();
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
    public int textWidth(String text) {
        return text.length() * 6;
    }

    @Override
    public int lineHeight() {
        return 9;
    }

    @Override
    public void text(String text, int x, int y, int argb) {
        texts.add(new Text(text, x, y, argb));
    }
}
