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
 * 实现电炉GUI界面，包括能量条和熔炼进度显示
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
        // 调整标题位置
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
        
        // 绘制背景
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        
        // 绘制熔炼进度箭头
        if (this.menu.isBurning()) {
            int progress = this.menu.getCookingProgress();
            guiGraphics.blit(TEXTURE, x + 79, y + 34, 176, 14, progress + 1, 16);
        }
        
        // 绘制能量条
        int energyProgress = this.menu.getEnergyProgress();
        if (energyProgress > 0) {
            guiGraphics.blit(TEXTURE, x + 10, y + 13 + 52 - energyProgress, 176, 52 - energyProgress, 14, energyProgress);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);
        
        // 能量条工具提示
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;
        
        if (x >= leftPos + 10 && x <= leftPos + 24 && y >= topPos + 13 && y <= topPos + 65) {
            Component energyTooltip = Component.literal(String.format("%d / %d FE", 
                this.menu.getEnergy(), this.menu.getMaxEnergy()));
            guiGraphics.renderTooltip(this.font, energyTooltip, x, y);
        }
    }
}