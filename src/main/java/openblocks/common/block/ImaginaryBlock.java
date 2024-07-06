package openblocks.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.registries.DeferredHolder;
import openblocks.OpenBlocks;
import openblocks.common.blockentity.ImaginaryBlockEntity;
import openblocks.lib.block.OpenEntityBlock;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ImaginaryBlock extends OpenEntityBlock<ImaginaryBlockEntity> {
    private final Map<BlockState, VoxelShape> shapeCache = new HashMap<>();

    // we need this during registration
    static Supplier<SoundEvent> SOUND_CRAYON_PLACE = DeferredHolder.create(ResourceKey.create(Registries.SOUND_EVENT, OpenBlocks.modLoc("crayon.place")));

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public static Properties makeProperties() {
        SoundType soundType = new DeferredSoundType(0.5f, 1f,
                SOUND_CRAYON_PLACE,
                SOUND_CRAYON_PLACE,
                SOUND_CRAYON_PLACE,
                SOUND_CRAYON_PLACE,
                SOUND_CRAYON_PLACE);
        return Properties.of()
                .isViewBlocking((pState, pLevel, pPos) -> false)
                .noOcclusion()
                .dynamicShape()
                .sound(soundType)
                .mapColor(MapColor.NONE)
                .isSuffocating((pState, pLevel, pPos) -> false)
                .noCollission()
                .noLootTable()
                .forceSolidOff();
    }

    public ImaginaryBlock(Properties properties) {
        super(properties, OpenBlocks.IMAGINARY_BE);
        for (BlockState state : getStateDefinition().getPossibleStates()) {
            shapeCache.put(state, computeShape(state));
        }
    }

    protected VoxelShape computeShape(BlockState state) {
        return Block.box(0,0,0, 16,16,16);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // TODO waterlogging
    }

    private boolean canInteract(BlockGetter pLevel, BlockPos pPos, CollisionContext context) {
        if(context == CollisionContext.empty()) {
            return true; // crash bug with particles
        }
        if(context instanceof EntityCollisionContext ec && ec.getEntity() instanceof LivingEntity le) {
            @Nullable ImaginaryBlockEntity be = getBlockEntity(pLevel, pPos);
            if(be != null)
                return be.is(ImaginaryBlockEntity.Property.SELECTABLE, le);
            return false;
        }
        return false;
    }

    private boolean canCollide(BlockGetter pLevel, BlockPos pPos, CollisionContext context) {
        @Nullable ImaginaryBlockEntity be = getBlockEntity(pLevel, pPos);
        if(context instanceof EntityCollisionContext ec && ec.getEntity() instanceof LivingEntity le) {
            if(be != null)
                return be.is(ImaginaryBlockEntity.Property.SOLID, le);
            return false;
        }
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return canInteract(pLevel, pPos, pContext) ? shapeCache.get(pState) : Shapes.empty();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return canCollide(pLevel, pPos, pContext) ? shapeCache.get(pState) : Shapes.empty();
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return Shapes.empty();
    }

    public int getDurabilityCost() {
        return 4;
    }

    public static class Stair extends ImaginaryBlock {
        public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

        public Stair(Properties properties) {
            super(properties);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(FACING);
        }

        @Override
        public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
            return defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
        }

        @Override
        protected VoxelShape computeShape(BlockState state) {
            //noinspection DataFlowIssue
            return Blocks.STONE_STAIRS.defaultBlockState().setValue(FACING, state.getValue(FACING)).getShape(null, null);
        }

        @Override
        public int getDurabilityCost() {
            return 3;
        }
    }

    public static class Panel extends ImaginaryBlock {
        public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

        public Panel(Properties properties) {
            super(properties);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(HALF);
        }

        @Override
        public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
            BlockPos pos = pContext.getClickedPos();
            Direction direction = pContext.getClickedFace();
            return this.defaultBlockState()
                    .setValue(HALF, direction != Direction.DOWN && (direction == Direction.UP || !(pContext.getClickLocation().y - (double)pos.getY() > 0.5))
                        ? Half.BOTTOM
                        : Half.TOP);
            }

        @Override
        protected VoxelShape computeShape(BlockState state) {
            if(state.getValue(HALF) == Half.TOP)
                return Block.box(0, 8, 0, 16, 16, 16);
            return Block.box(0, 0, 0, 16, 8, 16);
        }

        @Override
        public int getDurabilityCost() {
            return 2;
        }
    }
}
