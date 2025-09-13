package net.industrybase.futureenergy.block.entity;

import net.industrybase.api.electric.ElectricPower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * 太阳能板方块实体
 * 检测日光并在白天产生1FE/t的电力
 *
 * @author FutureEnergy
 */
public class SolarPanelBlockEntity extends BlockEntity {
	private final ElectricPower electricPower = new ElectricPower(this);
	private static final int POWER_OUTPUT = 10; // 1FE/t
	private boolean isGenerating = false;
	private int lastLightLevel = 0;
	public static final int RESISTANCE = 2;

	public SolarPanelBlockEntity(BlockPos pos, BlockState blockState) {
		super(BlockEntityTypeList.SOLAR_PANEL.get(), pos, blockState);
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
	public static void serverTick(Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity blockEntity) {
		if (level.isClientSide) return;

		// 检测日光强度
		int skyLightLevel = level.getBrightness(LightLayer.SKY, pos.above());
		boolean canGenerate = blockEntity.canGeneratePower(level, pos, skyLightLevel);

		// 更新发电状态
		if (canGenerate != blockEntity.isGenerating) {
			blockEntity.isGenerating = canGenerate;
			blockEntity.setChanged();
		}

		// 设置电力输出
		int outputPower = canGenerate ? POWER_OUTPUT : 0;
		blockEntity.electricPower.setOutputPower(outputPower);

		blockEntity.lastLightLevel = skyLightLevel;
	}

	/**
	 * 检查是否可以发电
	 * 参考日光检测器的逻辑
	 */
	private boolean canGeneratePower(Level level, BlockPos pos, int skyLightLevel) {
		// 检查是否为白天且有足够的天空光照
		if (skyLightLevel < 1) {
			return false;
		}

		// 检查上方是否有遮挡
		BlockPos abovePos = pos.above();
		if (!level.canSeeSky(abovePos)) {
			return false;
		}

		// 检查是否为白天（类似日光检测器的逻辑）
		long dayTime = level.getDayTime() % 24000L;

		return dayTime < 12300L || dayTime > 23850L;
	}

	/**
	 * 获取电力功能对象
	 */
	@Nullable
	public ElectricPower getElectricPower(Direction side) {
		return this.electricPower;
	}

	/**
	 * 获取当前是否正在发电
	 */
	public boolean isGenerating() {
		return this.isGenerating;
	}

	/**
	 * 获取当前光照等级
	 */
	public int getLastLightLevel() {
		return this.lastLightLevel;
	}

	/**
	 * 获取最大功率输出
	 */
	public static double getMaxPowerOutput() {
		return POWER_OUTPUT;
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		this.isGenerating = tag.getBoolean("IsGenerating");
		this.lastLightLevel = tag.getInt("LastLightLevel");
		this.electricPower.readFromNBT(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putBoolean("IsGenerating", this.isGenerating);
		tag.putInt("LastLightLevel", this.lastLightLevel);
		this.electricPower.writeToNBT(tag);
	}
}
