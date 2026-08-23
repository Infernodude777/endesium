package com.infernodude777.endesium.block;

import com.mojang.serialization.MapCodec;
import com.infernodude777.endesium.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.infernodude777.endesium.registry.ModBlockEntities;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ResonantMechanismBlock extends BaseEntityBlock {
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
	private static final MapCodec<ResonantMechanismBlock> CODEC = simpleCodec(ResonantMechanismBlock::new);

	public ResonantMechanismBlock(Properties properties) {
		super(properties.lightLevel(state -> state.getValue(ACTIVE) ? 4 : 0));
		registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE);
	}

	@Override
	@Nullable
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ResonantMechanismBlockEntity(pos, state);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.RESONANT_MECHANISM, ResonantMechanismBlockEntity::serverTick);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, InteractionHand hand, BlockHitResult hit) {
		if (!stack.is(ModItems.RESONANCE_LENS)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (level.getBlockEntity(pos) instanceof ResonantMechanismBlockEntity mechanism) {
			if (level.isClientSide()) return ItemInteractionResult.SUCCESS;
			if (player instanceof ServerPlayer serverPlayer) {
				if (mechanism.isActive()) {
					// Existing worlds may contain mechanisms activated before the
					// advancement data was reloaded; inspecting one repairs that
					// progression without spawning a second reward.
					mechanism.awardAdvancements(serverPlayer);
					return ItemInteractionResult.CONSUME;
				}
				if (mechanism.activate(serverPlayer)) return ItemInteractionResult.CONSUME;
			}
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}
}
