package net.industrybase.futureenergy;

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
	}
}
