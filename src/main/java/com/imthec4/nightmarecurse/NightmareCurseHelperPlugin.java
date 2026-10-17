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

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.Locale;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

/**
 * A reference card for the Nightmare's curse: the fixed table of which protection prayer button
 * turns on which protection while cursed, in a box and in the sidebar. It does not detect the
 * curse or anything else about the fight; the only game state it uses is whether the player is
 * at the Nightmare, to show the box and the sidebar button there.
 *
 * The arena is region 15515 for both The Nightmare and Phosani's Nightmare (it is an instance,
 * so the region comes from the instance template); a Nightmare NPC in the scene counts too.
 */
@PluginDescriptor(
	name = "Nightmare Curse Helper",
	description = "A reference card for the Nightmare's curse: which prayer button turns on which protection",
	tags = {"nightmare", "phosani", "curse", "prayer", "shuffle", "pvm"}
)
public class NightmareCurseHelperPlugin extends Plugin
{
	static final int ARENA_REGION = 15515;

	@Inject
	private Client client;

	@Inject
	private NightmareCurseConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private NightmareCurseOverlay overlay;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private SpriteManager spriteManager;

	private boolean inArena;
	private NavigationButton navButton;
	/** Whether the sidebar button is added right now; only touched on the Swing thread. */
	private boolean navShown;

	@Provides
	NightmareCurseConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NightmareCurseConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		BufferedImage icon = ImageUtil.loadImageResource(NightmareCurseHelperPlugin.class, "panel_icon.png");
		navButton = NavigationButton.builder()
			.tooltip("Nightmare Curse Helper")
			.icon(icon)
			.priority(10)
			.panel(new NightmareCursePanel(spriteManager))
			.build();
		updateNav();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		NavigationButton button = navButton;
		SwingUtilities.invokeLater(() ->
		{
			clientToolbar.removeNavigation(button);
			navShown = false;
		});
		inArena = false;
		navButton = null;
	}

	boolean isInArena()
	{
		return inArena;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		boolean wasInArena = inArena;
		inArena = findArena();
		if (inArena != wasInArena)
		{
			updateNav();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if ((state == GameState.LOGIN_SCREEN || state == GameState.HOPPING) && inArena)
		{
			inArena = false;
			updateNav();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (NightmareCurseConfig.GROUP.equals(event.getGroup())
			&& ("showPanel".equals(event.getKey()) || "panelOnlyAtNightmare".equals(event.getKey())))
		{
			updateNav();
		}
	}

	static boolean wantPanel(boolean showPanel, boolean onlyAtNightmare, boolean inArena)
	{
		return showPanel && (!onlyAtNightmare || inArena);
	}

	/** Adds or removes the sidebar button: setting on, and in the arena unless it should always show. */
	private void updateNav()
	{
		NavigationButton button = navButton;
		if (button == null)
		{
			return;
		}
		boolean want = wantPanel(config.showPanel(), config.panelOnlyAtNightmare(), inArena);
		SwingUtilities.invokeLater(() ->
		{
			if (navButton != button || want == navShown)
			{
				return;
			}
			if (want)
			{
				clientToolbar.addNavigation(button);
			}
			else
			{
				clientToolbar.removeNavigation(button);
			}
			navShown = want;
		});
	}

	private boolean findArena()
	{
		Player me = client.getLocalPlayer();
		if (me == null)
		{
			return false;
		}
		if (WorldPoint.fromLocalInstance(client, me.getLocalLocation()).getRegionID() == ARENA_REGION)
		{
			return true;
		}
		for (NPC npc : client.getTopLevelWorldView().npcs())
		{
			String name = npc.getName();
			if (name != null && name.toLowerCase(Locale.ROOT).contains("nightmare"))
			{
				return true;
			}
		}
		return false;
	}
}
