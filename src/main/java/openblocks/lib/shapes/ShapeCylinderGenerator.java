package openblocks.lib.shapes;

import openblocks.lib.shapes.DefaultShapeGenerator;
import openblocks.lib.shapes.IShapeable;
import openblocks.lib.utils.render.GeometryUtils;
import openblocks.lib.utils.render.GeometryUtils.Quadrant;

import java.util.Set;

public class ShapeCylinderGenerator extends DefaultShapeGenerator {

    private final Set<Quadrant> quadrants;

    public ShapeCylinderGenerator() {
        this(Quadrant.ALL);
    }

    public ShapeCylinderGenerator(Set<Quadrant> quadrants) {
        this.quadrants = quadrants;
    }

    @Override
    public void generateShape(int minX, final int minY, int minZ, int maxX, final int maxY, int maxZ, final IShapeable shapeable) {
        GeometryUtils.makeEllipse(minX, minZ, maxX, maxZ, 0, (x, ignore, z) -> {
            for (int y = minY; y <= maxY; y++)
                shapeable.setBlock(x, y, z);
        }, quadrants);
    }

}