# Commands

Endesium ships **one custom command**, and it is development-only.

## Endesium commands

| Command | Arguments | Permission | Purpose | Production/Debug |
|---|---|---|---|---|
| `/endesium dragonstate get` | none | level 2 (op) | print the current post-Dragon transformation state | Debug |
| `/endesium dragonstate set <true\|false>` | boolean | level 2 (op) | set the post-Dragon transformation state for testing | Debug |

### `/endesium dragonstate`

Development-only. It exists so the post-Dragon layer can be tested without
fighting the Ender Dragon. It does **not** bypass production rewards: the
Resonant Archive still requires a real Lens activation, and the transformation
still requires the state to be active.

```
/endesium dragonstate get
/endesium dragonstate set true
/endesium dragonstate set false
```

The command validates its argument (must be `true` or `false`), requires
operator permission (level 2), and reports the resulting state. It is a normal
vanilla-style server command and works identically on dedicated servers.

## Everything else

No other custom commands are registered. A full search for
`CommandRegistrationCallback` and `Commands.literal` across the source tree
returns only the `EndesiumCommands` registration class above.

This means:

- There is no other command input-validation or permission surface to maintain.
- There is no other debug command that could leak a production cheat.
- There is no command that could corrupt world state through misuse.

## Vanilla commands that interact with Endesium

| Command | Purpose | Notes |
|---|---|---|
| `/locate biome endesium:end_wastes` | find End Wastes | depends on the biome being in the source's possible set |
| `/locate biome endesium:chorus_wilds` | find Chorus Wilds | same |
| `/summon endesium:void_stalker` | spawn a Void Stalker | entity registered under `endesium:void_stalker` |
| `/give @s endesium:void_shard` | obtain a Void Shard | |
| `/give @s endesium:resonance_lens` | obtain a Resonance Lens | |
| `/give @s endesium:resonance_token` | obtain a Resonance Token | |
| `/give @s endesium:echo_compass` | obtain an Echo Compass | |
| `/give @s endesium:resonant_bloom` | obtain a Resonant Bloom | |
| `/give @s endesium:archive_sigil` | obtain an Archive Sigil | |

Endesium structures are world-generation **features**, not registered
structures, so `/locate structure` does not list them. This is intentional:
structures are meant to be discovered, not located.

## Testing without commands

The primary QA loop is headless and does not require an interactive session:

- `tools/qa_run.sh <tag>` starts a fresh dedicated server and pipes a script of
  locate/give/summon commands.
- `tools/qa_server_test.sh` is the command script itself.
- `tools/parse_mca.mjs` and `tools/scan_region_blocks.mjs` inspect generated
  chunks on disk to confirm biomes and feature blocks.

## Future commands

Any future development-only tool should follow the same pattern as
`/endesium dragonstate`: registered behind a permission level check
(`source.hasPermission(2)`), documented as development-only, and designed so it
never becomes a normal-player dependency or a cheat surface.
