package openblocks.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import openblocks.Config;
import openblocks.ModTags;
import openblocks.common.SpongeHandler;
import openblocks.lib.block.OpenBlock;
import org.jetbrains.annotations.Nullable;

public class SpongeBlock extends OpenBlock {

    private static final int TICK_RATE = 20 * 5;

    private static final int EVENT_BURN = 123;
    private final TagKey<Fluid> tag;
    private final @Nullable TagKey<Fluid> burnTag;

    public SpongeBlock(Properties props, TagKey<Fluid> tag, @Nullable TagKey<Fluid> burnTag) {
        super(props);
        //setSoundType(SoundType.CLOTH);
        //setHarvestLevel("axe", 1);
        this.tag = tag;
        this.burnTag = burnTag;
    }
    public static SpongeBlock makeWaterSponge(Properties props) {
        return new SpongeBlock(props, ModTags.SPONGE_EFFECTIVE, ModTags.SPONGE_BURNS);

    }
    public static SpongeBlock makeLavaSponge(Properties props) {
        return new SpongeBlock(props, ModTags.LAVA_SPONGE_EFFECTIVE, null);
    }

    @Override
    protected BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        // block ticks don't seem to run at the right time
        if(pLevel instanceof ServerLevel serverLevel && !pLevel.getFluidState(pNeighborPos).isEmpty()) {
            serverLevel.getServer().tell(new TickTask(0, () -> {
                doCleanup(serverLevel, pPos);
                SpongeHandler.doSuppressTicks(serverLevel, pPos, Config.spongeRange + 1);
            }));
        }
        return pState;
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (!Config.spongeBlockUpdate)
            SpongeHandler.unSuppressTicks(pLevel, pPos, Config.spongeRange);
    }

    @Override
    protected void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        if(pLevel instanceof ServerLevel serverLevel) {
            doCleanup(serverLevel, pPos);
            SpongeHandler.doSuppressTicks(serverLevel, pPos, Config.spongeRange + 1);
        }
        pLevel.scheduleTick(pPos, this, TICK_RATE + pLevel.random.nextInt(5));
    }

    @Override
    protected void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        doCleanup(pLevel, pPos);
        SpongeHandler.doSuppressTicks(pLevel, pPos, Config.spongeRange + 1);
        pLevel.scheduleTick(pPos, this, TICK_RATE + pLevel.random.nextInt(5));
    }

    private void doCleanup(ServerLevel level, BlockPos pos) {
        SpongeHandler.CleanupResult result = SpongeHandler.doCleanup(level, pos, Config.spongeRange, tag, burnTag, Config.spongeBlockUpdate);
        if(result.burn())
            level.blockEvent(pos, this, EVENT_BURN, 0);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level world, BlockPos pos, int eventId, int eventParam) {
        if (eventId == EVENT_BURN) {
            if (world.isClientSide) {
                for (int i = 0; i < 20; i++) {
                    double px = pos.getX() + world.random.nextDouble() * 0.1;
                    double py = pos.getY() + 1.0 + world.random.nextDouble();
                    double pz = pos.getZ() + world.random.nextDouble();
                    world.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, 0, 0, 0);
                }
            } else {
                world.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
            }
            return true;
        }
        return super.triggerEvent(state, world, pos, eventId, eventParam);
    }
}