package net.industrybase.futureenergy.block;

import net.industrybase.futureenergy.FutureEnergy;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockList {
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FutureEnergy.MODID);

	public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));

	// 线缆端口方块
	public static final DeferredBlock<CablePortBlock> CABLE_PORT = BLOCKS.register("cable_port", 
		() -> new CablePortBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.METAL)
			.strength(3.0F, 6.0F)
			.requiresCorrectToolForDrops()));

	// 太阳能板方块
	public static final DeferredBlock<SolarPanelBlock> SOLAR_PANEL = BLOCKS.register("solar_panel", 
		() -> new SolarPanelBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.COLOR_BLUE)
			.strength(0.2F)
			.noOcclusion()));

	// 电炉方块
	public static final DeferredBlock<ElectricFurnaceBlock> ELECTRIC_FURNACE = BLOCKS.register("electric_furnace", 
		() -> new ElectricFurnaceBlock(BlockBehaviour.Properties.of()
			.mapColor(MapColor.STONE)
			.strength(3.5F)
			.requiresCorrectToolForDrops()));
}
