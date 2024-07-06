package openblocks.lib.block;

import net.minecraft.core.Direction;

class RotationAxis {
    public static final Direction[] NO_AXIS = {};
    public static final Direction[] SINGLE_AXIS = { Direction.UP, Direction.DOWN };
    public static final Direction[] THREE_AXIS = Direction.values();
}