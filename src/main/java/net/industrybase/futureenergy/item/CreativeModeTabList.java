package net.industrybase.futureenergy.item;

import net.industrybase.futureenergy.FutureEnergy;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeModeTabList {
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FutureEnergy.MODID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("futureenergy", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.futureenergy"))
			.withTabsBefore(CreativeModeTabs.COMBAT)
			.icon(() -> ItemList.EXAMPLE_ITEM.get().getDefaultInstance())
			.displayItems((parameters, output) -> {
				output.accept(ItemList.EXAMPLE_ITEM.get());
				output.accept(ItemList.EXAMPLE_BLOCK_ITEM.get());
			}).build());
}
