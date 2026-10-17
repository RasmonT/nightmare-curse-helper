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

/**
 * Whether the player is cursed, taken only from the game's own chat lines (the same at The
 * Nightmare and Phosani's Nightmare):
 * "The Nightmare has cursed you, shuffling your prayers!" and
 * "You feel the effects of the Nightmare's curse wear off."
 * Only the stable middle part of each line is matched. Dying, leaving the arena or logging out
 * clears it, so a missed end line can never leave it stuck.
 */
class CurseState
{
	static final String START = "cursed you, shuffling your prayers";
	static final String END = "curse wear off";

	private boolean cursed;

	boolean isCursed()
	{
		return cursed;
	}

	/** A game message, tags already removed. Returns true if the state changed. */
	boolean onMessage(String message)
	{
		if (message.contains(START))
		{
			return set(true);
		}
		if (message.contains(END))
		{
			return set(false);
		}
		return false;
	}

	/** Returns true if the state changed. */
	boolean clear()
	{
		return set(false);
	}

	private boolean set(boolean now)
	{
		boolean changed = cursed != now;
		cursed = now;
		return changed;
	}
}
