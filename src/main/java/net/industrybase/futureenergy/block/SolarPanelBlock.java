package net.industrybase.futureenergy.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.api.util.ElectricHelper;
import net.industrybase.futureenergy.block.entity.SolarPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 太阳能板方块 - 在白天产生电力
 * 工作时间与日光检测器相同，产出1FE/t的能量
 * 
 * @author FutureEnergy
 */
public class SolarPanelBlock extends BaseEntityBlock {
    public static final MapCodec<SolarPanelBlock> CODEC = simpleCodec(SolarPanelBlock::new);

    public SolarPanelBlock() {
        super(Properties.ofFullCopy(Blocks.DAYLIGHT_DETECTOR)
                .strength(0.2F)
                .noOcclusion());
    }

    public SolarPanelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // 当方块被移除或状态改变时，更新电力网络
        ElectricHelper.updateOnRemove(level, state, newState, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarPanelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        // 只在服务端运行ticker
        return level.isClientSide ? null : createTickerHelper(blockEntityType, 
                net.industrybase.futureenergy.block.entity.BlockEntityTypeList.SOLAR_PANEL.get(), 
                SolarPanelBlockEntity::serverTick);
    }
}