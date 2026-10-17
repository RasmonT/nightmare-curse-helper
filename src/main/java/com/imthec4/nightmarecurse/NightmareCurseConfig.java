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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(NightmareCurseConfig.GROUP)
public interface NightmareCurseConfig extends Config
{
	String GROUP = "nightmarecursehelper";

	@ConfigItem(
		keyName = "showOverlay",
		name = "Show box",
		description = "A box at The Nightmare and Phosani's Nightmare with the curse state and the prayer table",
		position = 0
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "onlyAtNightmare",
		name = "Box only at the Nightmare",
		description = "Show the box only in the arena of The Nightmare and Phosani's Nightmare. Turn it off to keep"
			+ " the table on screen everywhere; outside the arena the box shows just the table",
		position = 1
	)
	default boolean onlyAtNightmare()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showStatus",
		name = "Show curse state",
		description = "Cursed or not, as the game's chat says; a tick or a cross in the box",
		position = 2
	)
	default boolean showStatus()
	{
		return true;
	}

	@ConfigItem(
		keyName = "tableMode",
		name = "Show table",
		description = "When the box shows which prayer button turns on which protection: only while cursed, or always",
		position = 3
	)
	default TableMode tableMode()
	{
		return TableMode.WHILE_CURSED;
	}

	@ConfigItem(
		keyName = "showPanel",
		name = "Side panel",
		description = "The table and a short explanation in RuneLite's sidebar",
		position = 4
	)
	default boolean showPanel()
	{
		return true;
	}

	@ConfigItem(
		keyName = "panelOnlyAtNightmare",
		name = "Panel only at the Nightmare",
		description = "Show the sidebar button only in the arena of The Nightmare and Phosani's Nightmare."
			+ " Turn it off to keep it in the sidebar everywhere",
		position = 5
	)
	default boolean panelOnlyAtNightmare()
	{
		return true;
	}
}
