# Devlog 13
hey guys.

this one might be the biggest one yet. we called it the 10x overhaul and honestly it earned the name.

## structures got a whole second tier
the ten flagships were already there (dust cathedral, skyrend keep, great caldera...), but walking between them was empty space. so now every region also scatters a LANDMARK tier on a roughly 256-block grid:

    dune fossil arch        (wastes)
    hollow stump            (wilds)
    windvane watchtower     (highlands)
    mire bell cairn         (marshes)
    lightwell gazebo        (groves)
    ember shrine            (ashen)
    shard spire cluster     (barrens)
    anchor ruin             (skirts)
    needle circle           (crown)
    null obelisk            (umbral)

each one has curated biome loot and a wakeable mini-mechanism that pays a small resonance cache. you basically cannot walk for two minutes without finding something.

## bosses are real now
END WARDEN: one per flagship vault, attuned to its region. ten texture palettes, region accessory bones (crests in the wastes, horns at the caldera, halos in the groves), a signature attack per region, a guard stance that nullifies frontal damage, minion calls at 66%, enrage below half. drops a WARDEN SIGIL keyed to its region.

sigils are the temptation: carry one for regen in the End, or consume it for a PERMANENT extra heart. all ten attuned = Warden Ascendant, an aura + regen pulse forever.

END GOLEM: wakes where the dragon falls. three phases, boss bar goes purple -> yellow -> red, homing barrage, shockwaves, beam sweeps, arena tether. the skill mechanic is the STAGGER: 60+ damage inside 8 seconds kneels it for 5 seconds of double damage. kills drop GOLEM CORES - absorb them for permanent +hearts AND +attack damage. ten cores unlocks Golem's Resolve: once per day, death refuses you.

and yes, you can craft a GOLEM EFFIGY to summon another one. renewable cores, deliberate pacing.

## the mobs stopped being clones
rebuild five models from scratch: nullwalker is finally what it was always supposed to be (a tattered void specter with orbiting shroud shards - sorry about the gray cube man), ash wraith is a hooded ember wraith with a flame crown, dust crawler is an actual scarab beetle with six legs, marsh crawler is a crocodilian with a proper jaw, void ray is a manta with tail streamers.

abilities diversified too: dust crawlers burrow-escape at low hp, marsh crawlers pounce, enraged wraith bolts ignite. no more nine mobs sharing one brain.

## the bug harvest
fixed along the way: buried boss spawns, duplicate golems, stuck casting flags, a BOM in ten loot tables (powershell, never change), an animation that overflowed its own length and glitched the volcano mob, lumen moth spawners sealed inside solid stone, and the ashwalker boots quietly doing nothing. all documented in the testing runbook now.

## whats next
more ruin variants for the mid-tier, ambient life for the empty skies, maybe the deep end. see the roadmap.

go get your hearts.
