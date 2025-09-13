package net.industrybase.api.example.block.entity;

import net.industrybase.api.electric.ElectricPower;
import net.industrybase.api.example.block.DynamoBlock;
import net.industrybase.api.transmit.MechanicalTransmit;
import net.industrybase.api.util.ElectricHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Example of typical block entity uses transmit and electric capabilities.
 * For the corresponding block, see {@link DynamoBlock}.
 *
 * @author vvvbbbcz
 */
@SuppressWarnings("unused")
public class DynamoBlockEntity extends BlockEntity {
	private double oldPower;
	private static final int RESISTANCE = 2;
	private final MechanicalTransmit transmit = new MechanicalTransmit(this);
	private final ElectricPower electricPower = new ElectricPower(this);

	public DynamoBlockEntity(BlockPos pos, BlockState state) {
		super(null /* BlockEntityTypeList.DYNAMO.get() */, pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.transmit.register(); // register to the transmit network
		this.transmit.setResistance(RESISTANCE); // set init resistance
		this.electricPower.register(); // register to the electric network
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, DynamoBlockEntity blockEntity) {
		double power = ElectricHelper.fromTransmit(blockEntity.transmit.getSpeed(), RESISTANCE);
		if (power != blockEntity.oldPower) { // if power changed
			blockEntity.electricPower.setOutputPower(power); // update power
			blockEntity.oldPower = power;
		}
	}

	@Nullable
	public MechanicalTransmit getTransmit(Direction side) { // get transmit capability
		if (side == this.getBlockState().getValue(DynamoBlock.FACING)) {
			return this.transmit;
		}
		return null;
	}

	@Nullable
	public ElectricPower getElectricPower(Direction side) { // get electric capability
		if (side == this.getBlockState().getValue(DynamoBlock.FACING).getOpposite()) {
			return this.electricPower;
		}
		return null;
	}

	@Override
	public void setRemoved() {
		this.transmit.remove(); // remove from the transmit network
		this.electricPower.remove(); // remove from the electric network
		super.setRemoved();
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.oldPower = tag.getDouble("OldPower");
		this.transmit.readFromNBT(tag); // read data about transmit
		this.electricPower.readFromNBT(tag); // read data about electric
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putDouble("OldPower", this.oldPower);
		this.transmit.writeToNBT(tag); // write data about transmit
		this.electricPower.writeToNBT(tag); // write data about electric
	}
}
