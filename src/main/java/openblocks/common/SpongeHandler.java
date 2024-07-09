package openblocks.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import openblocks.Config;
import openblocks.OpenBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;

@EventBusSubscriber(modid = OpenBlocks.MODID)
public class SpongeHandler {
    private static int getCleanupFlags(boolean update) {
        return update ? Block.UPDATE_ALL : Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    }

    public record CleanupResult(int count, boolean burn) {
        public boolean empty() {
            return count <= 0;
        }
    }

    public static CleanupResult doCleanup(ServerLevel level, BlockPos pos, int radius, TagKey<Fluid> tag, @Nullable TagKey<Fluid> burnTag, boolean update) {
        boolean burn = false;
        final int cleanupFlags = getCleanupFlags(update);
        int count = 0;
        for (BlockPos workPos : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))) {
            if (!level.isAreaLoaded(workPos, 0)) continue;
            FluidState fluidState = level.getFluidState(workPos);
            if (fluidState.isEmpty() || !(Config.spongeWorksOnEverything || fluidState.is(tag))) continue;
            BlockState blockState = level.getBlockState(workPos);
            if (blockState.getBlock() instanceof LiquidBlock) {
                level.setBlock(workPos, Blocks.AIR.defaultBlockState(), cleanupFlags);
            } else if (blockState.getBlock() instanceof SimpleWaterloggedBlock && fluidState.is(Fluids.WATER)) {
                level.setBlock(workPos, blockState.setValue(BlockStateProperties.WATERLOGGED, false), cleanupFlags);
            } else if (blockState.getBlock() instanceof BucketPickup bucketPickup) {
                // no good way to suppress updates
                bucketPickup.pickupBlock(null, level, pos, blockState);
            } else {
                // kelp etc, try our best
                Block.dropResources(blockState, level, workPos);
                level.setBlock(workPos, Blocks.AIR.defaultBlockState(), cleanupFlags);
            }
            count++;
            if(burnTag != null)
                burn |= fluidState.is(burnTag)
                        || Config.spongeBurnsInAllHotFluids && fluidState.getType().getFluidType().getTemperature() > 800;
            // 300 is water, 1300 is lava
        }
        return new CleanupResult(count, burn);
    }

    static int distance(BlockPos pos1, BlockPos pos2) {
        int dx = pos1.getX() - pos2.getX();
        int dy = pos1.getY() - pos2.getY();
        int dz = pos1.getZ() - pos2.getZ();
        return Math.max(Math.max(Mth.abs(dx), Mth.abs(dy)), Mth.abs(dz));
    }

    public static void doSuppressTicks(ServerLevel level, BlockPos center, int radius) {
        int x = center.getX();
        int z = center.getZ();
        int cx0 = SectionPos.blockToSectionCoord(x - radius);
        int cx1 = SectionPos.blockToSectionCoord(x + radius);
        int cz0 = SectionPos.blockToSectionCoord(z - radius);
        int cz1 = SectionPos.blockToSectionCoord(z + radius);
        if (level == null) return;
        for (int cx = cx0; cx <= cx1; cx++)
            for (int cz = cz0; cz <= cz1; cz++) {
                ChunkAccess chunk = level.getChunk(cx, cz, ChunkStatus.FULL, false);
                if (chunk == null) continue;
                if (chunk.getFluidTicks() instanceof LevelChunkTicks<Fluid> ticks)
                    ticks.removeIf(t -> distance(t.pos(), center) <= radius);
            }
    }

    @SubscribeEvent
    public static void onPlaced(BlockEvent.EntityPlaceEvent event) {
        if(event.getPlacedBlock().is(Blocks.SPONGE) && Config.vanillaSpongeHack)
            if(event.getLevel() instanceof ServerLevel serverLevel) {
                BlockPos pos = event.getPos();
                serverLevel.getServer().tell(new TickTask(0, () -> doSuppressTicks(serverLevel, pos, 7)));
            }
    }

    public static void unSuppressTicks(Level world, BlockPos pos, int radius) {
        // unfreeze liquids on cleared area border
        //for (BlockPos workPos : BlockPos.betweenClosed(pos.offset(-r, -r, -r), pos.offset(r, r, r))) {
        //    if (!world.isLoaded(workPos)) continue;
        //    if (world.getBlockState(workPos).is(OpenBlocks.SPONGE_AIR))
        //        world.setBlock(workPos, Blocks.AIR.defaultBlockState(), UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE);
        //}
        for (BlockPos workPos : BlockPos.betweenClosed(pos.offset(-radius - 1, -radius - 1, -radius - 1), pos.offset(radius + 1, radius + 1, radius + 1))) {
            if (!world.isLoaded(workPos)) continue;
            FluidState state = world.getFluidState(workPos);
            if(!state.isEmpty())
                world.scheduleTick(workPos.immutable(), state.getType(), 0);
        }
    }
}
