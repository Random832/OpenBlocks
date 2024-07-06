package openblocks.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import openblocks.OpenBlocks;
import openblocks.common.block.rotation.BlockRotationMode;
import openblocks.common.blockentity.XpDrainBlockEntity;
import openblocks.lib.block.OpenBlock;
import openblocks.lib.block.OpenEntityBlock;
import org.jetbrains.annotations.Nullable;

public class XpDrainBlock extends OpenEntityBlock<XpDrainBlockEntity> implements OpenBlock.Orientable {
    public XpDrainBlock(BlockBehaviour.Properties properties) {
        super(properties, OpenBlocks.DRAIN_BE);
    }
    private static final VoxelShape AABB = Block.box(0, 0, 0, 16, 1, 16);

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return AABB;
    }

    @Override
    protected @Nullable BlockEntityTicker<XpDrainBlockEntity> makeTicker(Level level, BlockState state) {
        return (pLevel, pPos, pState, pBlockEntity) -> pBlockEntity.tick();
    }

    @Override
    public BlockRotationMode getRotationMode() {
        return BlockRotationMode.FOUR_DIRECTION;
    }
}