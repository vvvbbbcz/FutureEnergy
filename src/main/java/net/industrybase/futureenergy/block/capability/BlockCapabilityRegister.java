package net.industrybase.futureenergy.block.capability;

import net.industrybase.api.CapabilityList;
import net.industrybase.api.electric.ElectricPower;
import net.industrybase.futureenergy.FutureEnergy;
import net.industrybase.futureenergy.block.entity.BlockEntityTypeList;
import net.industrybase.futureenergy.block.entity.InductionFurnaceBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = FutureEnergy.MODID)
public class BlockCapabilityRegister {
	@SubscribeEvent
	private static void registerCapabilities(final RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(CapabilityList.ELECTRIC_POWER,
			BlockEntityTypeList.INDUCTION_FURNACE.get(),
			InductionFurnaceBlockEntity::getElectricPower);
	}
}
