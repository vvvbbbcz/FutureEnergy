package net.industrybase.futureenergy;

import net.industrybase.futureenergy.block.BlockList;
import net.industrybase.futureenergy.item.CreativeModeTabList;
import net.industrybase.futureenergy.item.ItemList;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(FutureEnergy.MODID)
public class FutureEnergy {
	public static final String MODID = "futureenergy";

	public FutureEnergy(IEventBus modEventBus, ModContainer modContainer) {
		BlockList.BLOCKS.register(modEventBus);
		ItemList.ITEMS.register(modEventBus);
		CreativeModeTabList.CREATIVE_MODE_TABS.register(modEventBus);
	}
}
