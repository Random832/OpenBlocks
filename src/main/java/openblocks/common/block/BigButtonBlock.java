package openblocks.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

public class BigButtonBlock extends ButtonBlock {
    Map<BlockState, VoxelShape> shapeMap = new HashMap<>();

    public BigButtonBlock(BlockSetType type, int pressTick, Properties props) {
        super(type, pressTick, props);
    }

    private VoxelShape calculateShape(BlockState state) {
        BlockState button = Blocks.OAK_BUTTON.withPropertiesOf(state);
        VoxelShape shape = button.getShape(null, BlockPos.ZERO);
        AABB aabb = shape.bounds();
        double ominX = aabb.minX;
        double omaxX = aabb.maxX;
        double ominY = aabb.minY;
        double omaxY = aabb.maxY;
        double ominZ = aabb.minZ;
        double omaxZ = aabb.maxZ;
        double minX = mapBound(ominX, ominX, omaxX);
        double maxX = mapBound(omaxX, ominX, omaxX);
        double minY = mapBound(ominY, ominY, omaxY);
        double maxY = mapBound(omaxY, ominY, omaxY);
        double minZ = mapBound(ominZ, ominZ, omaxZ);
        double maxZ = mapBound(omaxZ, ominZ, omaxZ);
        return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static double mapBound(double value, double omin, double omax) {
        if(omin == 0 || omax == 1) {
            return value; // we're not changing the button thickness
        }

        return value < 0.5 ? 0.0625f : 0.9375f;
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return shapeMap.computeIfAbsent(pState, this::calculateShape);
    }
}
