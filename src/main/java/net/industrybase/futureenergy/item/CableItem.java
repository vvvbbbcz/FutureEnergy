package net.industrybase.futureenergy.item;

import net.industrybase.api.electric.ConnectHelper;
import net.industrybase.api.electric.IWireCoil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 线缆物品 - 用于连接线缆端口的工具
 * 实现IWireCoil接口，支持线缆连接功能
 * 
 * @author FutureEnergy
 */
public class CableItem extends Item implements IWireCoil {
    private static final int MAX_CABLE_LENGTH = 32; // 最大线缆长度
    private static final int DURABILITY = 64; // 耐久度

    public CableItem() {
        super(new Properties()
                .durability(DURABILITY)
                .stacksTo(1)); // 线缆不能堆叠，因为需要存储连接信息
    }

    public CableItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // 使用ConnectHelper处理线缆连接逻辑
        return ConnectHelper.wireCoilUseOn(context, MAX_CABLE_LENGTH);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        
        // 添加使用说明
        tooltipComponents.add(Component.translatable("item.futureenergy.cable.tooltip.usage"));
        
        // 显示最大连接距离
        tooltipComponents.add(Component.translatable("item.futureenergy.cable.tooltip.max_length", MAX_CABLE_LENGTH));
        
        // 显示当前耐久度
        int damage = stack.getDamageValue();
        int maxDamage = stack.getMaxDamage();
        int remaining = maxDamage - damage;
        tooltipComponents.add(Component.translatable("item.futureenergy.cable.tooltip.durability", remaining, maxDamage));
        
        // 如果物品存储了连接位置信息，显示提示
        if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            if (customData != null && customData.contains("ConnectPos")) {
                tooltipComponents.add(Component.translatable("item.futureenergy.cable.tooltip.connected"));
            }
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        // 显示耐久度条
        return stack.isDamaged();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        // 计算耐久度条宽度
        return Math.round(13.0F - (float)stack.getDamageValue() * 13.0F / (float)stack.getMaxDamage());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        // 根据耐久度设置颜色
        float durabilityRatio = 1.0F - (float)stack.getDamageValue() / (float)stack.getMaxDamage();
        return net.minecraft.util.Mth.hsvToRgb(durabilityRatio / 3.0F, 1.0F, 1.0F);
    }



    /**
     * 获取最大线缆长度
     */
    public static int getMaxCableLength() {
        return MAX_CABLE_LENGTH;
    }

    /**
     * 检查物品是否可以用于连接
     */
    public boolean canConnect(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == this && stack.getDamageValue() < stack.getMaxDamage();
    }

    /**
     * 消耗线缆长度（增加损坏值）
     */
    public void consumeLength(ItemStack stack, int length) {
        if (canConnect(stack)) {
            stack.setDamageValue(Math.min(stack.getDamageValue() + length, stack.getMaxDamage()));
        }
    }

    /**
     * 获取剩余可用长度
     */
    public int getRemainingLength(ItemStack stack) {
        if (!canConnect(stack)) {
            return 0;
        }
        return stack.getMaxDamage() - stack.getDamageValue();
    }
}