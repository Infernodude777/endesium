# QA Test Cases - 10-Hour Audit-Fix Session

These are the manual checks to run after the audit-fix session. Automated
checks live in `tools/audit_verify.py` and `tools/verify_guidebook.py`; this
file covers the in-game and on-disk checks that need a human or a live server.

## Guidebook

1. Hold the guidebook in the inventory. The icon must be pixel-crisp, not
   soft or blurry, at GUI scales 1, 2, 3, and 4.
2. Place the guidebook in an item frame. The icon must be crisp there too.
3. Open the book. The title must be gold and readable, the body light gray on
   a dark page, with no text clipping at the bottom of any page.
4. Flip through every page with the arrow keys and by clicking both halves of
   the page. The page indicator must match the number of pages.
5. Confirm the new pages exist: Field Notes (5), Crafting Reference (4),
   Resonance (2), Post-Dragon (2), Builder's Notes (2).

## World generation

6. Generate a fresh world. The log must contain no "Detected setBlock in a
   far chunk" warnings.
7. Fly to several End Ruins. Each must sit on real ground - no floating
   panels, no buried barrels, no clipping into cliffs.
8. Find a Shattered Spire. Its 23x23 platform must follow the terrain, not
   hover over a dip or cut into a rise.
9. Find a Resonant Archive. Same platform check; its core must be inert until
   the Dragon is defeated.
10. Confirm ruins, spires, and archives never overlap each other.

## Resonance

11. Kill a Void Stalker as a player. Resonance must increase by 4.
12. Kill a Dust Crawler. Resonance must increase by 2.
13. Kill enough mobs to cross 20 resonance. A chat notice must name the new
    tier ("Attuned").
14. Restart the server. Resonance must persist for the same player.
15. Kill a mob with a non-player source (e.g. fall damage). No resonance must
    be granted.

## Textures

16. Run `python tools/fix_power_of_two_textures.py`. It must report all
    textures already power-of-two (or fix any strays and keep *.bak.png).
17. Run `python tools/audit_verify.py`. All checks must PASS.
18. Run `python tools/verify_guidebook.py`. All checks must PASS.
