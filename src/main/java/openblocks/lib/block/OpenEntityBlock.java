package openblocks.lib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import openblocks.common.blockentity.*;
import openblocks.lib.blockentity.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class OpenEntityBlock<T extends BlockEntity> extends OpenBlock implements EntityBlock {
    private final Supplier<BlockEntityType<T>> blockEntityType;

    public OpenEntityBlock(Properties props, Supplier<BlockEntityType<T>> beType) {
        super(props);
        this.blockEntityType = beType;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult hit, LevelReader level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof ICustomPickItem be)
            return be.getPickBlock(player);
        return super.getCloneItemStack(state, hit, level, pos, player);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState pNeighborState, LevelAccessor level, BlockPos pos, BlockPos fromPos) {
        if (level.getBlockEntity(pos) instanceof INeighbourAwareTile be)
            be.onNeighbourChanged(fromPos, pNeighborState.getBlock());
        return super.updateShape(state, direction, pNeighborState, level, pos, fromPos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHitResult) {
        final BlockEntity te = pLevel.getBlockEntity(pPos);

        if (te instanceof UsableBlockEntity ue)
            return ue.useWithoutItem(pPlayer, pHitResult);
        //noinspection deprecation
        if (te instanceof IActivateAwareTile ae)
            return (ae).onBlockActivated(pPlayer, InteractionHand.MAIN_HAND, pHitResult);

        return InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        final BlockEntity te = pLevel.getBlockEntity(pPos);
        if (te instanceof UsableBlockEntity ue)
            return ue.useItemOn(pPlayer, pHand, pHitResult, pStack);
        //noinspection deprecation
        if (te instanceof IActivateAwareTile ae) {
            InteractionResult result = ae.onBlockActivated(pPlayer, pHand, pHitResult);
            return switch (result) {
                case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
                case CONSUME -> ItemInteractionResult.CONSUME;
                case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
                case PASS -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; // ???
                case FAIL -> ItemInteractionResult.FAIL;
            };
        }
        return super.useItemOn(pStack, pState, pLevel, pPos, pPlayer, pHand, pHitResult);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T> @Nullable T getTileEntity(LevelReader level, BlockPos blockPos, Class<? extends T> cls) {
        BlockEntity be = level.getBlockEntity(blockPos);
        return (cls.isInstance(be))? (T)be : null;
    }

    public @Nullable T getBlockEntity(BlockGetter level, BlockPos blockPos) {
        return blockEntityType.get().getBlockEntity(level, blockPos);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, blockPos, state, placer, stack);

        final BlockEntity te = level.getBlockEntity(blockPos);
        if (te instanceof IPlaceAwareTile)
            ((IPlaceAwareTile) te).onBlockPlacedBy(state, placer, stack);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return blockEntityType.get().create(pPos, pState);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public final <U extends BlockEntity> BlockEntityTicker<U> getTicker(Level pLevel, BlockState pState, BlockEntityType<U> pBlockEntityType) {
        if(pBlockEntityType == blockEntityType.get())
            return (BlockEntityTicker<U>)makeTicker(pLevel, pState);
        return null;
    }

    @Nullable
    protected BlockEntityTicker<T> makeTicker(Level level, BlockState state) {
        return null;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        if(pParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ICustomBreakDrops be)
            return be.getDrops(super.getDrops(pState, pParams));
        return super.getDrops(pState, pParams);
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }
}
