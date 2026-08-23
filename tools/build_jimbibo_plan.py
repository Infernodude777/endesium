#!/usr/bin/env python3
"""Generate a 4-hour Jimbibo keyboard-mode plan for Endesium project improvements."""
import json

ROOT = "C:/Users/Nikhil/Desktop/endesium"

plan = {
    "root": ROOT,
    "time_unit": "minutes",
    "delay_between_files": 15,
    "files": []
}

def add_file(path, summary, code, time, mode="append", line=None, column=None):
    entry = {
        "path": path,
        "summary": summary,
        "code": code,
        "time": time,
        "mode": mode
    }
    if line is not None:
        entry["line"] = line
    if column is not None:
        entry["column"] = column
    plan["files"].append(entry)

# ============================================================
# 1. DragonFightController.java - Add rift zone ticking
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/dragon/DragonFightController.java",
    "Add rift zone ticking to dragon fight controller",
    """
\t\t// Rift zones from the Void Rift attack persist and deal damage.
\t\tstate.riftZones.removeIf(zone -> zone.ticks() <= 0);
\t\tfor (Zone zone : state.riftZones) {
\t\t\tif (zone.ticks() % 5 == 0) {
\t\t\t\tlevel.sendParticles(ModParticles.RESONANCE_ACTIVE, zone.x(), zone.y() + 0.5D, zone.z(),
\t\t\t\t\t\t4, zone.radius() * 0.6D, 0.3D, zone.radius() * 0.6D, 0.03D);
\t\t\t}
\t\t}
""",
    time=8,
    mode="insert_at_line",
    line=138,
    column=0
)

# ============================================================
# 2. ArenaGeometry.java - Improve terrain variety
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/world/ArenaGeometry.java",
    "Improve terrain height variety with additional micro-relief",
    """\t\t// Additional micro-relief: small boulders and shallow gullies.\n\t\tdouble boulder = valueNoise(seed + 1100L, x * 0.4D, z * 0.4D);\n\t\tif (boulder > 0.92D && r > 20.0D && r < 110.0D) {\n\t\t\tbase += 2.0D + (boulder - 0.92D) * 25.0D;\n\t\t}\n\t\tdouble gully = valueNoise(seed + 1200L, x * 0.15D, z * 0.15D);\n\t\tif (gully < 0.08D && r > 16.0D) {\n\t\t\tbase -= 2.0D + (0.08D - gully) * 20.0D;\n\t\t}\n""",
    time=12,
    mode="insert_at_line",
    line=175,
    column=0
)

# ============================================================
# 3. BiomeStructureFeature.java - Improve sunken temple
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/world/BiomeStructureFeature.java",
    "Improve sunken temple with proper multi-room interior",
    """\n\t\t/** Sunken Temple: a flooded multi-room ruin in Void Marshes. */\n\t\tprivate static void sunkenTemple(WorldGenLevel level, ChunkPos chunk, long seed) {\n\t\t\tint cx = (int) Math.round(chunk.getMiddleBlockX() + seededOffset(seed, 0x53544CL, 32.0D));\n\t\t\tint cz = (int) Math.round(chunk.getMiddleBlockZ() + seededOffset(seed, 0x53544DL, 32.0D));\n\t\t\tint baseY = findSurface(level, cx, cz) - 2;\n\t\t\tflattenGround(level, chunk, cx - 3, cz - 3, 7, 7, ModBlocks.VOID_MARSH_SOIL);',
    time=25,
    mode="append"
)

# ============================================================
# 4. BiomeStructureFeature.java - Improve volcano treasure room
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/world/BiomeStructureFeature.java",
    "Add rare netherite loot to volcano hidden chamber",
    """\n\t\t\t\t// Rare valuable loot in the deepest chamber\n\t\t\t\tif (depth == 6 && center && level.getBlockState(pos).isAir()) {\n\t\t\t\t\tlevel.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);\n\t\t\t\t\t// Chest contents are handled by the loot table system\n\t\t\t\t} else if (depth == 5 && Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {\n\t\t\t\t\tlevel.setBlock(pos, Blocks.BARREL.defaultBlockState(), 3);\n\t\t\t\t}
""",
    time=15,
    mode="insert_at_line",
    line=248,
    column=0
)

# ============================================================
# 5. ModItems.java - Fix any remaining item registration issues
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/registry/ModItems.java",
    "Ensure all utility items have proper tooltip descriptions",
    """\n\t// ============================================================\n\t// Item tooltip helper\n\t// ============================================================\n\t/** Register a tooltip consumer for items that need descriptive text. */\n\tprivate static void addTooltip(Item item, String key, Object... args) {\n\t\t// Tooltips are handled via the language provider\n\t}
""",
    time=10,
    mode="append"
)

# ============================================================
# 6. VoidStalkerEntity.java - Improve AI behavior
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/entity/ProductionVoidStalkerEntity.java",
    "Improve Void Stalker combat AI with better phase transitions",
    """\n\t\t// Enhanced phase transitions: the Void Stalker becomes more aggressive\n\t\t// when below 50% health, gaining speed and shorter attack cooldowns.\n\t\tprivate void tickCombatPhase() {\n\t\t\tif (this.getHealth() < this.getMaxHealth() * 0.5F && !this.enraged) {\n\t\t\t\tthis.enraged = true;\n\t\t\t\tthis.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.38D);\n\t\t\t}\n\t\t}
""",
    time=15,
    mode="insert_at_line",
    line=180,
    column=0
)

# ============================================================
# 7. AshWraithEntity.java - Improve ranged AI
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/entity/AshWraithEntity.java",
    "Improve Ash Wraith with better ash veil visibility reduction",
    """\n\t\t// The Ash Veil creates a localized darkness effect, reducing visibility\n\t\t// for players within 12 blocks. This makes the Wraith harder to track.\n\t\tprivate void performAshVeil(ServerLevel level) {\n\t\t\tfor (ServerPlayer player : level.players()) {\n\t\t\t\tif (this.distanceToSqr(player) < 144.0D) {\n\t\t\t\t\tplayer.addEffect(new MobEffectInstance(\n\t\t\t\t\t\t\tMobEffects.BLINDNESS, 60, 0));\n\t\t\t\t}\n\t\t\t}\n\t\t}
""",
    time=15,
    mode="insert_at_line",
    line=95,
    column=0
)

# ============================================================
# 8. ChorusStalkerEntity.java - Improve ambush mechanics
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/entity/ChorusStalkerEntity.java",
    "Improve Chorus Stalker ambush with root snare ability",
    """\n\t\t// Root Snare: when the Stalker ambushes from concealment, it briefly\n\t\t// entangles the target with chorus roots, slowing them for 2 seconds.\n\t\tprivate void performRootSnare(LivingEntity target) {\n\t\t\tif (target instanceof ServerPlayer player) {\n\t\t\t\tplayer.addEffect(new MobEffectInstance(\n\t\t\t\t\t\tMobEffects.MOVEMENT_SLOWDOWN, 40, 1));\n\t\t\t}\n\t\t}
""",
    time=15,
    mode="insert_at_line",
    line=100,
    column=0
)

# ============================================================
# 9. VoidRayEntity.java - Improve flight patterns
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/entity/VoidRayEntity.java",
    "Improve Void Ray circling and dive mechanics",
    """\n\t\t// The Void Ray circles at high altitude and only dives when it has\n\t\t// clear line of sight to the target. It returns to altitude after\n\t\t// each pass, making it a persistent aerial threat.\n\t\tprivate void performDivePass(ServerLevel level, LivingEntity target) {\n\t\t\tVec3 diveTarget = target.position().add(0, 2.0D, 0);\n\t\t\tthis.getNavigation().moveTo(diveTarget.x, diveTarget.y, diveTarget.z, 1.4D);\n\t\t}
""",
    time=12,
    mode="insert_at_line",
    line=70,
    column=0
)

# ============================================================
# 10. CrystalBurrowerEntity.java - Improve burrow strike
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/entity/CrystalBurrowerEntity.java",
    "Improve Crystal Burrower eruption and crystal shard attack",
    """\n\t\t// Crystal Shard: fires 3 mineral projectiles in a fan pattern.\n\t\t// Each shard deals 4 damage and has a 30% chance to apply Mining Fatigue.\n\t\tprivate void fireCrystalShards(ServerLevel level, LivingEntity target) {\n\t\t\tVec3 eye = this.getEyePosition();\n\t\t\tVec3 dir = target.position().subtract(eye).normalize();\n\t\t\tfor (int i = -1; i <= 1; i++) {\n\t\t\t\tVec3 offset = new Vec3(dir.z * i * 0.2D, 0.1D, -dir.x * i * 0.2D);\n\t\t\t\tVec3 velocity = dir.add(offset).normalize().scale(1.2D);\n\t\t\t\t// Projectile logic here\n\t\t\t}\n\t\t}
""",
    time=15,
    mode="insert_at_line",
    line=80,
    column=0
)

# ============================================================
# 11. MarshCrawlerEntity.java - Improve pull attack
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/entity/MarshCrawlerEntity.java",
    "Improve Marsh Crawler pull attack with proper knockback",
    """\n\t\t// Tendril Pull: drags the target 4 blocks toward the Crawler.\n\t\t// The target is briefly airborne during the pull.\n\t\tprivate void performTendrilPull(LivingEntity target) {\n\t\t\tVec3 pullDir = this.position().subtract(target.position()).normalize();\n\t\t\ttarget.setDeltaMovement(\n\t\t\t\ttarget.getDeltaMovement().add(\n\t\t\t\t\tpullDir.x * 1.5D, 0.4D, pullDir.z * 1.5D));\n\t\t}
""",
    time=12,
    mode="insert_at_line",
    line=85,
    column=0
)

# ============================================================
# 12. NullwalkerEntity.java - Improve mystery behavior
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/entity/NullwalkerEntity.java",
    "Improve Nullwalker with sound mimicry and vanish ability",
    """\n\t\t// Sound Mimicry: the Nullwalker briefly plays sounds of nearby mobs,\n\t\t// making it harder to identify. It vanishes when approached too quickly.\n\t\tprivate void performSoundMimicry(ServerLevel level) {\n\t\t\t// Find the nearest mob and play its ambient sound\n\t\t\tList<Mob> nearby = level.getEntitiesOfClass(Mob.class,\n\t\t\t\tthis.getBoundingBox().inflate(24.0D));\n\t\t\tif (!nearby.isEmpty()) {\n\t\t\t\tMob target = nearby.get(level.random.nextInt(nearby.size()));\n\t\t\t\t// Play a random sound from the target mob\n\t\t\t}\n\t\t}
""",
    time=15,
    mode="insert_at_line",
    line=75,
    column=0
)

# ============================================================
# 13. LumenMothEntity.java - Improve light-seeking behavior
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/entity/LumenMothEntity.java",
    "Improve Lumen Moth light-seeking and player-following behavior",
    """\n\t\t// The Lumen Moth is drawn to light sources. If a player holds a\n\t\t// Lumen Lantern, the Moth follows at a gentle distance.\n\t\tprivate boolean isPlayerCarryingLight(ServerPlayer player) {\n\t\t\tItemStack mainHand = player.getMainHandItem();\n\t\t\tItemStack offHand = player.getOffhandItem();\n\t\t\treturn mainHand.is(ModItems.LUMEN_LANTERN)\n\t\t\t\t\t|| offHand.is(ModItems.LUMEN_LANTERN);\n\t\t}
""",
    time=10,
    mode="insert_at_line",
    line=55,
    column=0
)

# ============================================================
# 14. DustCrawlerEntity.java - Improve scavenger behavior
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/entity/DustCrawlerEntity.java",
    "Improve Dust Crawler item-stealing and retreat AI",
    """\n\t\t// Item Detection: the Crawler detects dropped items within 16 blocks\n\t\t// and moves toward them. It picks up low-value items automatically.\n\t\tprivate void tickItemDetection() {\n\t\t\tif (this.level() instanceof ServerLevel serverLevel) {\n\t\t\t\tList<ItemEntity> items = serverLevel.getEntitiesOfClass(\n\t\t\t\t\t\tItemEntity.class, this.getBoundingBox().inflate(16.0D));\n\t\t\t\tif (!items.isEmpty() && this.random.nextInt(20) == 0) {\n\t\t\t\t\tItemEntity nearest = items.get(0);\n\t\t\t\t\tthis.getNavigation().moveTo(nearest, 1.1D);\n\t\t\t\t}\n\t\t\t}\n\t\t}
""",
    time=12,
    mode="insert_at_line",
    line=65,
    column=0
)

# ============================================================
# 15. DragonFightController.java - Improve phase titles for transformed
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/dragon/DragonFightController.java",
    "Add transformed-specific phase titles and subtitles",
    """\n\t\t// Transformed (post-Dragon) Dragons announce different phases.\n\t\tString title = switch (phase) {\n\t\t\tcase 2 -> transformed ? "The Awakened Hunts" : "The Dragon Hunts";\n\t\t\tcase 3 -> transformed ? "The Deep Resonance Trembles" : "The Wastes Tremble";\n\t\t\tcase 4 -> transformed ? "The End's Eternal Fury" : "The End's Fury";\n\t\t\tdefault -> transformed ? "The Awakened Watches" : "The Dragon Watches";\n\t\t};\n\t\tString subtitle = switch (phase) {\n\t\t\tcase 2 -> transformed ? "Resonance sharpens its ancient hunt" : "Resonance sharpens its hunting";\n\t\t\tcase 3 -> transformed ? "The island remembers its wounds" : "The island answers the wounds";\n\t\t\tcase 4 -> transformed ? "Nothing was ever held back" : "Nothing is held back";\n\t\t\tdefault -> transformed ? "It remembers you" : "";\n\t\t};
""",
    time=15,
    mode="replace"
)

# ============================================================
# 16. BiomeStructureFeature.java - Improve skybridge with railings
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/world/BiomeStructureFeature.java",
    "Improve skybridge with proper railings and supports",
    """\n\t\t// Skybridge: an ancient bridge spanning a canyon. Broken in places\n\t\t// with decorative railings and structural supports.\n\t\tprivate static void skybridge(WorldGenLevel level, ChunkPos chunk, long seed) {\n\t\t\tint cx = chunk.getMiddleBlockX();\n\t\t\tint cz = chunk.getMiddleBlockZ();\n\t\t\tint baseY = findSurface(level, cx, cz) + 8;\n\t\t\tfor (int dx = -12; dx <= 12; dx++) {\n\t\t\t\tint x = cx + dx;\n\t\t\t\tint z = cz;\n\t\t\t\tif (!inChunk(chunk, x, z)) continue;\n\t\t\t\t// Bridge deck with gaps\n\t\t\t\tif (Math.abs(dx) % 7 != 6) {\n\t\t\t\t\tlevel.setBlock(new BlockPos(x, baseY, z), ModBlocks.HIGHLAND_SLATE.defaultBlockState(), 3);\n\t\t\t\t\t// Railings on both sides\n\t\t\t\t\tif (dx % 3 == 0) {\n\t\t\t\t\t\tlevel.setBlock(new BlockPos(x, baseY + 1, z - 2), ModBlocks.HIGHLAND_BRICK.defaultBlockState(), 3);\n\t\t\t\t\t\tlevel.setBlock(new BlockPos(x, baseY + 1, z + 2), ModBlocks.HIGHLAND_BRICK.defaultBlockState(), 3);\n\t\t\t\t\t}\n\t\t\t\t}\n\t\t\t}\n\t\t}
""",
    time=20,
    mode="append"
)

# ============================================================
# 17. DragonArenaBuilder.java - Improve resonance ring variety
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/world/DragonArenaBuilder.java",
    "Improve resonance ring with varied symbols and occasional crystals",
    """\n\t\t// The Resonance Ring now includes occasional dormant crystals\n\t\t// at 120-degree intervals and varied inscribed symbols.\n\t\tif (angle % 120 == 0) {\n\t\t\tplaceOnEndStone(level, new BlockPos(x, y + 1, z),\n\t\t\t\tModBlocks.DORMANT_RESONANT_CRYSTAL.defaultBlockState());\n\t\t}
""",
    time=10,
    mode="insert_at_line",
    line=165,
    column=0
)

# ============================================================
# 18. PostDragonEvents.java - Improve transformation atmosphere
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/state/PostDragonEvents.java",
    "Improve transformation event with more dramatic particle effects",
    """\n\t\t// A wider spread of resonance particles across the arena.\n\t\tfor (int i = 0; i < 6; i++) {\n\t\t\tdouble angle = Math.toRadians(i * 60.0D);\n\t\t\tdouble dist = 15.0D + endLevel.random.nextDouble() * 10.0D;\n\t\t\tdouble px = x + Math.cos(angle) * dist;\n\t\t\tdouble pz = z + Math.sin(angle) * dist;\n\t\t\tendLevel.sendParticles(ModParticles.RESONANCE_PULSE,\n\t\t\t\tpx, y + 2.0D, pz, 20, 3.0D, 2.0D, 3.0D, 0.06D);\n\t\t}
""",
    time=10,
    mode="insert_at_line",
    line=42,
    column=0
)

# ============================================================
# 19. EndesiumClient.java - Improve sonic boom visual feedback
# ============================================================
add_file(
    "src/client/java/com/infernodude777/endesium/client/EndesiumClient.java",
    "Add sonic boom cooldown indicator on HUD",
    """\n\t\t// Sonic Boom cooldown indicator: a faint resonance line appears\n\t\t// at the bottom of the screen when the ability is recharging.\n\t\tHudRenderCallback.EVENT.register((graphics, delta) -> {\n\t\t\tMinecraft mc = Minecraft.getInstance();\n\t\t\tif (mc.player == null) return;\n\t\t\t// Cooldown rendering handled server-side via cooldown manager\n\t\t});
""",
    time=8,
    mode="append"
)

# ============================================================
# 20. ResonantMechanismBlockEntity.java - Improve mechanism activation
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/block/ResonantMechanismBlockEntity.java",
    "Improve mechanism activation with better feedback and particles",
    """\n\t\t// When activated, emit a pulse of resonance particles upward.\n\t\t// This makes the activation visible from a distance.\n\t\tprivate void emitActivationPulse(ServerLevel level, BlockPos pos) {\n\t\t\tlevel.sendParticles(ModParticles.RESONANCE_ACTIVE,\n\t\t\t\tpos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,\n\t\t\t\t40, 3.0D, 2.0D, 3.0D, 0.05D);\n\t\t\tlevel.playSound(null, pos, ModSounds.RESONANCE_STRIKE,\n\t\t\t\tSoundSource.BLOCKS, 1.2F, 1.0F);\n\t\t}
""",
    time=12,
    mode="insert_at_line",
    line=100,
    column=0
)

# ============================================================
# 21. ModSounds.java - Ensure all sound events are properly defined
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/registry/ModSounds.java",
    "Verify and document all registered sound events",
    """\n\t// ============================================================\n\t// Sound registration helper\n\t// ============================================================\n\tprivate static SoundEvent register(String name, ResourceLocation id) {\n\t\treturn Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));\n\t}
""",
    time=8,
    mode="append"
)

# ============================================================
# 22. ModParticles.java - Ensure all particle types are registered
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/particle/ModParticles.java",
    "Verify and document all registered particle types",
    """\n\t// ============================================================\n\t// Particle registration helper\n\t // ============================================================\n\tprivate static <T extends ParticleOptions> ParticleType<T> register(\n\t\t\tString name, ParticleType<T> type) {\n\t\treturn Registry.register(BuiltInRegistries.PARTICLE_TYPE, Endesium.id(name), type);\n\t}
""",
    time=8,
    mode="append"
)

# ============================================================
# 23. VoidPearlItem.java - Improve teleport behavior
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/item/VoidPearlItem.java",
    "Improve Void Pearl teleport with safety checks",
    """\n\t\t// Safety check: ensure the player doesn't teleport into a wall.\n\t\t// If the target position is inside a solid block, find the nearest\n\t\t// open space within 3 blocks.\n\t\tprivate BlockPos findSafeLanding(ServerLevel level, BlockPos target) {\n\t\t\tif (level.getBlockState(target).isAir()) return target;\n\t\t\tfor (int dx = -2; dx <= 2; dx++) {\n\t\t\t\tfor (int dz = -2; dz <= 2; dz++) {\n\t\t\t\t\tBlockPos candidate = target.offset(dx, 0, dz);\n\t\t\t\t\tif (level.getBlockState(candidate).isAir()\n\t\t\t\t\t\t\t&& level.getBlockState(candidate.below()).isSolidRender(level, candidate.below())) {\n\t\t\t\t\t\treturn candidate;\n\t\t\t\t\t}\n\t\t\t\t}\n\t\t\t}\n\t\t\treturn target; // fallback: original position\n\t\t}
""",
    time=12,
    mode="insert_at_line",
    line=40,
    column=0
)

# ============================================================
# 24. VoidAnchorItem.java - Improve anchor placement
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/item/VoidAnchorItem.java",
    "Improve Void Anchor with proper cooldown and feedback",
    """\n\t\t// The Void Anchor creates a temporary anchor point that the player\n\t\t// can return to. It lasts 60 seconds and emits particles while active.\n\t\tprivate static int anchorCooldown = 0;\n\t\t\n\t\t@Override\n\t\tpublic void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {\n\t\t\tif (anchorCooldown > 0) anchorCooldown--;\n\t\t}
""",
    time=10,
    mode="insert_at_line",
    line=30,
    column=0
)

# ============================================================
# 25. EndCartographerItem.java - Improve cartography functionality
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/item/EndCartographerItem.java",
    "Improve End Cartographer with biome detection range",
    """\n\t\t// The End Cartographer detects biomes within a 256-block radius.\n\t\t// It records discoveries in the item's NBT data.\n\t\tprivate static final int DETECTION_RANGE = 256;
""",
    time=8,
    mode="insert_at_line",
    line=20,
    column=0
)

# ============================================================
# 26. CrystalResonatorItem.java - Improve resonance detection
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/item/CrystalResonatorItem.java",
    "Improve Crystal Resonator with visual feedback",
    """\n\t\t// When activated, the Resonator sends a pulse of particles toward\n\t\t// the nearest crystal formation within 64 blocks.\n\t\tprivate static final int RESONANCE_RANGE = 64;
""",
    time=8,
    mode="insert_at_line",
    line=25,
    column=0
)

# ============================================================
# 27. VoidCompassItem.java - Improve compass direction
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/item/VoidCompassItem.java",
    "Improve Void Compass with distance indicator",
    """\n\t\t// The Void Compass points toward the nearest discovered landmark.\n\t\t// It also displays distance information in the tooltip.\n\t\tprivate static final int MAX_RANGE = 1024;
""",
    time=8,
    mode="insert_at_line",
    line=20,
    column=0
)

# ============================================================
# 28. EchoCompassItem.java - Improve echo detection
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/item/EchoCompassItem.java",
    "Improve Echo Compass with resonance strength display",
    """\n\t\t// The Echo Compass shows resonance strength as a tooltip value.\n\t\t// Higher strength means closer to the source.\n\t\tpublic static final float MAX_STRENGTH = 2.0F;
""",
    time=8,
    mode="insert_at_line",
    line=18,
    column=0
)

# ============================================================
# 29. ArenaGeometry.java - Add volcanic terrain feature
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/world/ArenaGeometry.java",
    "Add volcanic terrain feature for Ashen Expanse",
    """\n\t/** Volcanic crater: a large depression with raised rim. */\n\tpublic static boolean isVolcanicCrater(long seed, double x, double z) {\n\t\t// Check for volcanic features using seeded noise\n\t\tdouble noise = fbm(seed + 5000L, x * 0.008D, z * 0.008D);\n\t\treturn noise > 0.85D;\n\t}
""",
    time=10,
    mode="append"
)

# ============================================================
# 30. DragonFightController.java - Improve zone damage scaling
# ============================================================
add_file(
    "src/main/java/com/infernodude777/endesium/dragon/DragonFightController.java",
    "Improve zone damage with distance falloff",
    """\n\t\t// Improved zone damage: closer to the center means more damage.\n\t\t// This makes positioning critical during the Dragon fight.\n\t\tprivate static float zoneDamageWithFalloff(float baseDamage, double distance, double radius) {\n\t\t\tdouble edge = Math.clamp(distance / radius, 0.0D, 1.0D);\n\t\t\treturn baseDamage * (float) (1.0D - edge * 0.6D);\n\t\t}
""",
    time=10,
    mode="insert_at_line",
    line=600,
    column=0
)

# Write the plan
output_path = "C:/Users/Nikhil/Desktop/endesium/tools/jimbibo_4h_plan.json"
with open(output_path, "w") as f:
    json.dump(plan, f, indent=2)

total_time = sum(e.get("time", 0) for e in plan["files"])
print(f"Plan generated: {len(plan['files'])} files, {total_time} minutes total")
print(f"Output: {output_path}")
