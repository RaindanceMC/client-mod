package de.raindancer.hyades.fabric;

import de.raindancer.client.protocol.Span;
import de.raindancer.client.protocol.Text;
import de.raindancer.hyades.ui.Canvas;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/** {@link Canvas} on this version's GUI. */
final class FabricCanvas implements Canvas {

	/** hyadesmc.net's typeface; letters it lacks fall through to the game's own font. */
	private static final FontDescription OUTFIT = new FontDescription.Resource(Identifier.fromNamespaceAndPath(HyadesMod.ID, "outfit"));

	/** The same few lines are drawn every frame; building their components once is enough. */
	private static final Map<Text, Component> BUILT = new LinkedHashMap<>(64, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Text, Component> eldest) {
			return size() > 256;
		}
	};

	private final GuiGraphicsExtractor graphics;
	private final Font font;

	FabricCanvas(GuiGraphicsExtractor graphics, Font font) {
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
	public int textWidth(Text text) {
		return font.width(component(text));
	}

	@Override
	public int lineHeight() {
		return font.lineHeight;
	}

	@Override
	public void text(Text text, int x, int y, int argb) {
		graphics.text(font, component(text).getVisualOrderText(), x, y, argb, false);
	}

	private static Component component(Text text) {
		return BUILT.computeIfAbsent(text, FabricCanvas::build);
	}

	private static Component build(Text text) {
		MutableComponent line = Component.empty();
		for (Span span : text.spans()) {
			// A font the server names is one of its resource pack's — its icons live there. A key that is
			// not one is drawn in ours rather than trusted.
			Identifier named = span.font().isEmpty() ? null : Identifier.tryParse(span.font());
			Style style = Style.EMPTY.withFont(named == null ? OUTFIT : new FontDescription.Resource(named));
			if (span.argb() != 0) {
				style = style.withColor(span.argb() & 0xFFFFFF);
			}
			style = style.withBold(span.has(Span.BOLD)).withItalic(span.has(Span.ITALIC))
					.withUnderlined(span.has(Span.UNDERLINED)).withStrikethrough(span.has(Span.STRIKETHROUGH))
					.withObfuscated(span.has(Span.OBFUSCATED));
			line.append(Component.literal(span.text()).setStyle(style));
		}
		return line;
	}
}
