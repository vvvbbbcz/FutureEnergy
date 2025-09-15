package net.industrybase.futureenergy.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 电炉菜单类
 * 处理电炉GUI的容器逻辑和物品槽位管理
 *
 * @author FutureEnergy
 */
public class ElectricFurnaceMenu extends AbstractContainerMenu {
	private final Container container;
	private final ContainerData data;

	// 槽位索引
	private static final int INPUT_SLOT = 0;
	private static final int OUTPUT_SLOT = 1;
	private static final int INVENTORY_START = 2;
	private static final int INVENTORY_END = 29;
	private static final int HOTBAR_START = 29;
	private static final int HOTBAR_END = 38;

	// 客户端构造函数
	public ElectricFurnaceMenu(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new SimpleContainer(2), new SimpleContainerData(4));
	}

	// 服务端构造函数
	public ElectricFurnaceMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
		super(MenuTypeList.ELECTRIC_FURNACE.get(), containerId);
		this.container = container;
		this.data = data;

		checkContainerSize(container, 2);
		checkContainerDataCount(data, 3);

		// 添加电炉槽位 - 根据UI元素位置调整 (输入槽: x:47, y:30, 输出槽: x:111, y:30)
		this.addSlot(new Slot(container, INPUT_SLOT, 47 + 5, 30 + 5) { // 居中显示，略大于默认大小
			@Override
			public boolean mayPlace(ItemStack stack) {
				// 只允许可熔炼的物品
				return canSmelt(stack);
			}
		});

		this.addSlot(new Slot(container, OUTPUT_SLOT, 111 + 5, 30 + 5) { // 居中显示，略大于默认大小
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false; // 输出槽不允许放入物品
			}
		});

		// 添加玩家背包槽位
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		// 添加玩家快捷栏槽位
		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
		}

		this.addDataSlots(data);
	}

	private boolean canSmelt(ItemStack stack) {
		return !stack.isEmpty();
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			itemStack = slotStack.copy();

			if (index == OUTPUT_SLOT) {
				// 从输出槽移动到背包
				if (!this.moveItemStackTo(slotStack, INVENTORY_START, HOTBAR_END, true)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickCraft(slotStack, itemStack);
			} else if (index == INPUT_SLOT) {
				// 从输入槽移动到背包
				if (!this.moveItemStackTo(slotStack, INVENTORY_START, HOTBAR_END, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= INVENTORY_START && index < HOTBAR_END) {
				// 从背包移动到电炉
				if (canSmelt(slotStack)) {
					if (!this.moveItemStackTo(slotStack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= INVENTORY_START && index < INVENTORY_END) {
					// 背包到快捷栏
					if (!this.moveItemStackTo(slotStack, HOTBAR_START, HOTBAR_END, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= HOTBAR_START && index < HOTBAR_END) {
					// 快捷栏到背包
					if (!this.moveItemStackTo(slotStack, INVENTORY_START, INVENTORY_END, false)) {
						return ItemStack.EMPTY;
					}
				}
			}

			if (slotStack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (slotStack.getCount() == itemStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, slotStack);
		}

		return itemStack;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}

	// 获取熔炼进度 (0-100)
	public int getCookingProgress() {
		int progress = this.data.get(0);
		int total = this.data.get(1);
		return total != 0 ? progress * 24 / total : 0;
	}

	// 检查是否正在燃烧
	public boolean isBurning() {
		return this.data.get(0) > 0;
	}

	// 获取电力状态
	public boolean hasEnergy() {
		return this.data.get(2) == 1;
	}
}
