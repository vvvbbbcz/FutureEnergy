package net.industrybase.futureenergy.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.With;
import net.industrybase.api.electric.ElectricPower;
import net.industrybase.futureenergy.inventory.ElectricFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * 电炉方块实体
 * 实现电力驱动的熔炼功能，容量2000FE，消耗20FE/t，速度1.2x
 *
 * @author FutureEnergy
 */
public class ElectricFurnaceBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
	private static final int ENERGY_CONSUMPTION = 20; // FE/t

	private final ElectricPower electricPower = new ElectricPower(this);
	private final ItemStackHandler inputHandler = new ItemStackHandler(1);
	private final ItemStackHandler outputHandler = new ItemStackHandler(1);

	public ItemStackHandler getItemStackHandler(Direction side) {
		return switch (side) {
			case DOWN -> outputHandler;
			case UP -> inputHandler;
			default -> null;
		};
	}

	private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;
	@Getter
	private boolean hasEnergy = false;

	private Data data = new Data(0, 0);

	@With
	public record Data(
		int cookingProgress,
		int cookingTotalTime
	) {
		public static final Codec<Data> CODEC = RecordCodecBuilder.create(ins -> ins.group(
			Codec.INT.fieldOf("cookingProgress").forGetter(Data::cookingProgress),
			Codec.INT.fieldOf("cookingTotalTime").forGetter(Data::cookingTotalTime)
		).apply(ins, Data::new));
	}

	// 槽位索引
	private static final int INPUT_SLOT = 0;
	private static final int OUTPUT_SLOT = 1;

	// 容器数据同步
	private final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> ElectricFurnaceBlockEntity.this.data.cookingProgress;
				case 1 -> ElectricFurnaceBlockEntity.this.data.cookingTotalTime;
				case 2 -> hasEnergy ? 1 : 0;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0 -> data = ElectricFurnaceBlockEntity.this.data.withCookingProgress(value);
				case 1 -> data = ElectricFurnaceBlockEntity.this.data.withCookingTotalTime(value);
				case 2 -> hasEnergy = value == 1;
			}
		}

		@Override
		public int getCount() {
			return 3;
		}
	};

	public ElectricFurnaceBlockEntity(BlockPos pos, BlockState blockState) {
		super(BlockEntityTypeList.ELECTRIC_FURNACE.get(), pos, blockState);
		this.quickCheck = RecipeManager.createCheck(RecipeType.SMELTING);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.electricPower.register();
		this.electricPower.setInputPower(ENERGY_CONSUMPTION);
	}

	@Override
	public void setRemoved() {
		this.electricPower.remove();
		super.setRemoved();
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity blockEntity) {
		if (level.isClientSide) return;

		var smeltingRecipe = blockEntity.quickCheck.getRecipeFor(
			new SingleRecipeInput(blockEntity.inputHandler.getStackInSlot(0)),
			level
		);
		blockEntity.hasEnergy = blockEntity.electricPower.getRealInput() >= ENERGY_CONSUMPTION;
		smeltingRecipe.ifPresentOrElse(recipeHolder -> {
			// 检查是否有足够的能量
			if (blockEntity.hasEnergy) {
				// 增加熔炼进度
				int cookingTime = (int) (recipeHolder.value().getCookingTime() / 1.2);
				blockEntity.data = blockEntity.data.withCookingProgress(blockEntity.data.cookingProgress + 1)
					.withCookingTotalTime(cookingTime);

				// 完成熔炼
				if (blockEntity.data.cookingProgress >= cookingTime) {
					blockEntity.inputHandler.extractItem(0, 1, false);
					blockEntity.outputHandler.insertItem(0, recipeHolder.value().getResultItem(level.registryAccess()).copy(), false);
					blockEntity.data = blockEntity.data.withCookingProgress(0);
				}
			}
		}, () -> blockEntity.data = blockEntity.data.withCookingProgress(0));

		blockEntity.setChanged();
	}

	private void drop(ItemStack stack) {
		if (!stack.isEmpty()) {
			Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), stack);
		}
	}

	public void dropContents() {
		if (this.level != null) {
			drop(inputHandler.getStackInSlot(0));
			drop(outputHandler.getStackInSlot(0));
		}
	}

	@Nullable
	public ElectricPower getElectricPower(Direction side) {
		return this.electricPower;
	}

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
		return 2;
	}

	@Override
	public boolean isEmpty() {
		return inputHandler.getStackInSlot(0).isEmpty() && outputHandler.getStackInSlot(0).isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return switch (slot) {
			case 0 -> inputHandler.getStackInSlot(0);
			case 1 -> outputHandler.getStackInSlot(0);
			default -> ItemStack.EMPTY;
		};
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack result = switch (slot) {
			case 0 -> inputHandler.extractItem(0, amount, false);
			case 1 -> outputHandler.extractItem(0, amount, false);
			default -> ItemStack.EMPTY;
		};
		setChanged();
		return result;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		switch (slot) {
			case 0 -> inputHandler.setStackInSlot(0, ItemStack.EMPTY);
			case 1 -> outputHandler.setStackInSlot(0, ItemStack.EMPTY);
		}

		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		switch (slot) {
			case 0 -> inputHandler.setStackInSlot(0, stack);
			case 1 -> outputHandler.setStackInSlot(0, stack);
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
		inputHandler.setStackInSlot(0, ItemStack.EMPTY);
		outputHandler.setStackInSlot(0, ItemStack.EMPTY);
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		inputHandler.setStackInSlot(0, items.get(0));
		outputHandler.setStackInSlot(0, items.get(1));
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		NonNullList<ItemStack> itemStacks = NonNullList.withSize(2, ItemStack.EMPTY);
		itemStacks.set(0, inputHandler.getStackInSlot(0));
		itemStacks.set(1, outputHandler.getStackInSlot(0));
		return itemStacks;
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
		data = Data.CODEC.parse(NbtOps.INSTANCE, tag.getCompound("data")).getOrThrow(IllegalArgumentException::new);
		this.electricPower.readFromNBT(tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.put("data", Data.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow(IllegalArgumentException::new));
		this.electricPower.writeToNBT(tag);
	}
}
