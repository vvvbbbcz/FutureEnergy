package net.industrybase.futureenergy.item;

import net.industrybase.futureenergy.FutureEnergy;
import net.industrybase.futureenergy.block.BlockList;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemList {
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FutureEnergy.MODID);

	public static final DeferredItem<CrucibleTongsItem> CRUCIBLE_TONGS =
		ITEMS.register("crucible_tongs", CrucibleTongsItem::new);

	public static final DeferredItem<CeramicCrucibleItem> CERAMIC_CRUCIBLE =
		ITEMS.register("ceramic_crucible", CeramicCrucibleItem::new);

	public static final DeferredItem<BlockItem> INDUCTION_FURNACE =
		ITEMS.registerSimpleBlockItem(BlockList.INDUCTION_FURNACE);
}
