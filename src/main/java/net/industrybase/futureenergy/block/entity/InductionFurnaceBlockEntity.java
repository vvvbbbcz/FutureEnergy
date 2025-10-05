package net.industrybase.futureenergy.block.entity;

import net.industrybase.api.electric.ElectricPower;
import net.industrybase.futureenergy.block.InductionFurnaceBlock;
import net.industrybase.futureenergy.inventory.InductionFurnaceMenu;
import net.industrybase.futureenergy.item.ICrucible;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;

public class InductionFurnaceBlockEntity extends BaseContainerBlockEntity {
	public static final int MAX_POWER = 30;
	private int oldPower = 0;
	private final ElectricPower electricPower = new ElectricPower(this);
	private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
	@Nullable
	private static volatile LinkedHashMap<Item, MetalInfo> metalCache;
	private int[] burnTimes = new int[4];
	private final ContainerData data = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0, 1, 2, 3 -> burnTimes[index] / 100;
				case 4 -> (int) (oldPower * 100.0);
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {

		}

		@Override
		public int getCount() {
			return 5;
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

	public static LinkedHashMap<Item, MetalInfo> getMetal() {
		LinkedHashMap<Item, MetalInfo> map = metalCache;
		if (map != null) {
			return map;
		} else {
			LinkedHashMap<Item, MetalInfo> map1 = new LinkedHashMap<>();
			buildMetals(map1);
			metalCache = map1;
			return map1;
		}
	}

	public static void buildMetals(LinkedHashMap<Item, MetalInfo> map) {
		map.put(Items.IRON_INGOT, new MetalInfo(200, 133, Metal.IRON));
		map.put(Items.IRON_NUGGET, new MetalInfo(50, 14, Metal.IRON));
		map.put(Items.GOLD_INGOT, new MetalInfo(200, 133, Metal.GOLD));
		map.put(Items.GOLD_NUGGET, new MetalInfo(50, 14, Metal.GOLD));
		map.put(Items.COPPER_INGOT, new MetalInfo(200, 133, Metal.COPPER));
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, InductionFurnaceBlockEntity BE) {
		boolean changed = false;
		boolean lit = BE.isLit();

		ItemStack crucibleStack = BE.getItem(0);
		if (crucibleStack.getItem() instanceof ICrucible crucible && !crucible.isFull()) {
			for (int i = 0; i < BE.burnTimes.length; i++) {
				ItemStack stack = BE.getItem(i + 1);
				MetalInfo info = getMetal().get(stack.getItem());
				if (info != null) {
					if (BE.oldPower != MAX_POWER) {
						BE.electricPower.setInputPower(MAX_POWER);
						BE.oldPower = MAX_POWER;
					}

					double inputPower = BE.electricPower.getRealInput();
					BE.burnTimes[i] += (int) (100 * inputPower / MAX_POWER);
					if (BE.burnTimes[i] >= info.burnTime) {
						changed = true;
						BE.burnTimes[i] = 0;
						BE.getItem(i + 1).shrink(1);
						crucible.put(info);
					}
				} else {
					if (BE.burnTimes[i] > 0) changed = true;
					BE.burnTimes[i] = 0;
				}
			}
		}

		boolean litNow = BE.isLit();
		if (lit != litNow) {
			BE.level.setBlock(pos, state.setValue(InductionFurnaceBlock.LIT, litNow), 2);
			if (!litNow) {
				BE.electricPower.setInputPower(0);
				BE.oldPower = 0;
			}
			changed = true;
		}

		if (changed) {
			BE.setChanged();
		}
	}

	private boolean isLit() {
		return this.litSlots() != 0;
	}

	private int litSlots() {
		int result = 0;
		for (int i = 0; i < this.burnTimes.length; i++) {
			if (this.burnTimes[i] > 0) result |= (1 << i);
		}
		return result;
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

		this.oldPower = tag.getInt("OldPower");

		int[] litTimes = tag.getIntArray("BurnTimes");
		if (litTimes.length == this.burnTimes.length)
			this.burnTimes = litTimes;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		this.electricPower.writeToNBT(tag);
		tag.putInt("OldPower", this.oldPower);
		tag.putIntArray("BurnTimes", this.burnTimes);
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

	public record MetalInfo(int burnTime, int volume, Metal metal) {
	}

	public enum Metal {
		IRON("iron"),
		GOLD("gold"),
		COPPER("copper"),
		;

		private final String name;

		Metal(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}
}
