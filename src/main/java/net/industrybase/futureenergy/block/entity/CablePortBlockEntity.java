package net.industrybase.futureenergy.block.entity;

import net.industrybase.api.electric.ElectricPower;
import net.industrybase.api.electric.IWireConnectable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * 线缆端口方块实体
 * 实现线缆连接功能和电力传输能力
 *
 * @author FutureEnergy
 */
public class CablePortBlockEntity extends BlockEntity implements IWireConnectable {
	private final ElectricPower electricPower;
	private boolean subscribed = false;

	public CablePortBlockEntity(BlockPos pos, BlockState blockState) {
		super(BlockEntityTypeList.CABLE_PORT.get(), pos, blockState);
		this.electricPower = new ElectricPower(this);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		// 注册到电力网络
		this.electricPower.register();
	}

	@Override
	public void setRemoved() {
		// 从电力网络中移除
		this.electricPower.remove();
		super.setRemoved();
	}

	/**
	 * 获取电力功能对象
	 */
	@Nullable
	public ElectricPower getElectricPower(Direction side) {
		return side.getOpposite() == this.getBlockState().getValue(BlockStateProperties.FACING) ? this.electricPower : null;
	}

	// IWireConnectable 接口实现
	@Override
	public boolean isSubscribed() {
		return this.subscribed;
	}

	@Override
	public void setSubscribed() {
		this.subscribed = true;
	}

	@Override
	public Set<BlockPos> getWires() {
		if (this.electricPower.getNetwork() != null) {
			return this.electricPower.getNetwork().getWireConn(this.worldPosition);
		}
		return Set.of();
	}
}
