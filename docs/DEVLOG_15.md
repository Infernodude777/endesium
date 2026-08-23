# Devlog 15

hey. it's been a minute.

this one is mostly bug fixing, and honestly it's the kind of bug fixing that should have happened a while ago. two big ones got found and killed.

## bosses were quietly not spawning

so this one was embarrassing. the wardens were supposed to be standing in every flagship - crypt floors, balcony rings, throne daises, all of it. and most of them just... weren't there.

dug into it and the placement helper was asking for a heightmap that doesn't even exist yet during world gen. so the game would try to figure out where the ground was, get garbage back, fail the collision check, and silently give up. no error. no warning. just no boss. ten flagships, one guy maybe shows up.

the fix: use the surface heightmap that actually works mid-generation, check footing with real collision shapes instead of vibes, and stop teleporting interior wardens to the roof. a warden meant to stand in a crypt now stands in the crypt. if there's genuinely nowhere legal to stand, we log it loudly instead of pretending everything's fine.

same disease had spread to the end golem - dying to the dragon could spawn him embedded inside obsidian, fully alive and fully useless. he tries both flanks of the portal now, and if there's no room he just doesn't wake up. better no colossus than a buried one.

also the golem effigy ate itself on a failed placement. that's fixed too. it refunds you now. it should have from day one.

## the structure grid. oh no

someone (me) pointed out that flagships spawn "in a straight line". went to check thinking it'd be a spacing tweak.

it was worse than that. the placement gate checked chunk coords against two fixed numbers with modulo math - which doesn't pick ONE spot per cell, it picks EVERY chunk that matches the mod. so the game was building a perfect lattice. cathedral, 160 blocks, cathedral, 160 blocks, forever, in neat little rows like crops.

landmarks had the same bug at 256 block scale.

both features now hash the cell coordinates themselves to pick a single host chunk per region per cell, so every attempt lands somewhere different. flagships also got pushed out to a 384 block minimum since they're supposed to be rare landmarks, not fence posts. and landmarks now refuse to generate within spitting distance of a flagship so you don't get a gazebo clipping through a castle's probe ring anymore.

worlds will need regen to see the new layout. old chunks keep whatever chaos they generated with.

## small warden polish while i was in there

the boss bar used to follow you around like a bad memory once you'd seen a warden once. it's proximity-based now - walk away 64 blocks and it goes away.

region attunement also retries now instead of giving up permanently, so no more white-bar wardens that never learned which biome they live in. and enrage gets an actual audio cue instead of just... happening.

## numbers

six files touched, ~135k characters retyped across the session. biggest single file was BiomeStructureFeature at over 70k. my keyboard may never forgive me.

## what's next

back to content. more landmark variants, ambient life in the skies, the deep end is still sitting there on the roadmap being ominous.

go find a cathedral. they're actually where they say they are now.
