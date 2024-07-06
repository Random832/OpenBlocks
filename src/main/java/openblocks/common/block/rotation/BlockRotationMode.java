package openblocks.common.block.rotation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import openblocks.lib.block.LegacyBlockRotationMode;
import openblocks.lib.geometry.HalfAxis;
import openblocks.lib.geometry.Orientation;

public abstract class BlockRotationMode {

    public abstract BlockState getStateForPlacement(BlockState state, BlockPos clickedPos, Player player);

    public abstract void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder);

    public abstract BlockState rotateY(BlockState state, Rotation rotation);

    public abstract BlockState mirror(BlockState state, Mirror mirror);

    public abstract BlockState toolRotate(BlockState state, Direction clickedFace);

    public abstract Orientation getOrientation(BlockState state);

    public static final BlockRotationMode NONE = new BlockRotationMode() {
        @Override
        public BlockState getStateForPlacement(BlockState state, BlockPos clickedPos, Player player) {
            return state;
        }

        @Override
        public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        }

        @Override
        public BlockState rotateY(BlockState state, Rotation rotation) {
            return state;
        }

        @Override
        public BlockState mirror(BlockState state, Mirror mirror) {
            return state;
        }

        @Override
        public BlockState toolRotate(BlockState state, Direction clickedFace) {
            return state;
        }

        @Override
        public Orientation getOrientation(BlockState state) {
            return Orientation.XP_YP;
        }
    };

    private static abstract class Directional extends BlockRotationMode {
        protected final DirectionProperty property;

        Directional(DirectionProperty property) {
            this.property = property;
        }

        @Override
        public BlockState getStateForPlacement(BlockState state, BlockPos clickedPos, Player player) {
            return state.setValue(property, player.getDirection().getOpposite());
        }

        @Override
        public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(property);
        }

        @Override
        public BlockState rotateY(BlockState state, Rotation rotation) {
            return state.setValue(property, rotation.rotate(state.getValue(property)));
        }

        @Override
        public BlockState mirror(BlockState state, Mirror mirror) {
            return rotateY(state, mirror.getRotation(state.getValue(property)));
        }
    }

    public static final BlockRotationMode FOUR_DIRECTION = new Directional(BlockStateProperties.HORIZONTAL_FACING) {
        @Override
        public BlockState toolRotate(BlockState state, Direction clickedFace) {
            return switch (clickedFace) {
                // TODO verify correct direction
                case UP -> rotateY(state, Rotation.CLOCKWISE_90);
                case DOWN -> rotateY(state, Rotation.COUNTERCLOCKWISE_90);
                default -> state.setValue(property, clickedFace);
            };
        }

        @Override
        public Orientation getOrientation(BlockState state) {
            return LegacyBlockRotationMode.FOUR_DIRECTIONS.getOrientationFacing(state.getValue(property));
        }
    };


    public static final BlockRotationMode SIX_DIRECTIONS = new Directional(BlockStateProperties.FACING) {
        @Override
        public BlockState toolRotate(BlockState state, Direction clickedFace) {
            return state.setValue(property, clickedFace);
        }

        @Override
        public Orientation getOrientation(BlockState state) {
            return LegacyBlockRotationMode.SIX_DIRECTIONS.getOrientationFacing(state.getValue(property));
        }
    };


    public static final BlockRotationMode THREE_FOUR_DIRECTIONS = new LegacyAdapter(LegacyBlockRotationMode.THREE_FOUR_DIRECTIONS);

    private static class LegacyAdapter extends BlockRotationMode {
        private final LegacyBlockRotationMode legacyMode;
        private final Property<Orientation> property;

        public LegacyAdapter(LegacyBlockRotationMode legacyMode) {
            super();
            this.legacyMode = legacyMode;
            this.property = legacyMode.getProperty();
        }

        @Override
        public BlockState getStateForPlacement(BlockState state, BlockPos clickedPos, Player player) {
            return state.setValue(property, legacyMode.getPlacementOrientationFromEntity(clickedPos, player));
        }

        @Override
        public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(property);
        }

        @Override
        public BlockState rotateY(BlockState state, Rotation rotation) {
            Orientation newOrientation = switch (rotation) {
                case NONE -> state.getValue(property);
                case CLOCKWISE_90 -> state.getValue(property).rotateAround(HalfAxis.POS_Y);
                case CLOCKWISE_180 -> state.getValue(property).rotateAround(HalfAxis.POS_Y).rotateAround(HalfAxis.POS_Y);
                case COUNTERCLOCKWISE_90 -> state.getValue(property).rotateAround(HalfAxis.NEG_Y);
            };
            if(property.getPossibleValues().contains(newOrientation))
                return state.setValue(property, newOrientation);
            return state;
        }

        @Override
        public BlockState mirror(BlockState state, Mirror mirror) {
            Direction front = legacyMode.getLocalDirections(state.getValue(property)).front;
            return rotateY(state, mirror.getRotation(front));
        }

        @Override
        public BlockState toolRotate(BlockState state, Direction clickedFace) {
            Orientation newOrientation = legacyMode.calculateToolRotation(state.getValue(property), clickedFace);
            return state.setValue(property, newOrientation);
        }

        @Override
        public Orientation getOrientation(BlockState state) {
            return state.getValue(property);
        }
    }
}