package openblocks.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import openblocks.lib.block.OpenBlock;

public class FlagBlock extends OpenBlock {
    public FlagBlock(Properties props) {
        super(props);
        //setPlacementMode(BlockPlacementMode.SURFACE);
        //setHardness(0.0F);
        // TODO support placing in directions other than down
    }

    private static final VoxelShape MIDDLE_AABB = Shapes.box(0.5 - (1.0 / 16.0), 0.0, 0.5 - (1.0 / 16.0), 0.5 + (1.0 / 16.0), 0.0 + 1.0, 0.5 + (1.0 / 16.0));
    private static final VoxelShape NS_AABB = Shapes.box(0.5 - (1.0 / 16.0), 0.0, 0.5 - (5.0 / 16.0), 0.5 + (1.0 / 16.0), 0.0 + 1.0, 0.5 + (5.0 / 16.0));
    private static final VoxelShape WE_AABB = Shapes.box(0.5 - (5.0 / 16.0), 0.0, 0.5 - (1.0 / 16.0), 0.5 + (5.0 / 16.0), 0.0 + 1.0, 0.5 + (1.0 / 16.0));

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return MIDDLE_AABB;
    }

    private boolean isFlagOnGround(BlockState state) {
        return true;
    }

    private boolean isBaseSolidForFlag(LevelReader world, BlockPos pos) {
        final BlockState belowState = world.getBlockState(pos.below());
        final Block belowBlock = belowState.getBlock();
        if (belowBlock instanceof FenceBlock) return true;
        if (belowBlock instanceof FlagBlock && isFlagOnGround(belowState)) return true;

        return false;
    }

    @Override
    protected boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return isBaseSolidForFlag(pLevel, pPos) || canSupportCenter(pLevel, pPos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        if (!pState.canSurvive(pLevel, pPos))
            return Blocks.AIR.defaultBlockState();
        else
            return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }
}