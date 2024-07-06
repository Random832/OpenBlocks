package openblocks.lib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import openblocks.ModTags;
import openblocks.common.block.rotation.BlockRotationMode;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class OpenBlock extends Block {

    public OpenBlock(Block.Properties props) {
        super(props);
    }

    protected boolean suppressPickBlock() {
        return false;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader world, BlockPos pos, Player player) {
        return suppressPickBlock() ? ItemStack.EMPTY : super.getCloneItemStack(state, target, world, pos, player);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if(this instanceof Orientable ob)
            ob.getRotationMode().createBlockStateDefinition(builder);
    }


    public interface Orientable extends ToolRotatable {
        BlockRotationMode getRotationMode();

        @Override
        default BlockState rotateWithTool(BlockState state, Direction clickedFace) {
            return getRotationMode().toolRotate(state, clickedFace);
        }
    }

    public interface ToolRotatable {
        @ApiStatus.OverrideOnly
        BlockState rotateWithTool(BlockState state, Direction clickedFace);

        default boolean rotateWithTool(UseOnContext context) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state1 = level.getBlockState(pos);
            BlockState state2 = rotateWithTool(state1, context.getClickedFace());
            return state1 != state2 && level.setBlockAndUpdate(pos, state2);
        }
    }

    //region Orientable
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockState state = defaultBlockState();
        if(this instanceof Orientable ob)
            return ob.getRotationMode().getStateForPlacement(state, pContext.getClickedPos(), pContext.getPlayer());
        else
            return state;
    }


    @Override
    public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation direction) {
        if(this instanceof Orientable ob)
            return ob.getRotationMode().rotateY(state, direction);
        else return state;
    }

    @Override
    protected BlockState mirror(BlockState pState, Mirror pMirror) {
        if (this instanceof Orientable ob)
            return ob.getRotationMode().mirror(pState, pMirror);
        else return pState;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if(pStack.is(ModTags.WRENCHES) && this instanceof ToolRotatable rb) {
            if(rb.rotateWithTool(new UseOnContext(pLevel, pPlayer, pHand, pStack, pHitResult)))
                return ItemInteractionResult.sidedSuccess(pLevel.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    //endregion
}