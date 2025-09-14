package net.industrybase.futureenergy.block.entity;

import com.mojang.datafixers.DSL;
import net.industrybase.futureenergy.FutureEnergy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntityTypeList {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE =
		DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, FutureEnergy.MODID);

	private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> blockEntity, DeferredHolder<Block, ? extends Block> block) {
		return BLOCK_ENTITY_TYPE.register(name, () -> BlockEntityType.Builder.of(blockEntity, block.get()).build(DSL.remainderType()));
	}
}
