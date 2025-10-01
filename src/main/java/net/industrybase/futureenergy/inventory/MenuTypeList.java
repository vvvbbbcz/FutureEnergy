package net.industrybase.futureenergy.inventory;

import net.industrybase.futureenergy.FutureEnergy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuTypeList {
	public static final DeferredRegister<MenuType<?>> MENU_TYPE = DeferredRegister.create(BuiltInRegistries.MENU, FutureEnergy.MODID);

	public static final DeferredHolder<MenuType<?>, MenuType<InductionFurnaceMenu>> INDUCTION_FURNACE =
		MENU_TYPE.register("induction_furnace",
			() -> new MenuType<>(InductionFurnaceMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
