package net.industrybase.futureenergy.block.entity;

import net.industrybase.api.electric.ElectricPower;
import net.industrybase.futureenergy.block.ElectricFurnaceBlock;
import net.industrybase.futureenergy.inventory.ElectricFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * 电炉方块实体
 * 实现电力驱动的熔炼功能，容量2000FE，消耗20FE/t，速度1.2x
 *
 * @author FutureEnergy
 */
public class ElectricFurnaceBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
	private static final int ENERGY_CAPACITY = 200000;
	private static final int ENERGY_CONSUMPTION = 20; // FE/t
	private static final int COOK_TIME_TOTAL = (int) (200 / 1.2); // 1.2x speed = ~167 ticks

	private final ElectricPower electricPower;
	private final ItemStackHandler itemHandler;
	private final NonNullList<ItemStack> items;

	// 熔炼进度
	private int cookingProgress = 0;
	private int cookingTotalTime = COOK_TIME_TOTAL;

	// 槽位索引
	private static final int INPUT_SLOT = 0;
	private static final int OUTPUT_SLOT = 1;

	// 容器数据同步
	private final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> ElectricFurnaceBlockEntity.this.cookingProgress;
				case 1 -> ElectricFurnaceBlockEntity.this.cookingTotalTime;
				case 2 -> ElectricFurnaceBlockEntity.this.electricPower.getEnergyStored();
				case 3 -> ElectricFurnaceBlockEntity.this.electricPower.getMaxEnergyStored();
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0 -> ElectricFurnaceBlockEntity.this.cookingProgress = value;
				case 1 -> ElectricFurnaceBlockEntity.this.cookingTotalTime = value;
			}
		}

		@Override
		public int getCount() {
			return 4;
		}
	};

	public ElectricFurnaceBlockEntity(BlockPos pos, BlockState blockState) {
		super(BlockEntityTypeList.ELECTRIC_FURNACE.get(), pos, blockState);
		this.electricPower = new ElectricPower(this);
		this.items = NonNullList.withSize(2, ItemStack.EMPTY);
		this.itemHandler = new ItemStackHandler(2) {
			@Override
			protected void onContentsChanged(int slot) {
				ElectricFurnaceBlockEntity.this.setChanged();
			}
		};
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.electricPower.register();
		this.electricPower.setInputPower(20);
	}

	@Override
	public void setRemoved() {
		this.electricPower.remove();
		super.setRemoved();
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity blockEntity) {
		if (level.isClientSide) return;

		boolean wasLit = state.getValue(ElectricFurnaceBlock.LIT);
		boolean isLit = false;

		// 检查是否可以熔炼
		if (blockEntity.canSmelt()) {
			// 检查是否有足够的能量
			if (blockEntity.electricPower.getRealInput() >= ENERGY_CONSUMPTION) {
				// 增加熔炼进度
				blockEntity.cookingProgress++;
				isLit = true;

				// 完成熔炼
				if (blockEntity.cookingProgress >= blockEntity.cookingTotalTime) {
					blockEntity.smeltItem();
					blockEntity.cookingProgress = 0;
				}
			}
		} else {
			blockEntity.cookingProgress = 0;
		}

		// 更新方块状态
		if (wasLit != isLit) {
			level.setBlock(pos, state.setValue(ElectricFurnaceBlock.LIT, isLit), 3);
		}

		blockEntity.setChanged();
	}



	private boolean canSmelt() {
		if (this.items.get(INPUT_SLOT).isEmpty()) {
			return false;
		}

		SingleRecipeInput recipeInput = new SingleRecipeInput(this.items.get(INPUT_SLOT));
		var recipe = this.level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, recipeInput, this.level);

		if (recipe.isEmpty()) {
			return false;
		}

		ItemStack result = recipe.get().value().getResultItem(this.level.registryAccess());
		ItemStack outputStack = this.items.get(OUTPUT_SLOT);

		if (outputStack.isEmpty()) {
			return true;
		}

		if (!ItemStack.isSameItemSameComponents(outputStack, result)) {
			return false;
		}

		return outputStack.getCount() + result.getCount() <= outputStack.getMaxStackSize();
	}

	private void smeltItem() {
		if (!canSmelt()) {
			return;
		}

		SingleRecipeInput recipeInput = new SingleRecipeInput(this.items.get(INPUT_SLOT));
		var recipe = this.level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, recipeInput, this.level);

		if (recipe.isPresent()) {
			ItemStack result = recipe.get().value().getResultItem(this.level.registryAccess()).copy();
			ItemStack outputStack = this.items.get(OUTPUT_SLOT);

			if (outputStack.isEmpty()) {
				this.items.set(OUTPUT_SLOT, result);
			} else {
				outputStack.grow(result.getCount());
			}

			this.items.get(INPUT_SLOT).shrink(1);
		}
	}

	public void dropContents() {
		if (this.level != null) {
			for (ItemStack stack : this.items) {
				if (!stack.isEmpty()) {
					net.minecraft.world.Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), stack);
				}
			}
		}
	}

	@Nullable
	public ElectricPower getElectricPower(Direction side) {
		return this.electricPower;
	}

	public ContainerData getDataAccess() {
		return this.dataAccess;
	}

	// BaseContainerBlockEntity 实现
	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.futureenergy.electric_furnace");
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new ElectricFurnaceMenu(containerId, inventory, this, this.dataAccess);
	}

	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	public boolean isEmpty() {
		return this.items.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		return ContainerHelper.removeItem(this.items, slot, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(this.items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.items.set(slot, stack);
		if (stack.getCount() > this.getMaxStackSize()) {
			stack.setCount(this.getMaxStackSize());
		}
		this.setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return this.level != null && this.level.getBlockEntity(this.worldPosition) == this &&
			player.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5) <= 64.0;
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items.clear();
		this.items.addAll(items);
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	// WorldlyContainer 实现
	@Override
	public int[] getSlotsForFace(Direction side) {
		return switch (side) {
			case DOWN -> new int[]{OUTPUT_SLOT}; // 底部只能输出
			default -> new int[]{INPUT_SLOT}; // 其他面只能输入
		};
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
		return index == INPUT_SLOT && direction != Direction.DOWN;
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		return index == OUTPUT_SLOT && direction == Direction.DOWN;
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		ContainerHelper.loadAllItems(tag, this.items, registries);
		this.cookingProgress = tag.getInt("CookingProgress");
		this.cookingTotalTime = tag.getInt("CookingTotalTime");
		this.electricPower.readFromNBT(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		ContainerHelper.saveAllItems(tag, this.items, registries);
		tag.putInt("CookingProgress", this.cookingProgress);
		tag.putInt("CookingTotalTime", this.cookingTotalTime);
		this.electricPower.writeToNBT(tag);
	}
}
