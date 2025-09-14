package net.industrybase.futureenergy.block.capability;

import net.industrybase.futureenergy.FutureEnergy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = FutureEnergy.MODID)
public class BlockCapabilityRegister {
	@SubscribeEvent
	private static void registerCapabilities(final RegisterCapabilitiesEvent event) {
	}
}
