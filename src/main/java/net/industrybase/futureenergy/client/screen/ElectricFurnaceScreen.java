package net.industrybase.futureenergy.client.screen;

import net.industrybase.futureenergy.FutureEnergy;
import net.industrybase.futureenergy.inventory.ElectricFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 电炉屏幕类
 * 实现电炉GUI界面，包括熔炼进度显示和电力状况显示
 *
 * @author FutureEnergy
 */
public class ElectricFurnaceScreen extends AbstractContainerScreen<ElectricFurnaceMenu> {
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(FutureEnergy.MODID, "textures/gui/electric_furnace.png");

	public ElectricFurnaceScreen(ElectricFurnaceMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
		this.imageHeight = 166;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;

		guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

		if (this.menu.hasEnergy()) {
			guiGraphics.blit(TEXTURE, x + 18, y + 57, 179, 3, 11, 20);
		}

		if (this.menu.isBurning()) {
			int progress = this.menu.getCookingProgress();
			int progressWidth = progress * 22 / 24;
			guiGraphics.blit(TEXTURE, x + 80, y + 35, 181, 28, progressWidth, 15);
		}
	}
}
