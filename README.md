# Nightmare Curse Helper

A reference card for the curse at The Nightmare and Phosani's Nightmare. While you are cursed,
each protection prayer button turns on a different protection. This plugin shows that table in a
small box next to the fight and in the sidebar, together with a tick or a cross that repeats
what the game's chat already told you: cursed or not.

The table is always the same. The plugin does not look at the boss's attacks, does not tell you
which prayer to use at any moment and does not time anything.

## While cursed

| You click | You get |
| --- | --- |
| Protect from Melee | Protect from Magic |
| Protect from Magic | Protect from Missiles |
| Protect from Missiles | Protect from Melee |

In the box and the sidebar each row is drawn as two prayer icons with an arrow: the prayer you
click on the left, the protection you get on the right.

## The box

The box appears in the Nightmare's arena. Its title is followed by the curse state, a green
tick and *Not cursed* or a red cross and *Cursed*, and by the table. The state comes from the
game's own chat lines, "The Nightmare has cursed you, shuffling your prayers!" and "You feel the
effects of the Nightmare's curse wear off.", and also clears when you die, leave the arena or log
out.

Turn off *Box only at the Nightmare* to keep the box on screen everywhere, for example to learn the
table before a trip. Outside the arena it shows just the table.

## Settings

| Setting | Default | What it does |
| --- | --- | --- |
| Show box | On | The box in the arena |
| Box only at the Nightmare | On | Off: the box stays on screen everywhere, with just the table outside the arena |
| Show curse state | On | The tick or cross in the box |
| Show table | While cursed | Show the table only while cursed, or always |
| Side panel | On | The table and a short explanation in the sidebar |
| Panel only at the Nightmare | On | Off: the sidebar button stays there everywhere |

## Data

The plugin stores nothing and makes no network requests.

## Support

Questions, ideas or a bug? Join the [Discord](https://discord.gg/XgxjhyznbZ) or open an issue on
GitHub. The side panel has a *Join the Discord* button as well; it only opens your browser when
you click it.
