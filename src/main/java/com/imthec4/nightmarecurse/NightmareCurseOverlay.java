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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.BackgroundComponent;
import net.runelite.client.ui.overlay.components.TextComponent;

/** The box: the fixed table of which prayer button turns on which protection while cursed. */
class NightmareCurseOverlay extends Overlay
{
	private static final Color HEADER = new Color(0xB8B8B8);

	private static final int PAD = 6;
	/** Sprites at their own size in the box: 1x keeps them pixel-sharp. */
	private static final int SCALE = 1;
	private static final int ROW_GAP = 4;
	private static final int MIN_WIDTH = 112;

	private final NightmareCurseHelperPlugin plugin;
	private final NightmareCurseConfig config;
	private final SpriteManager spriteManager;
	private final Map<Protection, BufferedImage> icons = new EnumMap<>(Protection.class);

	@Inject
	NightmareCurseOverlay(NightmareCurseHelperPlugin plugin, NightmareCurseConfig config, SpriteManager spriteManager)
	{
		this.plugin = plugin;
		this.config = config;
		this.spriteManager = spriteManager;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		if (!config.showOverlay() || (config.onlyAtNightmare() && !plugin.isInArena()))
		{
			return null;
		}
		return draw(g);
	}

	/** Draws the box at (0, 0); also used to render a preview outside the game. */
	Dimension draw(Graphics2D g)
	{
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Font bold = FontManager.getRunescapeBoldFont();
		Font small = FontManager.getRunescapeSmallFont();
		FontMetrics boldMetrics = g.getFontMetrics(bold);
		FontMetrics smallMetrics = g.getFontMetrics(small);

		String title = "Nightmare curse";
		String left = "Click";
		String right = "Get";

		loadIcons();
		int disc = CurseArt.disc(icons, SCALE);
		int rowWidth = CurseArt.rowWidth(disc);
		int width = Math.max(MIN_WIDTH, Math.max(boldMetrics.stringWidth(title), rowWidth) + 2 * PAD);
		int height = PAD + boldMetrics.getHeight() + 4 + smallMetrics.getHeight() + 3 * (disc + ROW_GAP) + PAD - 2;

		BackgroundComponent background = new BackgroundComponent();
		background.setRectangle(new Rectangle(0, 0, width, height));
		background.render(g);

		int y = PAD + boldMetrics.getAscent();
		text(g, bold, title, (width - boldMetrics.stringWidth(title)) / 2, y, Color.WHITE);
		y += boldMetrics.getDescent();

		int rowLeft = (width - rowWidth) / 2;
		int rightIcon = rowLeft + rowWidth - disc;
		y += 4 + smallMetrics.getAscent();
		text(g, small, left, rowLeft + (disc - smallMetrics.stringWidth(left)) / 2, y, HEADER);
		text(g, small, right, rightIcon + (disc - smallMetrics.stringWidth(right)) / 2, y, HEADER);
		y += smallMetrics.getDescent();
		for (Protection button : Protection.ROWS)
		{
			CurseArt.drawRow(g, icons, button, rowLeft, y + ROW_GAP / 2, SCALE);
			y += disc + ROW_GAP;
		}
		return new Dimension(width, height);
	}

	private void loadIcons()
	{
		if (spriteManager == null || icons.size() == Protection.values().length)
		{
			return;
		}
		for (Protection p : Protection.values())
		{
			if (!icons.containsKey(p))
			{
				BufferedImage image = spriteManager.getSprite(p.sprite(), 0);
				if (image != null)
				{
					icons.put(p, image);
				}
			}
		}
	}

	/** Lets a preview supply the prayer icons without a running client. */
	void setIcon(Protection p, BufferedImage image)
	{
		icons.put(p, image);
	}

	private static void text(Graphics2D g, Font font, String s, int x, int y, Color color)
	{
		TextComponent t = new TextComponent();
		t.setFont(font);
		t.setText(s);
		t.setColor(color);
		t.setPosition(new Point(x, y));
		t.render(g);
	}
}
