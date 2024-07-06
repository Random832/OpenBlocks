package openblocks.lib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.properties.Property;
import openblocks.lib.geometry.LocalDirections;
import openblocks.lib.geometry.Orientation;

import java.util.Set;

public interface ILegacyBlockRotationMode {

    Property<Orientation> getProperty();

    int getMask();

    Orientation fromValue(int value);

    int toValue(Orientation dir);

    boolean isOrientationValid(Orientation dir);

    Set<Orientation> getValidDirections();

    Orientation getOrientationFacing(Direction side);

    // per Minecraft convention, front should be same as placement side - unless not possible, where it's on the same axis
    Direction getFront(Orientation orientation);

    // When front ='north', top should be 'up'. Also, for most modes for n|s|w|e top = 'up'
    Direction getTop(Orientation orientation);

    LocalDirections getLocalDirections(Orientation orientation);

    Orientation getPlacementOrientationFromEntity(BlockPos pos, LivingEntity player);

    boolean toolRotationAllowed();

    Direction[] getToolRotationAxes();

    Orientation calculateToolRotation(Orientation currentOrientation, Direction axis);

}