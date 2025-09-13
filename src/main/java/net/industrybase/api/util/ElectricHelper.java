package net.industrybase.api.util;

import net.industrybase.api.electric.ElectricNetwork;
import net.industrybase.api.example.block.DynamoBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricHelper {
	public static double fromTransmit(double speed, int resistance) {
		return speed * resistance * Math.PI / 50.0D;
	}

	/**
	 * This method should be called in the onRemove method of block,
	 * such as {@link DynamoBlock#onRemove(BlockState, Level, BlockPos, BlockState, boolean)}.
	 * It will check if the state is the same as the new state,
	 * if not, they will return false and NOT update.
	 * <p>
	 * These hooks only take effect when the block state is changed,
	 * such as when the block direction is changed by
	 * {@link Level#setBlock(BlockPos, BlockState, int)} or debug stick.
	 *
	 * @param level      level
	 * @param state      old state
	 * @param newState   new state
	 * @param pos        BlockPos
	 */
	@SuppressWarnings("deprecation")
	public static void updateOnRemove(LevelAccessor level, BlockState state, BlockState newState, BlockPos pos) {
		if (!level.isClientSide()) {
			if (state.is(newState.getBlock())) { // 确保是同种方块
				ElectricNetwork.Manager.get(level).addOrChangeBlock(pos, () -> {
				});
			}
		}
	}
}
