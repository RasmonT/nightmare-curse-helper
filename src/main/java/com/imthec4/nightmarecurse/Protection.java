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

import net.runelite.api.gameval.SpriteID;

/**
 * The three protection prayers and how the Nightmare's curse shuffles them. While cursed, each
 * prayer button turns on a different protection (checked in game, The Nightmare, 2026-10-17:
 * clicking Protect from Melee turned on Magic, Magic turned on Missiles, Missiles turned on Melee).
 * This is a fixed table: it never depends on what the boss is doing.
 */
enum Protection
{
	MAGIC("Magic", SpriteID.Prayeron.PROTECT_FROM_MAGIC),
	MISSILES("Missiles", SpriteID.Prayeron.PROTECT_FROM_MISSILES),
	MELEE("Melee", SpriteID.Prayeron.PROTECT_FROM_MELEE);

	/** Table rows: the button you click, in the order players learn it (Melee, Magic, Missiles). */
	static final Protection[] ROWS = {MELEE, MAGIC, MISSILES};

	private final String label;
	private final int sprite;

	Protection(String label, int sprite)
	{
		this.label = label;
		this.sprite = sprite;
	}

	String label()
	{
		return label;
	}

	int sprite()
	{
		return sprite;
	}

	/** What this prayer button turns on while cursed. */
	Protection whenClickedCursed()
	{
		switch (this)
		{
			case MELEE:
				return MAGIC;
			case MAGIC:
				return MISSILES;
			default:
				return MELEE;
		}
	}

	/** The prayer button that turns this protection on while cursed. */
	Protection buttonWhileCursed()
	{
		for (Protection p : values())
		{
			if (p.whenClickedCursed() == this)
			{
				return p;
			}
		}
		throw new IllegalStateException();
	}
}
