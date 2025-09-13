package net.industrybase.futureenergy;

import net.industrybase.futureenergy.block.entity.BlockEntityTypeList;
import net.industrybase.futureenergy.client.renderer.CablePortRenderer;
import net.industrybase.futureenergy.client.screen.ElectricFurnaceScreen;
import net.industrybase.futureenergy.inventory.MenuTypeList;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = FutureEnergy.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = FutureEnergy.MODID, value = Dist.CLIENT)
public class FutureEnergyClient {
	public FutureEnergyClient(ModContainer container) {
	}

	@SubscribeEvent
	static void onClientSetup(FMLClientSetupEvent event) {
		// 注册方块实体渲染器
		BlockEntityRenderers.register(BlockEntityTypeList.CABLE_PORT.get(), CablePortRenderer::new);
	}

	@SubscribeEvent
	static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
		// 注册屏幕
		event.register(MenuTypeList.ELECTRIC_FURNACE.get(), ElectricFurnaceScreen::new);
	}
}
