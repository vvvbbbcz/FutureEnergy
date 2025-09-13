package net.industrybase.futureenergy.inventory;

import net.industrybase.futureenergy.FutureEnergy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuTypeList {
	public static final DeferredRegister<MenuType<?>> MENU_TYPE =
		DeferredRegister.create(BuiltInRegistries.MENU, FutureEnergy.MODID);

	// 电炉菜单类型
	public static final DeferredHolder<MenuType<?>, MenuType<ElectricFurnaceMenu>> ELECTRIC_FURNACE =
		MENU_TYPE.register("electric_furnace", () -> IMenuTypeExtension.create(
			(containerId, inventory, buffer) -> new ElectricFurnaceMenu(containerId, inventory)));
}
