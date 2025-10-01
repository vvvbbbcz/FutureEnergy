package net.industrybase.futureenergy.util;

import net.industrybase.futureenergy.FutureEnergy;
import net.minecraft.network.chat.Component;

public class ComponentHelper {
	public static Component translatable(String type, String key) {
		return Component.translatable(String.join(".", type, FutureEnergy.MODID, key));
	}
}
