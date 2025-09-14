package net.industrybase.futureenergy.block;

import net.industrybase.futureenergy.FutureEnergy;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockList {
	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FutureEnergy.MODID);

	public static final DeferredBlock<CableConnectorBlock> CABLE_CONNECTOR =
		BLOCKS.register("cable_connector", CableConnectorBlock::new);
}
