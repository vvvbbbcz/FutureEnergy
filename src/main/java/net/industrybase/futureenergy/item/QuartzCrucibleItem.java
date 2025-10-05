package net.industrybase.futureenergy.item;

import net.industrybase.futureenergy.block.entity.InductionFurnaceBlockEntity;
import net.minecraft.world.item.Item;

public class QuartzCrucibleItem extends Item implements ICrucible {
	public QuartzCrucibleItem() {
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
