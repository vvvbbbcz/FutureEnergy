package net.industrybase.futureenergy.block.entity;

import net.industrybase.api.electric.ElectricPower;
import net.industrybase.futureenergy.inventory.InductionFurnaceMenu;
import net.industrybase.futureenergy.util.ComponentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class InductionFurnaceBlockEntity extends BaseContainerBlockEntity {
	private final ElectricPower electricPower = new ElectricPower(this);
	private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
	@Nullable
	private static volatile LinkedHashMap<Item, MetalInfo> metalCache;
	private int[] burnTimes = new int[4];
	private ContainerData data = new ContainerData() {
		@Override
		public int get(int index) {
			return 0;
		}

		@Override
		public void set(int index, int value) {

		}

		@Override
		public int getCount() {
			return 1;
		}
	};

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

	@Override
	protected Component getDefaultName() {
		return ComponentHelper.translatable("container", "induction_furnace");
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new InductionFurnaceMenu(containerId, inventory, this, this.data);
	}

	@Override
	public int getContainerSize() {
		return this.items.size();
	}
}
