package net.industrybase.futureenergy.client.renderer.blockentity;

import net.industrybase.api.client.renderer.blockentity.WireConnectableRenderer;
import net.industrybase.futureenergy.FutureEnergy;
import net.industrybase.futureenergy.block.entity.BlockEntityTypeList;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = FutureEnergy.MODID, value = Dist.CLIENT)
public class RendererManager {
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) { // 注册方块实体的渲染器
		event.registerBlockEntityRenderer(BlockEntityTypeList.CABLE_CONNECTOR.get(), WireConnectableRenderer::new);
	}

	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
	}
}
