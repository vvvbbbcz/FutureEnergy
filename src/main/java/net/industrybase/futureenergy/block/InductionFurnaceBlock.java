package net.industrybase.futureenergy.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.api.util.ElectricHelper;
import net.industrybase.futureenergy.block.entity.BlockEntityTypeList;
import net.industrybase.futureenergy.block.entity.InductionFurnaceBlockEntity;
import net.industrybase.futureenergy.item.ICrucible;
import net.industrybase.futureenergy.item.ItemList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class InductionFurnaceBlock extends BaseEntityBlock {
	public static final MapCodec<InductionFurnaceBlock> CODEC = simpleCodec((properties) -> new InductionFurnaceBlock());
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	protected InductionFurnaceBlock() {
		super(Properties.ofFullCopy(Blocks.IRON_BLOCK));
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			Direction direction = hitResult.getDirection();
			BlockEntity entity = level.getBlockEntity(pos);
			if (entity instanceof InductionFurnaceBlockEntity furnaceBE) {
				ItemStack crucibleStack = furnaceBE.getItem(0);
				if (direction != Direction.UP) {
					if (crucibleStack.getItem() instanceof ICrucible crucible) {
						// TODO open gui
					} else {
						return InteractionResult.PASS;
					}
				} else {
					ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
					if (crucibleStack.getItem() instanceof ICrucible crucible) {
						if (stack.is(ItemList.CRUCIBLE_TONGS)) {
							// TODO get crucible
						}
					} else {
						if (stack.getItem() instanceof ICrucible) {
							player.setItemInHand(InteractionHand.MAIN_HAND, furnaceBE.getItem(0));
							furnaceBE.setItem(0, stack);
							furnaceBE.setChanged();

							return InteractionResult.CONSUME;
						}
					}
				}
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		ElectricHelper.updateOnRemove(level, state, newState, pos);
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new InductionFurnaceBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> serverType) {
		return level.isClientSide ? null : createTickerHelper(serverType, BlockEntityTypeList.INDUCTION_FURNACE.get(), InductionFurnaceBlockEntity::serverTick);
	}
}
