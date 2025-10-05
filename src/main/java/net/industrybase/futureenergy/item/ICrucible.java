package net.industrybase.futureenergy.item;

import net.industrybase.futureenergy.block.entity.InductionFurnaceBlockEntity;

public interface ICrucible {
	boolean isFull();

	void put(InductionFurnaceBlockEntity.MetalInfo info);
}
