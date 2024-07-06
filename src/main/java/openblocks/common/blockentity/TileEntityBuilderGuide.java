package openblocks.common.blockentity;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import openblocks.OpenBlocks;
import openblocks.lib.utils.BlockUtils;
import openblocks.lib.utils.render.GeometryUtils;

public class TileEntityBuilderGuide extends TileEntityGuide {
    private static final Random RANDOM = new Random();

    public TileEntityBuilderGuide(BlockPos blockPos, BlockState blockState) {
        super(OpenBlocks.BUILDER_GUIDE_BE.get(), blockPos, blockState);
    }

    private int ticks;

    @Override
    public boolean onItemUse(ServerPlayer player, ItemStack heldStack, final BlockHitResult hit) {
        if (active) {
            final Item heldItem = heldStack.getItem();
            if (heldItem instanceof BlockItem) {
                final BlockItem itemBlock = (BlockItem)heldItem;
                final Block block = itemBlock.getBlock();

                if (player.getAbilities().instabuild && isInFillMode()) {
                    creativeReplaceBlocks(block);
                    return true;
                } else {
                    return survivalPlaceBlocks(player, heldStack, hit);
                }
            }
        }

        return super.onItemUse(player, heldStack, hit);
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) {
            ticks++;
        }
    }

    private void creativeReplaceBlocks(final Block block) {
        // TODO verify
        for (BlockPos coord : getShapeSafe().getCoords()) {
            final BlockPos clickPos = worldPosition.offset(coord);
            level.setBlockAndUpdate(clickPos, block.defaultBlockState());
        }
    }

    @Override
    protected boolean canAddCoord(int x, int y, int z) {
        // create safe space around builder, so it's always accesible
        return Math.abs(x) > 1 || Math.abs(y) > 1 || Math.abs(z) > 1;
    }

    private boolean survivalPlaceBlocks(ServerPlayer player, ItemStack heldItem, final BlockHitResult hit) {
        for (BlockPos relCoord : getShapeSafe().getCoords()) {
            BlockPos absPos = worldPosition.offset(relCoord);
            if (level.isLoaded(absPos) && BlockUtils.isAir(level, absPos) && absPos.getY() >= 0 && absPos.getY() < 256) {
                final BlockHitResult fakeHit = new BlockHitResult(hit.getLocation(), hit.getDirection(), absPos, hit.isInside());
                final InteractionResult placeResult = player.gameMode.useItemOn(player, level, heldItem, InteractionHand.MAIN_HAND, fakeHit);

                if (placeResult.consumesAction()) {
                    //createServerRpcProxy(IGuideAnimationTrigger.class).trigger(absPos, world.getBlockState(absPos));
                    return true;
                }
            }

        }

        return false;
    }

    private boolean isInFillMode() {
        return level.getBlockState(worldPosition.above()).getBlock() == Blocks.OBSIDIAN;
    }

    public float getTicks() {
        return ticks;
    }

    // TODO animation
    //public void trigger(BlockPos pos, final BlockState state) {
    //    GeometryUtils.line3D(this.worldPosition, pos, (x, y, z) -> {
    //        final double dx = x + 0.5;
    //        final double dy = y + 0.5;
    //        final double dz = z + 0.5;
    //        for (int i = 0; i < 5; i++) {
    //            final double px = dx + 0.3 * RANDOM.nextFloat();
    //            final double py = dy + 0.3 * RANDOM.nextFloat();
    //            final double pz = dz + 0.3 * RANDOM.nextFloat();
    //            level.addParticle(ParticleTypes.PORTAL, px, py, pz, 0, 0, 0);
    //            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), px, py, pz, 0, 0, 0);
    //        }
    //    });
    //}
}