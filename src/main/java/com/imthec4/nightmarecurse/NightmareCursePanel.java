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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.LinkBrowser;

/** The sidebar: the fixed prayer table and what it means. */
class NightmareCursePanel extends PluginPanel
{
	private static final Color CARD = ColorScheme.DARKER_GRAY_COLOR;
	/** Text widths in the sidebar: Swing wraps HTML only when it is given a width. */
	private static final int TEXT_WIDTH = 150;
	/** Widest a table row may be in the sidebar card. */
	private static final int ROW_MAX_WIDTH = 185;
	/** Discord's brand colour and its darker hover shade, as in the author's other plugins. */
	private static final Color DISCORD = new Color(0x5865F2);
	private static final Color DISCORD_HOVER = new Color(0x4752C4);
	static final String DISCORD_URL = "https://discord.gg/XgxjhyznbZ";

	private final JPanel rows = new JPanel(new GridLayout(0, 1, 0, 6));
	private final Map<Protection, BufferedImage> icons = new EnumMap<>(Protection.class);

	NightmareCursePanel(@Nullable SpriteManager spriteManager)
	{
		setLayout(new BorderLayout(0, 8));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
		top.setOpaque(false);

		JLabel title = new JLabel("Nightmare Curse Helper");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(Color.WHITE);
		title.setAlignmentX(LEFT_ALIGNMENT);
		top.add(title);
		top.add(gap(8));

		JPanel tableCard = card(new BorderLayout(0, 6));
		JLabel tableTitle = new JLabel("While cursed");
		tableTitle.setFont(FontManager.getRunescapeBoldFont());
		tableTitle.setForeground(Color.WHITE);
		tableCard.add(tableTitle, BorderLayout.NORTH);
		rows.setOpaque(false);
		tableCard.add(rows, BorderLayout.CENTER);
		tableCard.add(note("Left: the prayer you click. Right: the protection you get. "
			+ "A fixed table: the plugin never looks at the fight."), BorderLayout.SOUTH);
		top.add(tableCard);

		add(top, BorderLayout.NORTH);
		add(discordButton(), BorderLayout.SOUTH);

		rebuildRows();
		if (spriteManager != null)
		{
			for (Protection p : Protection.values())
			{
				spriteManager.getSpriteAsync(p.sprite(), 0, (BufferedImage image) -> javax.swing.SwingUtilities.invokeLater(() ->
				{
					icons.put(p, image);
					rebuildRows();
				}));
			}
		}
	}

	/** Lets a preview supply the prayer icons without a running client. */
	void setIcon(Protection p, BufferedImage image)
	{
		icons.put(p, image);
		rebuildRows();
	}

	private void rebuildRows()
	{
		rows.removeAll();
		// The largest whole-number sprite scale that fits; extra 2x and 3x copies stay sharp on
		// screens with Windows display scaling above 100 %
		int scale = 1;
		while (scale < 4 && CurseArt.rowWidth(CurseArt.disc(icons, scale + 1)) <= ROW_MAX_WIDTH)
		{
			scale++;
		}
		for (Protection button : Protection.ROWS)
		{
			Image image = new BaseMultiResolutionImage(CurseArt.rowImage(icons, button, scale, 1),
				CurseArt.rowImage(icons, button, scale, 2), CurseArt.rowImage(icons, button, scale, 3));
			JLabel row = new JLabel(new ImageIcon(image));
			row.setHorizontalAlignment(SwingConstants.CENTER);
			row.setToolTipText("Click Protect from " + button.label() + ": you get Protect from " + button.whenClickedCursed().label());
			rows.add(row);
		}
		rows.revalidate();
		rows.repaint();
	}

	private static JPanel card(java.awt.LayoutManager layout)
	{
		JPanel card = new JPanel(layout);
		card.setBackground(CARD);
		card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		card.setAlignmentX(LEFT_ALIGNMENT);
		return card;
	}

	private static JLabel note(String text)
	{
		JLabel label = new JLabel(html(TEXT_WIDTH, text));
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		return label;
	}

	private static String html(int width, String body)
	{
		return "<html><div style='width:" + width + "px'>" + body + "</div></html>";
	}

	private static JPanel gap(int height)
	{
		JPanel gap = new JPanel();
		gap.setOpaque(false);
		gap.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, height));
		gap.setPreferredSize(new java.awt.Dimension(0, height));
		gap.setAlignmentX(LEFT_ALIGNMENT);
		return gap;
	}

	private static JLabel discordButton()
	{
		// Opens the browser only when clicked; the plugin itself never contacts Discord
		JLabel discord = new JLabel("Join the Discord", SwingConstants.CENTER);
		discord.setOpaque(true);
		discord.setBackground(DISCORD);
		discord.setForeground(Color.WHITE);
		discord.setFont(FontManager.getRunescapeBoldFont());
		discord.setBorder(new EmptyBorder(7, 0, 7, 0));
		discord.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		discord.setToolTipText("Questions, ideas or a bug? Opens " + DISCORD_URL + " in your browser");
		discord.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				LinkBrowser.browse(DISCORD_URL);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				discord.setBackground(DISCORD_HOVER);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				discord.setBackground(DISCORD);
			}
		});
		return discord;
	}
}
