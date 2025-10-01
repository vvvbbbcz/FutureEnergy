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

public class InductionFurnaceMenu extends AbstractContainerMenu {
	private final Container container;
	private final ContainerData data;

	public InductionFurnaceMenu(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new SimpleContainer(4), new SimpleContainerData(1));
	}

	public InductionFurnaceMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
		super(MenuTypeList.INDUCTION_FURNACE.get(), containerId);
		this.container = container;
		this.data = data;

		checkContainerSize(container, 4);
		checkContainerDataCount(data, 1);

		// add player inventory
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; ++i) {
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
		}

		this.addDataSlots(data);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}
}
