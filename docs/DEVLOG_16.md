# Devlog 16 - The Fight Gets Teeth

Short one, but it is the one I have been putting off. The dragon fight was
authored - real attacks, real phases, real telegraphs - but the pillars were
decor and the last third of the health bar played identically to the first.
Three changes fix that.

## The pillars fight back

Crystal aegis is the simplest thing in the world: while crystals stand, the
dragon heals. One line of health regen did more for the fight's shape than any
attack I have authored, because it moves the win condition backwards. You do
not win by out-DPS-ing a health bar; you win by climbing pillars under fire.
Five crystals means two HP a second, which is enough to visibly undo a sloppy
opening. That is the fight telling you it is watching.

## Enrage is a ladder, not a switch

Three thresholds - 60, 35, 15 percent - each announced, each spawning void
wisps into the arena. The wisps are not the threat; they are the *clock*. Every
second you spend swatting a wisp off your back is a second the dragon is
cycling toward catastrophe. From enrage two the dragon leaves breath pools
under its flight path, which turns its flyovers into map control. By enrage
three the arena floor is a decision tree.

## A hoard worth the walk

The kill now drops a real pile: heart, fangs, bone, resonant scales. First kill
still fires the transformation. `/dragonfight` gives a live readout for tuning
the curve without guessing.

## Notes for next time

- The wisps cap at eight; if playtests say the floor is unreadable, that cap
  is the first knob.
- Aegis regen is intentionally visible (1-2 HP/s). If it feels unfair, the
  fix is fewer crystals required, not slower regen.
- Docs live in `docs/DRAGON_FIGHT.md` now, including the testing checklist.
