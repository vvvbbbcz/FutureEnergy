package net.industrybase.futureenergy.block.capability;

import net.industrybase.api.CapabilityList;
import net.industrybase.api.IndustryBaseApi;
import net.industrybase.futureenergy.block.entity.BlockEntityTypeList;
import net.industrybase.futureenergy.block.entity.CablePortBlockEntity;
import net.industrybase.futureenergy.block.entity.ElectricFurnaceBlockEntity;
import net.industrybase.futureenergy.block.entity.SolarPanelBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * @author TT432
 */
@EventBusSubscriber(modid = IndustryBaseApi.MODID)
public class CapabilityRegisterHandler {
	@SubscribeEvent
	public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(CapabilityList.ELECTRIC_POWER,
			BlockEntityTypeList.ELECTRIC_FURNACE.get(), ElectricFurnaceBlockEntity::getElectricPower);
		event.registerBlockEntity(CapabilityList.ELECTRIC_POWER,
			BlockEntityTypeList.SOLAR_PANEL.get(), SolarPanelBlockEntity::getElectricPower);
		event.registerBlockEntity(CapabilityList.ELECTRIC_POWER,
			BlockEntityTypeList.CABLE_PORT.get(), CablePortBlockEntity::getElectricPower);

		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
			BlockEntityTypeList.ELECTRIC_FURNACE.get(), ElectricFurnaceBlockEntity::getItemStackHandler);
	}
}
