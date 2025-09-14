package net.industrybase.futureenergy.item;

import net.industrybase.futureenergy.FutureEnergy;
import net.industrybase.futureenergy.block.BlockList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemList {
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FutureEnergy.MODID);

	public static final DeferredItem<Item> CABLE = ITEMS.register("cable", CableItem::new);

	public static final DeferredItem<BlockItem> CABLE_CONNECTOR =
		ITEMS.registerSimpleBlockItem(BlockList.CABLE_CONNECTOR);
}
