package net.industrybase.futureenergy.item;

import net.industrybase.api.electric.ConnectHelper;
import net.industrybase.futureenergy.FutureEnergy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;
import java.util.Optional;

public class CableItem extends Item {
	public static final int MAX_LENGTH = 256;

	public CableItem() {
		super(new Properties()
				.durability(MAX_LENGTH)
				.component(DataComponents.CUSTOM_DATA, CustomData.EMPTY));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		return ConnectHelper.wireCoilUseOn(context, MAX_LENGTH);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag isAdvanced) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
		Optional<BlockPos> posOptional = NbtUtils.readBlockPos(tag, "ConnectPos");
		posOptional.ifPresent(bind ->
				components.add(Component.translatable("itemTooltip." + FutureEnergy.MODID + ".cable.1",
						bind.getX(), bind.getY(), bind.getZ())));
		components.add(Component.translatable("itemTooltip." + FutureEnergy.MODID + ".cable.2",
				stack.getMaxDamage() - stack.getDamageValue()));
	}
}
