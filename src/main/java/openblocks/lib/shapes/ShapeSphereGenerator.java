package openblocks.lib.shapes;

import java.util.Set;
import openblocks.lib.utils.render.GeometryUtils;
import openblocks.lib.utils.render.GeometryUtils.Octant;

public class ShapeSphereGenerator extends DefaultShapeGenerator {

    private final Set<Octant> octants;

    public ShapeSphereGenerator() {
        this(Octant.ALL);
    }

    public ShapeSphereGenerator(Set<Octant> octants) {
        this.octants = octants;
    }

    @Override
    public void generateShape(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, IShapeable shapeable) {
        GeometryUtils.makeEllipsoid(minX, minY, minZ, maxX, maxY, maxZ, shapeable, octants);
    }
}