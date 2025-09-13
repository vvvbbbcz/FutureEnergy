package net.industrybase.futureenergy.block.entity;

import net.industrybase.futureenergy.FutureEnergy;
import net.industrybase.futureenergy.block.BlockList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntityTypeList {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE =
		DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, FutureEnergy.MODID);

	// 线缆端口方块实体类型
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CablePortBlockEntity>> CABLE_PORT =
		BLOCK_ENTITY_TYPE.register("cable_port", () -> BlockEntityType.Builder.of(
			CablePortBlockEntity::new, BlockList.CABLE_PORT.get()).build(null));

	// 太阳能板方块实体类型
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL =
		BLOCK_ENTITY_TYPE.register("solar_panel", () -> BlockEntityType.Builder.of(
			SolarPanelBlockEntity::new, BlockList.SOLAR_PANEL.get()).build(null));

	// 电炉方块实体类型
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricFurnaceBlockEntity>> ELECTRIC_FURNACE =
		BLOCK_ENTITY_TYPE.register("electric_furnace", () -> BlockEntityType.Builder.of(
			ElectricFurnaceBlockEntity::new, BlockList.ELECTRIC_FURNACE.get()).build(null));
}
