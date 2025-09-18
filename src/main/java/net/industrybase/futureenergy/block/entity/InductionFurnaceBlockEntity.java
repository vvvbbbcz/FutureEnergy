package net.industrybase.futureenergy.block.entity;

import net.industrybase.api.electric.ElectricPower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class InductionFurnaceBlockEntity extends BlockEntity {
	private final ElectricPower electricPower = new ElectricPower(this);

	public InductionFurnaceBlockEntity(BlockPos pos, BlockState blockState) {
		super(BlockEntityTypeList.INDUCTION_FURNACE.get(), pos, blockState);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.electricPower.register();
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, InductionFurnaceBlockEntity blockEntity) {
	}

	@Nullable
	public ElectricPower getElectricPower(Direction side) { // get electric capability
		if (side != Direction.UP) {
			return this.electricPower;
		}
		return null;
	}

	@Override
	public void setRemoved() {
		this.electricPower.remove();
		super.setRemoved();
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.electricPower.readFromNBT(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		this.electricPower.writeToNBT(tag);
	}
}
