package net.industrybase.api.util;

import net.industrybase.api.example.block.DynamoBlock;
import net.industrybase.api.transmit.TransmitNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class TransmitHelper {
	public static int fromElectric(double electricPower) {
		return (int) (electricPower * 50 / Math.PI);
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
	 * @param level    level
	 * @param state    old state
	 * @param newState new state
	 * @param pos      BlockPos
	 */
	@SuppressWarnings("deprecation")
	public static boolean updateOnRemove(LevelAccessor level, BlockState state, BlockState newState, BlockPos pos) {
		if (!level.isClientSide()) {
			if (state.is(newState.getBlock())) { // 确保是同种方块，防止重复更新
				TransmitNetwork.Manager.get(level).addOrChangeBlock(pos, () -> {
				});
				return true;
			}
		}
		return false;
	}
}
