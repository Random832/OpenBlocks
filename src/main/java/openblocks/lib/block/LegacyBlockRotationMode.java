package openblocks.lib.block;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import openblocks.lib.geometry.HalfAxis;
import openblocks.lib.geometry.LocalDirections;
import openblocks.lib.geometry.Orientation;
import openblocks.lib.utils.BlockUtils;

import java.util.Set;

public enum LegacyBlockRotationMode implements ILegacyBlockRotationMode {
    /**
     * No rotations - always oriented by world directions
     */
    NONE(RotationAxis.NO_AXIS, Orientation.XP_YP) {
        @Override
        public boolean isOrientationValid(Orientation dir) {
            return true;
        }

        @Override
        public Orientation fromValue(int value) {
            return Orientation.XP_YP;
        }

        @Override
        public int toValue(Orientation dir) {
            return 0;
        }

        @Override
        public Orientation getOrientationFacing(Direction side) {
            return Orientation.XP_YP;
        }

        @Override
        public Orientation getPlacementOrientationFromEntity(BlockPos pos, LivingEntity player) {
            return Orientation.XP_YP;
        }

        @Override
        public boolean toolRotationAllowed() {
            return false;
        }

        @Override
        public Orientation calculateToolRotation(Orientation currentOrientation, Direction axis) {
            return null;
        }

        @Override
        public Direction getFront(Orientation orientation) {
            return Direction.NORTH;
        }

        @Override
        public Direction getTop(Orientation orientation) {
            return Direction.UP;
        }

    },
    /**
     * Two orientations - either N-S or W-E. Top side remains unchanged.
     * Placement side will become local north or south.
     * Tool rotation will either rotate around Y (if clicked T or B) or set to clicked side (otherwise).
     */
    TWO_DIRECTIONS(RotationAxis.THREE_AXIS, Orientation.ZN_YP, Orientation.XP_YP) {
        @Override
        public Orientation getOrientationFacing(Direction side) {
            switch (side) {
                case EAST:
                case WEST:
                    return Orientation.ZN_YP;
                case NORTH:
                case SOUTH:
                    return Orientation.XP_YP;
                default:
                    return null;
            }
        }

        @Override
        public Orientation getPlacementOrientationFromEntity(BlockPos pos, LivingEntity player) {
            return getOrientationFacing(player.getDirection());
        }

        @Override
        public Orientation calculateToolRotation(Orientation currentOrientation, Direction axis) {
            switch (axis) {
                case UP:
                case DOWN:
                    return (currentOrientation == Orientation.ZN_YP)? Orientation.XP_YP : Orientation.ZN_YP;
                case NORTH:
                case SOUTH:
                case EAST:
                case WEST:
                    return getOrientationFacing(axis);
                default:
                    return null;
            }
        }

        @Override
        public Direction getFront(Orientation orientation) {
            return orientation.north();
        }

        @Override
        public Direction getTop(Orientation orientation) {
            return orientation.up();
        }
    },
    /**
     * Three orientations: N-S, W-E, T-B.
     * Placement side will become local top or bottom.
     * Tool rotation will set top direction to clicked side.
     */
    THREE_DIRECTIONS(RotationAxis.THREE_AXIS, Orientation.XP_YP, Orientation.ZN_XN, Orientation.XP_ZN) {
        @Override
        public Orientation getOrientationFacing(Direction side) {
            switch (side) {
                case EAST:
                case WEST:
                    return Orientation.ZN_XN;
                case NORTH:
                case SOUTH:
                    return Orientation.XP_ZN;
                case UP:
                case DOWN:
                default:
                    return Orientation.XP_YP;
            }
        }

        @Override
        public Orientation getPlacementOrientationFromEntity(BlockPos pos, LivingEntity player) {
            final Direction normalDir = BlockUtils.get3dOrientation(player, pos);
            return getOrientationFacing(normalDir);
        }

        @Override
        public Orientation calculateToolRotation(Orientation currentOrientation, Direction axis) {
            return getOrientationFacing(axis);
        }

        @Override
        public Direction getFront(Orientation orientation) {
            return orientation.up();
        }

        @Override
        public Direction getTop(Orientation orientation) {
            return orientation.south();
        }
    },
    /**
     * Rotate around Y in for directions: N,S,W,E.
     * Placement side will become local north.
     * Tool rotation will either rotate around Y (if clicked T or B) or set to clicked side (otherwise).
     */
    FOUR_DIRECTIONS(RotationAxis.THREE_AXIS, Orientation.XP_YP, Orientation.ZN_YP, Orientation.XN_YP, Orientation.ZP_YP) {
        @Override
        public Orientation getOrientationFacing(Direction side) {
            switch (side) {
                case SOUTH:
                    return Orientation.XN_YP;
                case WEST:
                    return Orientation.ZN_YP;
                case NORTH:
                    return Orientation.XP_YP;
                case EAST:
                    return Orientation.ZP_YP;
                default:
                    return null;
            }
        }

        @Override
        public Orientation getPlacementOrientationFromEntity(BlockPos pos, LivingEntity player) {
            return getOrientationFacing(player.getDirection().getOpposite());
        }

        @Override
        public Orientation calculateToolRotation(Orientation currentOrientation, Direction axis) {
            switch (axis) {
                case UP:
                    return currentOrientation.rotateAround(HalfAxis.POS_Y);
                case DOWN:
                    return currentOrientation.rotateAround(HalfAxis.NEG_Y);
                case NORTH:
                case SOUTH:
                case EAST:
                case WEST: {
                    final Direction currentFront = getFront(currentOrientation);
                    final Direction target = (currentFront == axis)? axis.getOpposite() : axis;
                    return getOrientationFacing(target);
                }
                default:
                    return null;
            }
        }

        @Override
        public Direction getFront(Orientation orientation) {
            return orientation.north();
        }

        @Override
        public Direction getTop(Orientation orientation) {
            return orientation.up();
        }
    },
    /**
     * Rotations in every cardinal direction.
     * Horizontal directions are created by rotating over X to get north, and then rotating around Y (so local north/-z will always be down).
     * Placement side will become local bottom.
     * Tool rotation will set top to clicked side.
     */
    SIX_DIRECTIONS(RotationAxis.THREE_AXIS, Orientation.XP_YN, Orientation.XP_YP, Orientation.XP_ZN, Orientation.XN_ZP, Orientation.ZN_XN, Orientation.ZP_XP) {
        @Override
        public Orientation getOrientationFacing(Direction side) {
            switch (side) {
                case DOWN:
                    return Orientation.XP_YN;
                case EAST:
                    return Orientation.ZP_XP;
                case NORTH:
                    return Orientation.XP_ZN;
                case SOUTH:
                    return Orientation.XN_ZP;
                case WEST:
                    return Orientation.ZN_XN;
                case UP:
                default:
                    return Orientation.XP_YP;
            }
        }

        @Override
        public Orientation getPlacementOrientationFromEntity(BlockPos pos, LivingEntity player) {
            return getOrientationFacing(BlockUtils.get3dOrientation(player, pos).getOpposite());
        }

        @Override
        public Orientation calculateToolRotation(Orientation currentOrientation, Direction axis) {
            final Direction currentFront = getFront(currentOrientation);
            final Direction target = (currentFront == axis)? axis.getOpposite() : axis;
            return getOrientationFacing(target);
        }

        @Override
        public Direction getFront(Orientation orientation) {
            return orientation.up();
        }

        @Override
        public Direction getTop(Orientation orientation) {
            return orientation.south();
        }
    },
    /**
     * And now it's time for weird ones...
     * Three orientations: N-S, W-E, T-B.
     * Placement side will become local top or bottom.
     * Side can be rotated in four directions
     */
    THREE_FOUR_DIRECTIONS(RotationAxis.THREE_AXIS,
            Orientation.XP_YP, Orientation.XN_YP, Orientation.ZP_YP, Orientation.ZN_YP,
            Orientation.YP_XN, Orientation.YN_XN, Orientation.ZP_XN, Orientation.ZN_XN,
            Orientation.XP_ZN, Orientation.XN_ZN, Orientation.YP_ZN, Orientation.YN_ZN) {

        @Override
        public Orientation getOrientationFacing(Direction side) {
            switch (side) {
                case EAST:
                case WEST:
                    return Orientation.ZN_XN;
                case NORTH:
                case SOUTH:
                    return Orientation.XP_ZN;
                case UP:
                case DOWN:
                default:
                    return Orientation.XP_YP;
            }
        }

        @Override
        public Orientation getPlacementOrientationFromEntity(BlockPos pos, LivingEntity player) {
            return getOrientationFacing(BlockUtils.get3dOrientation(player, pos));
        }

        @Override
        public Orientation calculateToolRotation(Orientation currentOrientation, Direction axis) {
            final HalfAxis newTop = HalfAxis.fromDirection(axis);
            final HalfAxis currentTop = currentOrientation.y;

            if (newTop == currentTop) {
                return currentOrientation.rotateAround(HalfAxis.POS_Y);
            } else if (newTop == currentTop.negate()) {
                return currentOrientation.rotateAround(HalfAxis.NEG_Y);
            } else {
                return getOrientationFacing(axis);
            }
        }

        @Override
        public Direction getFront(Orientation orientation) {
            return orientation.up();
        }

        @Override
        public Direction getTop(Orientation orientation) {
            return orientation.south();
        }
    },
    /**
     * Yet another weird one.
     * Top side can rotate when oriented up or down.
     * When top points to cardinal direction, texture top should always align with horizon
     */
    TWELVE_DIRECTIONS(RotationAxis.THREE_AXIS,
            Orientation.lookupYZ(HalfAxis.NEG_Y, HalfAxis.NEG_Z), // first two TOP/BOTTOM orientation are here for easy migration from SIX_DIRECTIONS
            Orientation.lookupYZ(HalfAxis.POS_Y, HalfAxis.POS_Z),

            Orientation.lookupYZ(HalfAxis.NEG_Z, HalfAxis.POS_Y),
            Orientation.lookupYZ(HalfAxis.POS_Z, HalfAxis.POS_Y),
            Orientation.lookupYZ(HalfAxis.NEG_X, HalfAxis.POS_Y),
            Orientation.lookupYZ(HalfAxis.POS_X, HalfAxis.POS_Y),

            Orientation.lookupYZ(HalfAxis.NEG_Y, HalfAxis.POS_Z),
            Orientation.lookupYZ(HalfAxis.NEG_Y, HalfAxis.NEG_X),
            Orientation.lookupYZ(HalfAxis.NEG_Y, HalfAxis.POS_X),

            Orientation.lookupYZ(HalfAxis.POS_Y, HalfAxis.NEG_Z),
            Orientation.lookupYZ(HalfAxis.POS_Y, HalfAxis.POS_X),
            Orientation.lookupYZ(HalfAxis.POS_Y, HalfAxis.NEG_X)) {

        public Orientation directionToOrientation(Direction localTop) {
            switch (localTop) {
                case DOWN:
                    return Orientation.lookupYZ(HalfAxis.NEG_Y, HalfAxis.NEG_Z);
                case EAST:
                    return Orientation.lookupYZ(HalfAxis.POS_X, HalfAxis.POS_Y);
                case NORTH:
                    return Orientation.lookupYZ(HalfAxis.NEG_Z, HalfAxis.POS_Y);
                case SOUTH:
                    return Orientation.lookupYZ(HalfAxis.POS_Z, HalfAxis.POS_Y);
                case WEST:
                    return Orientation.lookupYZ(HalfAxis.NEG_X, HalfAxis.POS_Y);
                case UP:
                default:
                    return Orientation.lookupYZ(HalfAxis.POS_Y, HalfAxis.POS_Z);
            }
        }

        @Override
        public Orientation getOrientationFacing(Direction side) {
            return directionToOrientation(side);
        }

        @Override
        public Orientation getPlacementOrientationFromEntity(BlockPos pos, LivingEntity player) {
            final Direction player3d = BlockUtils.get3dOrientation(player, pos).getOpposite();
            if (player3d.equals(Direction.UP)) {
                final Direction player2d = player.getDirection().getOpposite();
                return Orientation.lookupYZ(HalfAxis.POS_Y, HalfAxis.fromDirection(player2d));
            } else if (player3d.equals(Direction.DOWN)) {
                final Direction player2d = player.getDirection().getOpposite();
                return Orientation.lookupYZ(HalfAxis.NEG_Y, HalfAxis.fromDirection(player2d));
            } else {
                return Orientation.lookupYZ(HalfAxis.fromDirection(player3d), HalfAxis.POS_Y);
            }
        }

        @Override
        public Orientation calculateToolRotation(Orientation currentOrientation, Direction axis) {
            switch (axis) {
                case NORTH:
                case SOUTH:
                case EAST:
                case WEST: {
                    final Direction currentFront = getFront(currentOrientation);
                    final Direction target = (currentFront == axis)? axis.getOpposite() : axis;
                    return Orientation.lookupYZ(HalfAxis.fromDirection(target), HalfAxis.POS_Y);
                }
                case UP:
                    if (currentOrientation.y != HalfAxis.POS_Y) return Orientation.lookupYZ(HalfAxis.POS_Y, HalfAxis.POS_Z);
                    else return currentOrientation.rotateAround(HalfAxis.POS_Y);
                case DOWN:
                    if (currentOrientation.y != HalfAxis.NEG_Y) return Orientation.lookupYZ(HalfAxis.NEG_Y, HalfAxis.NEG_Z);
                    else return currentOrientation.rotateAround(HalfAxis.POS_Y);
                default:
                    return null;
            }
        }

        @Override
        public Direction getFront(Orientation orientation) {
            return orientation.up();
        }

        @Override
        public Direction getTop(Orientation orientation) {
            return orientation.south();
        }
    };

    private static final int MAX_ORIENTATIONS = 16;

    private LegacyBlockRotationMode(Direction[] rotations, Orientation... validOrientations) {
        this.rotationAxes = rotations;
        this.validDirections = ImmutableSet.copyOf(validOrientations);

        final int count = validOrientations.length;

        Preconditions.checkArgument(this.validDirections.size() == count, "Duplicated directions");
        Preconditions.checkArgument(count <= MAX_ORIENTATIONS, "Too many values: %s", count);

        this.property = EnumProperty.create("orientation", Orientation.class, validDirections);

        this.idToOrientation = new Orientation[MAX_ORIENTATIONS];
        this.orientationToId = new int[Orientation.VALUES.length];

        for (int i = 0; i < count; i++) {
            final Orientation orientation = validOrientations[i];
            Preconditions.checkNotNull(orientation);
            idToOrientation[i] = orientation;
            orientationToId[orientation.ordinal()] = i;
        }

        if (count == 0) {
            this.bitCount = 0;
            this.mask = 0;
        } else {
            final int maxValue = count - 1;
            this.bitCount = Integer.SIZE - Integer.numberOfLeadingZeros(maxValue);
            this.mask = (1 << bitCount) - 1;

            for (int i = count; i < idToOrientation.length; i++)
                idToOrientation[i] = idToOrientation[0];
        }
    }

    private final Orientation[] idToOrientation;

    private final int[] orientationToId;

    private final Direction[] rotationAxes;

    private final Set<Orientation> validDirections;

    private final int bitCount;

    private final int mask;

    private final EnumProperty<Orientation> property;

    @Override
    public Orientation fromValue(int value) {
        try {
            return idToOrientation[value];
        } catch (IndexOutOfBoundsException e) {
            return idToOrientation[0];
        }
    }

    @Override
    public int toValue(Orientation dir) {
        try {
            return orientationToId[dir.ordinal()];
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }
    }

    @Override
    public boolean isOrientationValid(Orientation dir) {
        return validDirections.contains(dir);
    }

    @Override
    public boolean toolRotationAllowed() {
        return true;
    }

    @Override
    public LocalDirections getLocalDirections(Orientation orientation) {
        return LocalDirections.fromFrontAndTop(getFront(orientation), getTop(orientation));
    }

    @Override
    public Property<Orientation> getProperty() {
        return property;
    }

    @Override
    public int getMask() {
        return mask;
    }

    @Override
    public Direction[] getToolRotationAxes() {
        return rotationAxes;
    }

    @Override
    public Set<Orientation> getValidDirections() {
        return validDirections;
    }
}