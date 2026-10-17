/*
 * Copyright (c) 2026, ImTheC4
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.imthec4.nightmarecurse;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * One table row drawn the same way in the box and in the sidebar: the button you click, a yellow
 * arrow, and the protection it turns on while cursed. The prayer sprites are pixel art, so they
 * are only ever drawn at a whole-number scale (1x, 2x, ...) to keep every pixel sharp; the disc,
 * arrow and gaps are sized from the sprite.
 */
final class CurseArt
{
	/** Sprite size assumed until the game has loaded the prayer sprites. */
	static final int DEFAULT_SPRITE = 24;

	private static final Color DISC = new Color(0x5A4A32);
	private static final Color DISC_EDGE = new Color(0x2B2318);
	private static final Color ARROW = new Color(0xFFE94D);
	private static final Color ARROW_LIGHT = new Color(0xFFF7B0);

	private CurseArt()
	{
	}

	/** The largest side of the loaded prayer sprites, at 1x. */
	static int spriteSize(Map<Protection, BufferedImage> icons)
	{
		int size = 0;
		for (BufferedImage image : icons.values())
		{
			size = Math.max(size, Math.max(image.getWidth(), image.getHeight()));
		}
		return size > 0 ? size : DEFAULT_SPRITE;
	}

	/** Diameter of the disc behind a sprite drawn at {@code scale}. */
	static int disc(Map<Protection, BufferedImage> icons, int scale)
	{
		return disc(icons, scale, 1);
	}

	/** The same disc on a screen with {@code density} device pixels per pixel. */
	private static int disc(Map<Protection, BufferedImage> icons, int scale, int density)
	{
		return (spriteSize(icons) * scale + 6) * density;
	}

	/** Width of a row: disc, gap, arrow, gap, disc. */
	static int rowWidth(int disc)
	{
		return 2 * disc + arrowWidth(disc) + 2 * gap(disc);
	}

	static void drawRow(Graphics2D g, Map<Protection, BufferedImage> icons, Protection button, int x, int y, int scale)
	{
		drawRow(g, icons, button, x, y, scale, 1);
	}

	private static void drawRow(Graphics2D g, Map<Protection, BufferedImage> icons, Protection button, int x, int y, int scale, int density)
	{
		int disc = disc(icons, scale, density);
		int spriteScale = scale * density;
		drawIcon(g, icons.get(button), button, x, y, disc, spriteScale);
		int arrowX = x + disc + gap(disc);
		drawArrow(g, arrowX, y + disc / 2, arrowWidth(disc), disc * 7 / 10);
		Protection result = button.whenClickedCursed();
		drawIcon(g, icons.get(result), result, arrowX + arrowWidth(disc) + gap(disc), y, disc, spriteScale);
	}

	/**
	 * A row as an image for the sidebar, with sprites at {@code scale}, drawn for a screen with
	 * {@code density} device pixels per pixel (2 for 200 % Windows display scaling).
	 */
	static BufferedImage rowImage(Map<Protection, BufferedImage> icons, Protection button, int scale, int density)
	{
		int disc = disc(icons, scale, density);
		BufferedImage image = new BufferedImage(rowWidth(disc) + density, disc + density, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		drawRow(g, icons, button, 0, 0, scale, density);
		g.dispose();
		return image;
	}

	private static int arrowWidth(int disc)
	{
		return disc * 9 / 10;
	}

	private static int gap(int disc)
	{
		return Math.max(3, disc / 7);
	}

	private static void drawIcon(Graphics2D g, @Nullable BufferedImage sprite, Protection p, int x, int y, int size, int scale)
	{
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(DISC_EDGE);
		g.fillOval(x, y, size, size);
		g.setColor(DISC);
		g.fillOval(x + 1, y + 1, size - 2, size - 2);
		if (sprite == null)
		{
			g.setColor(Color.WHITE);
			g.drawString(p.label().substring(0, 2), x + size / 4, y + size * 2 / 3);
			return;
		}
		// Whole-number scale with no smoothing: every sprite pixel stays a crisp square
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		int w = sprite.getWidth() * scale;
		int h = sprite.getHeight() * scale;
		g.drawImage(sprite, x + (size - w) / 2, y + (size - h) / 2, w, h, null);
	}

	/** A chunky yellow arrow pointing right, centred on {@code midY}. */
	private static void drawArrow(Graphics2D g, int x, int midY, int width, int height)
	{
		int shaftHalf = Math.max(2, height / 4);
		int headStart = x + width * 11 / 20;
		int half = height / 2;
		Polygon arrow = new Polygon();
		arrow.addPoint(x, midY - shaftHalf);
		arrow.addPoint(headStart, midY - shaftHalf);
		arrow.addPoint(headStart, midY - half);
		arrow.addPoint(x + width, midY);
		arrow.addPoint(headStart, midY + half);
		arrow.addPoint(headStart, midY + shaftHalf);
		arrow.addPoint(x, midY + shaftHalf);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(ARROW);
		g.fillPolygon(arrow);
		g.setColor(ARROW_LIGHT);
		g.fillRect(x + 1, midY - shaftHalf + 1, Math.max(1, (headStart - x) / 2), Math.max(1, shaftHalf));
		Stroke before = g.getStroke();
		g.setStroke(new BasicStroke(Math.max(1.5f, height / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
		g.setColor(Color.BLACK);
		g.drawPolygon(arrow);
		g.setStroke(before);
	}
}
