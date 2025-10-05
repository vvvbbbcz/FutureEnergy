package net.industrybase.futureenergy.item;

import net.industrybase.futureenergy.block.entity.InductionFurnaceBlockEntity;
import net.minecraft.world.item.Item;

public class CeramicCrucibleItem extends Item implements ICrucible {
	public CeramicCrucibleItem() {
		super(new Properties());
	}

	@Override
	public boolean isFull() {
		return false;
	}

	@Override
	public void put(InductionFurnaceBlockEntity.MetalInfo info) {

	}
}
