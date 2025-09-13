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

	public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder()
			.alwaysEdible().nutrition(1).saturationModifier(2f).build()));

	public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", BlockList.EXAMPLE_BLOCK);

	// 线缆物品
	public static final DeferredItem<CableItem> CABLE = ITEMS.register("cable", 
		() -> new CableItem(new Item.Properties().durability(64).stacksTo(1)));

	// 线缆端口方块物品
	public static final DeferredItem<BlockItem> CABLE_PORT_ITEM = ITEMS.registerSimpleBlockItem("cable_port", BlockList.CABLE_PORT);

	// 太阳能板方块物品
	public static final DeferredItem<BlockItem> SOLAR_PANEL_ITEM = ITEMS.registerSimpleBlockItem("solar_panel", BlockList.SOLAR_PANEL);

	// 电炉方块物品
	public static final DeferredItem<BlockItem> ELECTRIC_FURNACE_ITEM = ITEMS.registerSimpleBlockItem("electric_furnace", BlockList.ELECTRIC_FURNACE);
}
