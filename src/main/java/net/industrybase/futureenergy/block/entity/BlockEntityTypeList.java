package net.industrybase.futureenergy.block.entity;

import net.industrybase.futureenergy.FutureEnergy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntityTypeList {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE =
		DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, FutureEnergy.MODID);
}
