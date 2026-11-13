package de.raindancer.hyades.fabric;

import de.raindancer.hyades.ui.Canvas;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** {@link Canvas} on this version's GUI. */
final class FabricCanvas implements Canvas {

	private final GuiGraphics graphics;
	private final Font font;

	FabricCanvas(GuiGraphics graphics, Font font) {
		this.graphics = graphics;
		this.font = font;
	}

	@Override
	public int width() {
		return graphics.guiWidth();
	}

	@Override
	public int height() {
		return graphics.guiHeight();
	}

	@Override
	public void fill(int x, int y, int width, int height, int argb) {
		graphics.fill(x, y, x + width, y + height, argb);
	}

	@Override
	public int textWidth(String text) {
		return font.width(text);
	}

	@Override
	public int lineHeight() {
		return font.lineHeight;
	}

	@Override
	public void text(String text, int x, int y, int argb) {
		graphics.drawString(font, text, x, y, argb, false);
	}
}
