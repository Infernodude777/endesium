package com.infernodude777.endesium.item;

import com.infernodude777.endesium.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Luminous / Ash / Null tool lines — all abilities are physical/particle/world
 * manipulation, no potion effects. Each tool has a distinct right-click and
 * on-hit behavior.
 */
public final class EndgearTools {
	private EndgearTools() {
	}

	private static final Map<UUID, Integer> ASH_STANCE = new HashMap<>();

	private static boolean isAshStanceActive(Player player, Level level) {
		Integer exp = ASH_STANCE.get(player.getUUID());
		return exp != null && level.getGameTime() < exp;
	}

	// =====================================================================
	// LUMINOUS — prism / light geometry
	// =====================================================================

	public static class LuminousSword extends SwordItem {
		public LuminousSword(Tier tier, Properties properties) { super(tier, properties); }
		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			if (attacker.level() instanceof ServerLevel sl) {
				sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.3, 0.5, 0.3, 0.06);
				sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.4, 0.4, 0.4, 0.2);
			}
			// light shard burst: extra 2 damage to 2 nearby foes
			if (attacker instanceof Player p && attacker.level() instanceof ServerLevel sl2) {
				for (LivingEntity e : sl2.getEntitiesOfClass(LivingEntity.class, attacker.getBoundingBox().inflate(4), en -> en != attacker && en != target && en.isAlive())) {
					if (e.distanceTo(target) < 3.5) {
						e.hurt(attacker.damageSources().playerAttack((Player) attacker), 2.0F);
						sl2.sendParticles(ParticleTypes.FLASH, e.getX(), e.getY() + 0.8, e.getZ(), 1, 0, 0, 0, 0);
					}
				}
			}
			return super.hurtEnemy(stack, target, attacker);
		}
		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel sl) {
				Vec3 start = player.getEyePosition();
				Vec3 dir = player.getLookAngle();
				int pierced = 0;
				for (int i = 1; i <= 20; i++) {
					Vec3 pos = start.add(dir.scale(i));
					sl.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 2, 0.06, 0.06, 0.06, 0.01);
					if (i == 10) sl.sendParticles(ParticleTypes.FLASH, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
					AABB box = new AABB(pos.x - 0.6, pos.y - 0.6, pos.z - 0.6, pos.x + 0.6, pos.y + 0.6, pos.z + 0.6);
					for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, box, en -> en != player && en.isAlive() && !(en instanceof Player))) {
						float dmg = 6.0F - pierced * 1.0F;
						if (dmg < 2.0F) dmg = 2.0F;
						e.hurt(player.damageSources().playerAttack(player), dmg);
						e.setDeltaMovement(e.getDeltaMovement().add(dir.x * 0.25, 0.15, dir.z * 0.25));
						e.hurtMarked = true;
						pierced++;
						if (pierced >= 4) break;
					}
					if (pierced >= 4) break;
				}
				sl.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.6F);
				player.getCooldowns().addCooldown(this, 60);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		@Override
		public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag f) {
			tip.add(Component.literal("Prism Beam: right-click fires 20b piercing light (6 dmg)").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Light Shards: hit splashes 2 dmg to nearby foes").withStyle(ChatFormatting.GRAY));
		}
	}

	public static class LuminousPickaxe extends PickaxeItem {
		public LuminousPickaxe(Tier tier, Properties properties) { super(tier, properties); }
		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			if (attacker.level() instanceof ServerLevel sl) {
				sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 0.8, target.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
				// armor-piercing tap
				target.hurt(attacker.damageSources().playerAttack((Player) attacker), 1.0F);
			}
			return super.hurtEnemy(stack, target, attacker);
		}
		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel sl) {
				BlockPos at = BlockPos.containing(player.position()).above();
				if (level.getBlockState(at).isAir()) {
					level.setBlock(at, ModBlocks.VOID_LAMP.defaultBlockState(), 3);
					sl.sendParticles(ParticleTypes.FIREWORK, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 12, 0.4, 0.4, 0.4, 0.08);
					sl.playSound(null, at, SoundEvents.LANTERN_PLACE, SoundSource.PLAYERS, 1.0F, 1.2F);
				}
				// reveal pulse
				sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2, player.getZ(), 18, 3.0, 1.0, 3.0, 0.04);
				sl.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 1.2F);
				player.getCooldowns().addCooldown(this, 30);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		@Override
		public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag f) {
			tip.add(Component.literal("Lamp Placer: right-click places Void Lamp + light pulse").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Piercing Tap: hit adds 1 true damage + crit burst").withStyle(ChatFormatting.GRAY));
		}
	}

	public static class LuminousAxe extends AxeItem {
		public LuminousAxe(Tier tier, Properties properties) { super(tier, properties); }
		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			if (attacker.level() instanceof ServerLevel sl) {
				Vec3 dir = attacker.getLookAngle();
				AABB arc = attacker.getBoundingBox().inflate(3.2);
				int hit = 0;
				for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, arc, en -> en != attacker && en.isAlive())) {
					if (e == target) continue;
					Vec3 to = e.position().subtract(attacker.position()).normalize();
					if (to.dot(dir) > 0.35 && e.distanceTo(attacker) < 3.5) {
						e.hurt(attacker.damageSources().playerAttack((Player) attacker), 4.0F);
						e.setDeltaMovement(e.getDeltaMovement().add(to.x * 0.5, 0.2, to.z * 0.5));
						e.hurtMarked = true;
						sl.sendParticles(ParticleTypes.SWEEP_ATTACK, e.getX(), e.getY() + 1.0, e.getZ(), 1, 0, 0, 0, 0);
						if (++hit >= 2) break;
					}
				}
				sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 14, 0.4, 0.5, 0.4, 0.25);
				sl.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.9F, 1.1F);
			}
			return super.hurtEnemy(stack, target, attacker);
		}
		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel sl) {
				Vec3 dir = player.getLookAngle().multiply(1, 0, 1).normalize();
				Vec3 start = player.position();
				player.setDeltaMovement(dir.x * 1.35, 0.18, dir.z * 1.35);
				player.hurtMarked = true;
				player.resetFallDistance();
				for (int i = 0; i < 8; i++) {
					Vec3 p = start.add(dir.scale(i * 1.0));
					sl.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 0.8, p.z, 3, 0.12, 0.12, 0.12, 0.01);
					for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, new AABB(p.x - 1, p.y - 0.6, p.z - 1, p.x + 1, p.y + 1.2, p.z + 1), en -> en != player && en.isAlive())) {
						e.hurt(player.damageSources().playerAttack(player), 3.0F);
						e.setDeltaMovement(e.getDeltaMovement().add(dir.x * 0.6, 0.25, dir.z * 0.6));
						e.hurtMarked = true;
					}
				}
				sl.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.25F);
				player.getCooldowns().addCooldown(this, 80);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		@Override
		public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag f) {
			tip.add(Component.literal("Prism Cleave: hit arcs to 2 foes in front (4 dmg)").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Lumen Dash: right-click dashes 8b, trail hits 3 dmg").withStyle(ChatFormatting.GRAY));
		}
	}

	public static class LuminousShovel extends ShovelItem {
		public LuminousShovel(Tier tier, Properties properties) { super(tier, properties); }
		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			if (attacker.level() instanceof ServerLevel sl) {
				Vec3 kb = target.position().subtract(attacker.position()).normalize();
				target.setDeltaMovement(kb.x * 0.9, 0.45, kb.z * 0.9);
				target.hurtMarked = true;
				sl.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 0.6, target.getZ(), 2, 0.2, 0.2, 0.2, 0);
				sl.playSound(null, target.blockPosition(), SoundEvents.SHOVEL_FLATTEN, SoundSource.PLAYERS, 1.0F, 1.0F);
			}
			return super.hurtEnemy(stack, target, attacker);
		}
		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel sl) {
				BlockPos base = BlockPos.containing(player.position()).below();
				int converted = 0;
				for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
					BlockPos p = base.offset(dx, 0, dz);
					BlockState s = level.getBlockState(p);
					if (s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.ROOTED_DIRT)) {
						level.setBlock(p, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
						if (++converted >= 12) break;
					}
					if (s.is(Blocks.DIRT_PATH) || s.is(Blocks.GRASS_BLOCK)) {
						// already pathed
					}
				}
				// shockwave
				for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5), en -> en != player && en.isAlive())) {
					Vec3 push = e.position().subtract(player.position()).normalize().scale(0.85);
					e.setDeltaMovement(e.getDeltaMovement().add(push.x, 0.35, push.z));
					e.hurtMarked = true;
				}
				sl.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.2, player.getZ(), 14, 2.2, 0.2, 2.2, 0.08);
				sl.playSound(null, player.blockPosition(), SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 0.85F);
				player.getCooldowns().addCooldown(this, 40);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		@Override
		public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag f) {
			tip.add(Component.literal("Launch: hit knocks target skyward").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Path Pulse: right-click 5-block shockwave + till 5×5").withStyle(ChatFormatting.GRAY));
		}
	}

	public static class LuminousHoe extends HoeItem {
		public LuminousHoe(Tier tier, Properties properties) { super(tier, properties); }
		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			if (attacker.level() instanceof ServerLevel sl) {
				// reap 3x3 crops around target
				BlockPos c = BlockPos.containing(target.position());
				for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) for (int dy = -1; dy <= 1; dy++) {
					BlockPos p = c.offset(dx, dy, dz);
					BlockState s = levelOf(attacker).getBlockState(p);
					if (s.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop && crop.isMaxAge(s)) {
						levelOf(attacker).destroyBlock(p, true, attacker);
						sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX() + 0.5, p.getY() + 0.6, p.getZ() + 0.5, 4, 0.25, 0.25, 0.25, 0.2);
					}
				}
			}
			return super.hurtEnemy(stack, target, attacker);
		}
		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel sl) {
				BlockPos base = BlockPos.containing(player.position()).below();
				int tilled = 0, grown = 0;
				for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
					BlockPos p = base.offset(dx, 0, dz);
					BlockState s = level.getBlockState(p);
					if ((s.is(Blocks.DIRT) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.COARSE_DIRT)) && level.getBlockState(p.above()).isAir()) {
						level.setBlock(p, Blocks.FARMLAND.defaultBlockState(), 3);
						tilled++;
					}
					BlockPos above = p.above();
					BlockState crop = level.getBlockState(above);
					if (crop.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock growable && growable.isValidBonemealTarget(level, p.above(), crop)) {
						for (int i = 0; i < 6; i++) crop.randomTick(sl, above, sl.random);
						grown++;
						sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, above.getX() + 0.5, above.getY() + 0.7, above.getZ() + 0.5, 2, 0.22, 0.22, 0.22, 0.2);
					}
					if (tilled + grown > 28) break;
				}
				sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 16, 2.2, 0.8, 2.2, 0.02);
				sl.playSound(null, player.blockPosition(), SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1.0F, 1.1F);
				player.getCooldowns().addCooldown(this, 50);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		private static Level levelOf(LivingEntity e) { return e.level(); }
		@Override
		public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag f) {
			tip.add(Component.literal("Tiller: right-click tills 7×7 + grows crops").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Reap: hit harvests 3×3 ripe crops").withStyle(ChatFormatting.GRAY));
		}
	}

	// =====================================================================
	// ASH - firebending
	// =====================================================================

	public static class AshSword extends SwordItem {
		public AshSword(Tier tier, Properties properties) { super(tier, properties); }
		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			target.igniteForSeconds(3);
			if (attacker instanceof Player p && isAshStanceActive(p, attacker.level())) {
				// extra fireball on stance hit
				Vec3 look = p.getLookAngle();
				Vec3 pos = p.getEyePosition().add(look.scale(0.6));
				if (attacker.level() instanceof ServerLevel sl) {
					sl.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 8, 0.18, 0.18, 0.18, 0.04);
					sl.sendParticles(ParticleTypes.SMALL_FLAME, target.getX(), target.getY() + 0.9, target.getZ(), 6, 0.2, 0.4, 0.2, 0.02);
				}
				// small extra true damage burst
				target.hurt(attacker.damageSources().playerAttack(p), 2.0F);
				target.igniteForSeconds(5);
			}
			if (attacker.level() instanceof ServerLevel sl2) {
				sl2.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 0.9, target.getZ(), 6, 0.25, 0.35, 0.25, 0.04);
			}
			return super.hurtEnemy(stack, target, attacker);
		}
		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel sl) {
				if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(player.getItemInHand(hand));
				int now = (int) level.getGameTime();
				Integer exp = ASH_STANCE.get(player.getUUID());
				if (exp != null && now < exp) {
					return InteractionResultHolder.fail(player.getItemInHand(hand));
				}
				ASH_STANCE.put(player.getUUID(), now + 60); // 3s toggle
				sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.2, player.getZ(), 22, 0.5, 0.6, 0.5, 0.08);
				sl.sendParticles(ParticleTypes.LAVA, player.getX(), player.getY() + 1.2, player.getZ(), 6, 0.4, 0.4, 0.4, 0.02);
				sl.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 0.85F);
				sl.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.7F, 1.2F);
				player.getCooldowns().addCooldown(this, 72);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		@Override
		public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag f) {
			tip.add(Component.literal("Firebending Toggle: right-click 3s stance — swings shoot flame").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Ignite: hit burns 3s (5s in stance + 2 bonus dmg)").withStyle(ChatFormatting.GRAY));
		}
	}

	public static class AshPickaxe extends PickaxeItem {
		public AshPickaxe(Tier tier, Properties properties) { super(tier, properties); }
		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			target.igniteForSeconds(2);
			Vec3 kb = target.position().subtract(attacker.position()).normalize();
			target.setDeltaMovement(target.getDeltaMovement().add(kb.x * 0.35, 0.18, kb.z * 0.35));
			target.hurtMarked = true;
			if (attacker.level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.SMALL_FLAME, target.getX(), target.getY() + 0.9, target.getZ(), 5, 0.2, 0.3, 0.2, 0.02);
			return super.hurtEnemy(stack, target, attacker);
		}
		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel sl) {
				BlockPos target = BlockPos.containing(player.getEyePosition().add(player.getLookAngle().scale(4.5)));
				// if looking at air, use block below feet
				if (level.getBlockState(target).isAir()) target = BlockPos.containing(player.position()).below();
				int smelted = 0;
				for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
					BlockPos p = target.offset(dx, dy, dz);
					BlockState s = level.getBlockState(p);
					Block b = s.getBlock();
					Block out = null;
					if (b == Blocks.STONE) out = Blocks.SMOOTH_STONE;
					else if (b == Blocks.COBBLESTONE) out = Blocks.STONE;
					else if (b == Blocks.DEEPSLATE) out = Blocks.COBBLED_DEEPSLATE;
					else if (b == Blocks.NETHERRACK) out = Blocks.MAGMA_BLOCK;
					else if (s.is(Blocks.IRON_ORE) || s.is(Blocks.DEEPSLATE_IRON_ORE)) { level.destroyBlock(p, false); Block.popResource(level, p, new ItemStack(net.minecraft.world.item.Items.RAW_IRON, 1)); smelted++; sl.sendParticles(ParticleTypes.LAVA, p.getX()+0.5, p.getY()+0.7, p.getZ()+0.5, 4, 0.2,0.2,0.2,0.02); continue; }
					else if (s.is(Blocks.GOLD_ORE) || s.is(Blocks.DEEPSLATE_GOLD_ORE)) { level.destroyBlock(p, false); Block.popResource(level, p, new ItemStack(net.minecraft.world.item.Items.RAW_GOLD, 1)); smelted++; continue; }
					else if (s.is(Blocks.COPPER_ORE) || s.is(Blocks.DEEPSLATE_COPPER_ORE)) { level.destroyBlock(p, false); Block.popResource(level, p, new ItemStack(net.minecraft.world.item.Items.RAW_COPPER, 2)); smelted++; continue; }
					if (out != null) { level.setBlock(p, out.defaultBlockState(), 3); sl.sendParticles(ParticleTypes.FLAME, p.getX()+0.5, p.getY()+0.8, p.getZ()+0.5, 3, 0.18,0.18,0.18,0.02); smelted++; }
				}
				sl.sendParticles(ParticleTypes.LAVA, target.getX()+0.5, target.getY()+0.8, target.getZ()+0.5, 10, 0.7,0.45,0.7,0.02);
				sl.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY()+1.0, player.getZ(), 10, 0.4,0.4,0.4,0.04);
				sl.playSound(null, target, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.4F);
				sl.playSound(null, player.blockPosition(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
				player.getCooldowns().addCooldown(this, 50);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		@Override
		public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag f) {
			tip.add(Component.literal("Magma Quench: right-click superheats 3×3×3 — smelts ores, bakes stone").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Sear: hit ignites 2s + small knock").withStyle(ChatFormatting.GRAY));
		}
	}

	public static class AshAxe extends AxeItem {
		public AshAxe(Tier tier, Properties properties) { super(tier, properties); }
		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			if (attacker.level() instanceof ServerLevel sl) {
				Vec3 dir = attacker.getLookAngle().multiply(1,0,1).normalize();
				// crescent arc
				for (int step = 2; step <= 10; step++) {
					double angle = (step - 6) * 0.22;
					Vec3 off = new Vec3(Math.cos(angle) * 0.9, 0, Math.sin(angle) * 0.9);
					// rotate off by yaw
					double yaw = Math.atan2(dir.z, dir.x);
					Vec3 r = new Vec3(off.x * Math.cos(yaw) - off.z * Math.sin(yaw), 0, off.x * Math.sin(yaw) + off.z * Math.cos(yaw));
					Vec3 p = attacker.position().add(dir.scale(step * 0.9)).add(r).add(0, 1.0, 0);
					sl.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 2, 0.06,0.06,0.06,0.01);
				}
				AABB sweep = attacker.getBoundingBox().inflate(6);
				int hit = 0;
				for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, sweep, en -> en != attacker && en.isAlive())) {
					if (e == target) continue;
					Vec3 to = e.position().subtract(attacker.position()).normalize();
					if (to.dot(dir) > 0.55 && to.lengthSqr() < 36 && e.distanceTo(attacker) > 1.8) {
						e.igniteForSeconds(3);
						e.hurt(attacker.damageSources().playerAttack((Player) attacker), 4.0F);
						e.setDeltaMovement(e.getDeltaMovement().add(to.x * 0.45, 0.12, to.z * 0.45));
						e.hurtMarked = true;
						if (++hit >= 3) break;
					}
				}
				target.igniteForSeconds(4);
				sl.playSound(null, target.blockPosition(), SoundEvents.BLAZE_HURT, SoundSource.PLAYERS, 0.9F, 0.9F);
			}
			return super.hurtEnemy(stack, target, attacker);
		}
		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel sl) {
				Vec3 center = player.position();
				for (int r = 2; r <= 6; r++) {
					double y = player.getY() + 0.25;
					for (int a = 0; a < 18; a++) {
						double ang = a * 20 * Math.PI / 180;
						sl.sendParticles(ParticleTypes.FLAME, center.x + Math.cos(ang)*r, y, center.z + Math.sin(ang)*r, 1, 0,0,0,0.02);
					}
				}
				for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(6), en -> en != player && en.isAlive())) {
					double d = e.distanceTo(player);
					if (d < 6.2 && d > 1.2) {
						e.igniteForSeconds(5);
						Vec3 kb = e.position().subtract(center).normalize();
						e.setDeltaMovement(e.getDeltaMovement().add(kb.x * 0.85, 0.38, kb.z * 0.85));
						e.hurtMarked = true;
						e.hurt(player.damageSources().playerAttack(player), 5.0F);
					}
				}
				sl.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9F, 1.2F);
				sl.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 0.7F);
				player.getCooldowns().addCooldown(this, 90);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		@Override
		public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag f) {
			tip.add(Component.literal("Crescent: hit throws 10b flame arc piercing 3 (4 dmg + burn)").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Ring Slam: right-click 6b fire ring + knock (5 dmg)").withStyle(ChatFormatting.GRAY));
		}
	}

	public static class AshShovel extends ShovelItem {
		public AshShovel(Tier tier, Properties properties) { super(tier, properties); }
		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			target.igniteForSeconds(2);
			target.setDeltaMovement(target.getDeltaMovement().add(0, 0.62, 0));
			target.hurtMarked = true;
			if (attacker.level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY()+0.6, target.getZ(), 7, 0.2,0.2,0.2,0.04);
			return super.hurtEnemy(stack, target, attacker);
		}
		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel sl) {
				Vec3 dir = player.getLookAngle().multiply(1,0,1).normalize();
				BlockPos origin = BlockPos.containing(player.position().add(dir.scale(2)).add(0, -1, 0));
				for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) for (int dy = 0; dy <= 1; dy++) {
					BlockPos p = origin.offset(dx, dy, dz);
					BlockState s = level.getBlockState(p);
					if (!s.isAir() && s.getDestroySpeed(level, p) >= 0 && s.getDestroySpeed(level, p) < 50) {
						FallingBlockEntity fb = FallingBlockEntity.fall(level, p, s);
						fb.setDeltaMovement(dir.x * 0.55 + (sl.random.nextDouble()-0.5)*0.2, 0.42 + sl.random.nextDouble()*0.2, dir.z * 0.55 + (sl.random.nextDouble()-0.5)*0.2);
						fb.time = 1;
						level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
						sl.sendParticles(ParticleTypes.FLAME, p.getX()+0.5, p.getY()+0.7, p.getZ()+0.5, 5, 0.2,0.2,0.2,0.03);
					}
				}
				sl.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8F, 0.9F);
				player.getCooldowns().addCooldown(this, 70);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		@Override
		public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag f) {
			tip.add(Component.literal("Pop: hit launches foe skyward + burn").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Earth Fling: right-click hurls 3×3 blocks as flaming projectiles").withStyle(ChatFormatting.GRAY));
		}
	}

	public static class AshHoe extends HoeItem {
		public AshHoe(Tier tier, Properties properties) { super(tier, properties); }
		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			if (attacker.level() instanceof ServerLevel sl) {
				BlockPos c = BlockPos.containing(target.position()).below();
				for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
					BlockPos p = c.offset(dx, 0, dz);
					if (levelOf(attacker).getBlockState(p).is(Blocks.DIRT) || levelOf(attacker).getBlockState(p).is(Blocks.GRASS_BLOCK)) {
						levelOf(attacker).setBlock(p, Blocks.FARMLAND.defaultBlockState(), 3);
						sl.sendParticles(ParticleTypes.FLAME, p.getX()+0.5, p.getY()+1.0, p.getZ()+0.5, 3, 0.18,0.1,0.18,0.02);
					}
				}
				target.igniteForSeconds(2);
			}
			return super.hurtEnemy(stack, target, attacker);
		}
		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel sl) {
				BlockPos base = BlockPos.containing(player.position()).below();
				int tilled = 0;
				for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
					BlockPos p = base.offset(dx, 0, dz);
					BlockState s = level.getBlockState(p);
					if ((s.is(Blocks.DIRT) || s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.DIRT_PATH)) && level.getBlockState(p.above()).isAir()) {
						level.setBlock(p, Blocks.FARMLAND.defaultBlockState(), 3);
						tilled++;
					}
					// scorch edge ring
					if ((Math.abs(dx)==2 || Math.abs(dz)==2) && sl.random.nextFloat() < 0.35) {
						BlockPos above = p.above();
						if (level.getBlockState(above).isAir()) sl.sendParticles(ParticleTypes.FLAME, above.getX()+0.5, above.getY()+0.2, above.getZ()+0.5, 2, 0.12,0.06,0.12,0.02);
					}
				}
				sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY()+0.8, player.getZ(), 16, 1.2,0.3,1.2,0.04);
				sl.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.9F, 1.1F);
				player.getCooldowns().addCooldown(this, 45);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		private static Level levelOf(LivingEntity e){ return e.level(); }
		@Override
		public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag f) {
			tip.add(Component.literal("Scorch Till: right-click tills 5×5 with flame edge").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Sear Furrow: hit tills 3×3 + burns foe 2s").withStyle(ChatFormatting.GRAY));
		}
	}

	// =====================================================================
	// NULL - deletion / gravity
	// =====================================================================

	public static class NullSword extends SwordItem {
		public NullSword(Tier tier, Properties properties) { super(tier, properties); }
		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			// void rip: shred 4 armor durability points
			for (var slot : new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
				ItemStack worn = target.getItemBySlot(slot);
				if (!worn.isEmpty() && worn.isDamageableItem()) {
					worn.setDamageValue(worn.getDamageValue() + 4);
					if (worn.getDamageValue() >= worn.getMaxDamage()) worn.shrink(1);
					break;
				}
			}
			if (attacker.level() instanceof ServerLevel sl) {
				sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY()+0.9, target.getZ(), 14, 0.3,0.5,0.3,0.18);
				sl.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY()+0.9, target.getZ(), 6, 0.2,0.3,0.2,0.04);
				sl.playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, 0.5F);
			}
			return super.hurtEnemy(stack, target, attacker);
		}
		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel sl) {
				Vec3 center = player.position().add(0, 1.0, 0);
				for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(7), en -> en != player && en.isAlive())) {
					Vec3 pull = center.subtract(e.position().add(0, e.getBbHeight()/2, 0)).normalize().scale(0.68);
					e.setDeltaMovement(e.getDeltaMovement().add(pull.x, pull.y * 0.55 + 0.06, pull.z));
					e.hurtMarked = true;
					// strip 1 random non-empty inventory stack visual
					sl.sendParticles(ParticleTypes.PORTAL, e.getX(), e.getY()+0.8, e.getZ(), 3, 0.2,0.3,0.2,0.12);
				}
				sl.sendParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y, center.z, 42, 2.2, 1.0, 2.2, 0.10);
				sl.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y, center.z, 28, 1.8,0.8,1.8,0.06);
				sl.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.45F);
				// implosion after pull
				sl.getServer().execute(() -> {}); // placeholder for delayed — immediate burst for simplicity
				for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5.5), en -> en != player && en.isAlive() && en.distanceTo(player) < 5.5)) {
					float dmg = (float)(5.5 - e.distanceTo(player));
					if (dmg < 1) dmg = 1;
					e.hurt(player.damageSources().magic(), dmg);
					// delete their projectiles
					for (Entity proj : sl.getEntitiesOfClass(Entity.class, e.getBoundingBox().inflate(2), en -> en.getType().toString().contains("Arrow") || en.getType().toString().contains("Trident") || en.getType().toString().contains("Fireball"))) proj.discard();
				}
				sl.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 3, 0.4,0.4,0.4,0);
				player.getCooldowns().addCooldown(this, 100);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		@Override
		public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag f) {
			tip.add(Component.literal("Void Rip: hit shreds 4 armor durability + portal burst").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Collapse: right-click pulls 7b then implodes (5 dmg falloff)").withStyle(ChatFormatting.GRAY));
		}
	}

	public static class NullPickaxe extends PickaxeItem {
		public NullPickaxe(Tier tier, Properties properties) { super(tier, properties); }
		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			Vec3 kb = target.position().subtract(attacker.position()).normalize();
			target.setDeltaMovement(target.getDeltaMovement().add(kb.x * 0.18, 0.12, kb.z * 0.18));
			target.hurtMarked = true;
			if (attacker.level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY()+0.8, target.getZ(), 6, 0.2,0.3,0.2,0.1);
			return super.hurtEnemy(stack, target, attacker);
		}
		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel sl) {
				Vec3 look = player.getLookAngle();
				// ray 12b to find solid top
				Vec3 dest = null;
				for (int i = 12; i >= 1; i--) {
					Vec3 p = player.position().add(look.x * i, look.y * i * 0.25 + 0.5, look.z * i);
					BlockPos bp = BlockPos.containing(p);
					BlockPos below = bp.below();
					if (!level.getBlockState(bp).isSuffocating(level, bp) && level.getBlockState(below).isSolidRender(level, below) && level.getBlockState(bp).isAir() && level.getBlockState(bp.above()).isAir()) { dest = new Vec3(bp.getX()+0.5, bp.getY(), bp.getZ()+0.5); break; }
				}
				if (dest == null) dest = player.position().add(look.x * 8, 0.5, look.z * 8);
				sl.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY()+1.0, player.getZ(), 18, 0.35,0.7,0.35, 0.10);
				player.teleportTo(dest.x, dest.y, dest.z);
				player.resetFallDistance();
				sl.sendParticles(ParticleTypes.REVERSE_PORTAL, dest.x, dest.y+1.0, dest.z, 20, 0.35,0.7,0.35, 0.10);
				sl.sendParticles(ParticleTypes.PORTAL, dest.x, dest.y+1.0, dest.z, 10, 0.2,0.4,0.2, 0.10);
				sl.playSound(null, BlockPos.containing(dest), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
				sl.playSound(null, player.blockPosition(), SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 0.5F, 1.6F);
				player.getCooldowns().addCooldown(this, 60);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		@Override
		public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag f) {
			tip.add(Component.literal("Void Step: right-click aimed 12b teleport to solid top").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Nudge: hit small portal knock").withStyle(ChatFormatting.GRAY));
		}
	}

	public static class NullAxe extends AxeItem {
		public NullAxe(Tier tier, Properties properties) { super(tier, properties); }
		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			// void cleave affinity: also shreds
			for (var slot : new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
				ItemStack worn = target.getItemBySlot(slot);
				if (!worn.isEmpty() && worn.isDamageableItem()) { worn.setDamageValue(worn.getDamageValue()+3); break; }
			}
			target.setDeltaMovement(target.getDeltaMovement().add(0, 0.28, 0));
			target.hurtMarked = true;
			if (attacker.level() instanceof ServerLevel sl) { sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY()+0.9, target.getZ(), 10, 0.25,0.4,0.25,0.14); sl.playSound(null, target.blockPosition(), SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 0.7F, 0.6F); }
			return super.hurtEnemy(stack, target, attacker);
		}
		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel sl) {
				Vec3 dir = player.getLookAngle().multiply(1,0,1).normalize();
				Vec3 dest = player.position().add(dir.scale(6));
				// try to phase through wall up to 4b
				BlockPos cur = BlockPos.containing(player.position());
				for (int i = 1; i <= 4; i++) {
					BlockPos probe = BlockPos.containing(player.position().add(dir.scale(i)));
					if (level.getBlockState(probe).isAir()) { dest = new Vec3(probe.getX()+0.5, probe.getY(), probe.getZ()+0.5); break; }
				}
				sl.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY()+1.0, player.getZ(), 16, 0.3,0.6,0.3,0.12);
				player.teleportTo(dest.x, dest.y, dest.z);
				player.resetFallDistance();
				sl.sendParticles(ParticleTypes.REVERSE_PORTAL, dest.x, dest.y+1.0, dest.z, 16, 0.3,0.6,0.3,0.12);
				sl.playSound(null, BlockPos.containing(dest), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9F, 0.7F);
				player.getCooldowns().addCooldown(this, 70);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		@Override
		public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag f) {
			tip.add(Component.literal("Cleave: hit shreds 3 armor durability + lift").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Phase Cut: right-click steps through wall 4b").withStyle(ChatFormatting.GRAY));
		}
	}

	public static class NullShovel extends ShovelItem {
		public NullShovel(Tier tier, Properties properties) { super(tier, properties); }
		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			if (attacker.level() instanceof ServerLevel sl) {
				// phase target: squid ink + push-through
				sl.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY()+0.9, target.getZ(), 10, 0.22,0.35,0.22,0.05);
				sl.sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY()+0.9, target.getZ(), 8, 0.2,0.3,0.2,0.12);
				// brief no-collision slip: push through attacker
				Vec3 slip = target.position().subtract(attacker.position()).normalize().scale(0.85);
				target.setDeltaMovement(slip.x, 0.18, slip.z);
				target.hurtMarked = true;
				sl.playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6F, 0.7F);
			}
			return super.hurtEnemy(stack, target, attacker);
		}
		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel sl) {
				Vec3 dir = player.getLookAngle().multiply(1,0,1).normalize();
				BlockPos origin = BlockPos.containing(player.position().add(dir.scale(3)).add(0, -1, 0));
				// fold 5x5 sink 1b
				for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
					BlockPos p = origin.offset(dx, 0, dz);
					BlockState s = level.getBlockState(p);
					BlockPos below = p.below();
					BlockState belowS = level.getBlockState(below);
					if (!s.isAir() && !s.is(Blocks.BEDROCK) && belowS.isAir()) continue; // keep
					if (s.isAir() || s.getDestroySpeed(level, p) < 0) continue;
					// sink visual: move down 1 if air below
					if (level.getBlockState(p.below()).isAir()) {
						level.setBlock(p.below(), s, 3);
						level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
						sl.sendParticles(ParticleTypes.PORTAL, p.getX()+0.5, p.getY()+0.6, p.getZ()+0.5, 3, 0.12,0.12,0.12,0.04);
					}
				}
				// levitate then drop entities in fold
				for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, new AABB(origin).inflate(3.5, 2, 3.5), en -> en != player && en.isAlive())) {
					e.setDeltaMovement(e.getDeltaMovement().add(0, 0.95, 0));
					e.hurtMarked = true;
					sl.sendParticles(ParticleTypes.REVERSE_PORTAL, e.getX(), e.getY()+0.7, e.getZ(), 6, 0.2,0.4,0.2,0.08);
				}
				// unfold after 80t
				sl.getServer().tell(new net.minecraft.server.TickTask(sl.getServer().getTickCount()+80, () -> {
					for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
						BlockPos low = origin.offset(dx, -1, dz);
						BlockPos high = origin.offset(dx, 0, dz);
						BlockState lowS = sl.getBlockState(low);
						if (!lowS.isAir() && sl.getBlockState(high).isAir() && !lowS.is(Blocks.BEDROCK)) {
							sl.setBlock(high, lowS, 3);
							sl.setBlock(low, Blocks.AIR.defaultBlockState(), 3);
							sl.sendParticles(ParticleTypes.REVERSE_PORTAL, high.getX()+0.5, high.getY()+0.5, high.getZ()+0.5, 2, 0.12,0.12,0.12,0.04);
						}
					}
				}));
				sl.sendParticles(ParticleTypes.PORTAL, origin.getX()+0.5, origin.getY()+0.8, origin.getZ()+0.5, 26, 1.6,0.4,1.6,0.08);
				sl.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.7F, 0.6F);
				player.getCooldowns().addCooldown(this, 90);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		@Override
		public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag f) {
			tip.add(Component.literal("Phase Slip: hit makes foe slip through you (portal)").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Fold: right-click sinks 5×5 ground 1b, lifts foes, unfolds 4s").withStyle(ChatFormatting.GRAY));
		}
	}

	public static class NullHoe extends HoeItem {
		public NullHoe(Tier tier, Properties properties) { super(tier, properties); }
		@Override
		public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
			if (attacker.level() instanceof ServerLevel sl) {
				// reap: strip 1 armor piece durability heavier
				for (var slot : new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST, net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
					ItemStack worn = target.getItemBySlot(slot);
					if (!worn.isEmpty() && worn.isDamageableItem()) { worn.setDamageValue(worn.getDamageValue()+5); break; }
				}
				sl.sendParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY()+0.9, target.getZ(), 7, 0.2,0.3,0.2,0.05);
			}
			return super.hurtEnemy(stack, target, attacker);
		}
		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
			if (level instanceof ServerLevel sl) {
				BlockPos base = BlockPos.containing(player.position()).below();
				int harvested = 0;
				for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
					for (int dy = -1; dy <= 1; dy++) {
						BlockPos p = base.offset(dx, dy, dz);
						BlockState s = level.getBlockState(p);
						Block b = s.getBlock();
						boolean isHarvestable = b instanceof net.minecraft.world.level.block.CropBlock
								|| s.is(Blocks.POTATOES) || s.is(Blocks.CARROTS) || s.is(Blocks.WHEAT) || s.is(Blocks.BEETROOTS)
								|| s.is(Blocks.NETHER_WART) || s.is(Blocks.COCOA)
								|| s.is(Blocks.SUGAR_CANE) || s.is(Blocks.CACTUS) || s.is(Blocks.KELP) || s.is(Blocks.BAMBOO)
								|| s.is(Blocks.MELON) || s.is(Blocks.PUMPKIN) || s.is(Blocks.SWEET_BERRY_BUSH)
								|| b instanceof net.minecraft.world.level.block.LeavesBlock;
						if (isHarvestable) {
							level.destroyBlock(p, true, player);
							harvested++;
							sl.sendParticles(ParticleTypes.PORTAL, p.getX()+0.5, p.getY()+0.6, p.getZ()+0.5, 3, 0.18,0.18,0.18,0.06);
							if (harvested >= 18) break;
						}
					}
				}
				sl.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY()+1.0, player.getZ(), 14, 1.6,0.7,1.6,0.06);
				sl.playSound(null, player.blockPosition(), SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 0.6F, 1.3F);
				player.getCooldowns().addCooldown(this, 55);
			}
			return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
		}
		@Override
		public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag f) {
			tip.add(Component.literal("Void Harvest: right-click phases 5×5 crops/leaves to inventory").withStyle(ChatFormatting.GRAY));
			tip.add(Component.literal("Reap: hit shreds 5 armor durability").withStyle(ChatFormatting.GRAY));
		}
	}
}
