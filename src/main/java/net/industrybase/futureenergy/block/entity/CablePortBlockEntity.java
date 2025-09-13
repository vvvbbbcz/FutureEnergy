package net.industrybase.futureenergy.block.entity;

import net.industrybase.api.electric.ElectricPower;
import net.industrybase.api.electric.IWireConnectable;
import net.industrybase.futureenergy.block.CablePortBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
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
    private double inputPower = 0.0;
    private double outputPower = 0.0;
    
    // 能量存储相关
    private static final int MAX_ENERGY_CAPACITY = 10000;
    private static final int MAX_TRANSFER_RATE = 1000;

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
     * 服务端tick方法
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, CablePortBlockEntity blockEntity) {
        if (level.isClientSide) return;
        
        // 处理能量传输逻辑
//        blockEntity.handleEnergyTransfer();

		BlockEntity blockEntity1 = level.getBlockEntity(pos.above());
		// 更新电力输出
        if (blockEntity.outputPower > 0) {
            blockEntity.electricPower.setOutputPower(blockEntity.outputPower);
        }
        
        // 更新电力输入需求
        if (blockEntity.inputPower > 0) {
            blockEntity.electricPower.setInputPower(blockEntity.inputPower);
        }
    }

    /**
     * 处理能量传输
     */
    private void handleEnergyTransfer() {
        if (level == null || level.isClientSide) return;
        
        Direction facing = getBlockState().getValue(CablePortBlock.FACING);
        BlockPos adjacentPos = worldPosition.relative(facing);

        // 尝试与相邻方块进行能量交换
        if (level.isLoaded(adjacentPos)) {
            IEnergyStorage adjacentEnergy = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                    adjacentPos, facing.getOpposite());

            if (adjacentEnergy != null) {
                // 从相邻方块接收能量
                int received = adjacentEnergy.extractEnergy(MAX_TRANSFER_RATE, true);
                if (received > 0) {
                    int actualReceived = electricPower.receiveEnergy(received, false);
                    adjacentEnergy.extractEnergy(actualReceived, false);
                }

                // 向相邻方块输出能量
                int extracted = electricPower.extractEnergy(MAX_TRANSFER_RATE, true);
                if (extracted > 0) {
                    int actualSent = adjacentEnergy.receiveEnergy(extracted, false);
                    electricPower.extractEnergy(actualSent, false);
                }
            }
        }
    }

    /**
     * 获取电力功能对象
     */
    @Nullable
    public ElectricPower getElectricPower(Direction side) {
        return this.electricPower;
    }

    /**
     * 设置输入功率
     */
    public void setInputPower(double power) {
        this.inputPower = Math.max(0, power);
        setChanged();
    }

    /**
     * 设置输出功率
     */
    public void setOutputPower(double power) {
        this.outputPower = Math.max(0, power);
        setChanged();
    }

    /**
     * 获取当前输入功率
     */
    public double getInputPower() {
        return this.inputPower;
    }

    /**
     * 获取当前输出功率
     */
    public double getOutputPower() {
        return this.outputPower;
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

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.inputPower = tag.getDouble("InputPower");
        this.outputPower = tag.getDouble("OutputPower");
        this.subscribed = tag.getBoolean("Subscribed");
        this.electricPower.readFromNBT(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("InputPower", this.inputPower);
        tag.putDouble("OutputPower", this.outputPower);
        tag.putBoolean("Subscribed", this.subscribed);
        this.electricPower.writeToNBT(tag);
    }
}
