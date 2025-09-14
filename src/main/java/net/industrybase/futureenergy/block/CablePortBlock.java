package net.industrybase.futureenergy.block;

import com.mojang.serialization.MapCodec;
import net.industrybase.api.util.ElectricHelper;
import net.industrybase.futureenergy.block.entity.CablePortBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

/**
 * 线缆端口方块 - 用于电力传输的连接点
 * 支持线缆连接和电力输入输出功能
 * 
 * @author FutureEnergy
 */
public class CablePortBlock extends BaseEntityBlock {
    public static final MapCodec<CablePortBlock> CODEC = simpleCodec(CablePortBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public CablePortBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 根据玩家放置时的朝向设置方块朝向
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CablePortBlockEntity(pos, state);
    }
}
