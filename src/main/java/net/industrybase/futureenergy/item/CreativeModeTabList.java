package net.industrybase.futureenergy.item;

import net.industrybase.futureenergy.FutureEnergy;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeModeTabList {
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FutureEnergy.MODID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FUTURE_ENERGY =
			CREATIVE_MODE_TABS.register("future_energy", () -> CreativeModeTab.builder()
				.title(Component.translatable("itemGroup.future_energy"))
				.icon(() -> ItemList.CABLE_CONNECTOR.get().getDefaultInstance())
				.displayItems((p, context) -> {
					context.accept(ItemList.CABLE_CONNECTOR.get());
					context.accept(ItemList.CABLE.get());
				})
				.build());
}
