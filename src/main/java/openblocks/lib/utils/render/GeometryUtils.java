package openblocks.lib.utils.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import openblocks.lib.shapes.IShapeable;
import openblocks.lib.utils.Coord;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class GeometryUtils {

    public enum Axis {
        X(1, 0, 0, Direction.EAST, Direction.WEST),
        Y(0, 1, 0, Direction.UP, Direction.DOWN),
        Z(0, 0, 1, Direction.SOUTH, Direction.NORTH);
        public final int dx;
        public final int dy;
        public final int dz;

        public final Direction positive;
        public final Direction negative;

        Axis(int dx, int dy, int dz, Direction positive, Direction negative) {
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
            this.positive = positive;
            this.negative = negative;
        }
    }

    public enum Octant {
        TopSouthWest("Top South West", Direction.WEST, Direction.UP, Direction.SOUTH),
        TopNorthEast("Top North East", Direction.EAST, Direction.UP, Direction.NORTH),
        TopNorthWest("Top North West", Direction.WEST, Direction.UP, Direction.NORTH),
        TopSouthEast("Top South East", Direction.EAST, Direction.UP, Direction.SOUTH),
        BottomSouthWest("Bottom South West", Direction.WEST, Direction.DOWN, Direction.SOUTH),
        BottomNorthEast("Bottom North East", Direction.EAST, Direction.DOWN, Direction.NORTH),
        BottomNorthWest("Bottom North West", Direction.WEST, Direction.DOWN, Direction.NORTH),
        BottomSouthEast("Bottom South East", Direction.EAST, Direction.DOWN, Direction.SOUTH);

        public static final EnumSet<Octant> ALL = EnumSet.allOf(Octant.class);
        public static final EnumSet<Octant> TOP = select(Direction.UP);
        public static final EnumSet<Octant> BOTTOM = select(Direction.DOWN);
        public static final EnumSet<Octant> NORTH = select(Direction.NORTH);
        public static final EnumSet<Octant> SOUTH = select(Direction.SOUTH);
        public static final EnumSet<Octant> EAST = select(Direction.EAST);
        public static final EnumSet<Octant> WEST = select(Direction.WEST);

        public final EnumSet<Direction> dirs;
        public final int x, y, z;
        public final String name;

        public int getXOffset() {
            return x;
        }

        public int getYOffset() {
            return y;
        }

        public int getZOffset() {
            return z;
        }

        public String getFriendlyName() {
            return name;
        }

        Octant(String friendlyName, Direction dirX, Direction dirY, Direction dirZ) {
            this.x = dirX.getStepX() + dirY.getStepX() + dirZ.getStepX();
            this.y = dirX.getStepY() + dirY.getStepY() + dirZ.getStepY();
            this.z = dirX.getStepZ() + dirY.getStepZ() + dirZ.getStepZ();
            this.dirs = EnumSet.of(dirX, dirY, dirZ);
            this.name = friendlyName;
        }

        private static EnumSet<Octant> select(Direction dir) {
            Set<Octant> result = Sets.newIdentityHashSet();
            for (Octant o : values())
                if (o.dirs.contains(dir))
                    result.add(o);

            return EnumSet.copyOf(result);
        }
    }

    public enum Quadrant {
        TopSouthWest(-1, 1),
        TopNorthEast(1, -1),
        TopNorthWest(-1, -1),
        TopSouthEast(1, 1);

        public static final EnumSet<Quadrant> ALL = EnumSet.allOf(Quadrant.class);

        public final int x;
        public final int z;

        Quadrant(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

    public static void makeLine(int startX, int startY, int startZ, Axis axis, int length, IShapeable shapeable) {
        makeLine(startX, startY, startZ, axis.positive, length, shapeable);
    }

    /**
     * Makes a link of blocks in a shape
     */
    public static void makeLine(int startX, int startY, int startZ, Direction direction, int length, IShapeable shapeable) {
        if (length < 0) return;
        Vec3i v = direction.getNormal();
        for (int offset = 0; offset <= length; offset++)
            // Create a line in the direction of direction, length in size
            shapeable.setBlock(
                    startX + (offset * v.getX()),
                    startY + (offset * v.getY()),
                    startZ + (offset * v.getZ()));

    }

    public static void makePlane(int startX, int startY, int startZ, int width, int height, Axis right, Axis up, IShapeable shapeable) {
        makePlane(startX, startY, startZ, width, height, right.positive, up.positive, shapeable);
    }

    /**
     * Makes a flat plane along two directions
     */
    public static void makePlane(int startX, int startY, int startZ, int width, int height, Direction right, Direction up, IShapeable shapeable) {
        if (width < 0 || height < 0) return;
        int lineOffsetX, lineOffsetY, lineOffsetZ;
        // We offset each line by up, and then apply it right

        final Vec3i v = up.getNormal();
        for (int h = 0; h <= height; h++) {
            lineOffsetX = startX + (h * v.getX());
            lineOffsetY = startY + (h * v.getY());
            lineOffsetZ = startZ + (h * v.getZ());
            makeLine(lineOffsetX, lineOffsetY, lineOffsetZ, right, width, shapeable);
        }
    }

    @Deprecated
    public static void makeSphere(int radiusX, int radiusY, int radiusZ, IShapeable shapeable, EnumSet<Octant> octants) {
        makeEllipsoid(radiusX, radiusY, radiusZ, shapeable, octants);
    }

    public static void makeEllipsoid(int radiusX, int radiusY, int radiusZ, IShapeable shapeable, Set<Octant> octants) {
        final List<Octant> octantsList = ImmutableList.copyOf(octants);

        final double invRadiusX = 1.0 / (radiusX + 0.5);
        final double invRadiusY = 1.0 / (radiusY + 0.5);
        final double invRadiusZ = 1.0 / (radiusZ + 0.5);

        double nextXn = 0;
        forX:
        for (int x = 0; x <= radiusX; ++x) {
            final double xn = nextXn;
            nextXn += invRadiusX;
            double nextYn = 0;
            forY:
            for (int y = 0; y <= radiusY; ++y) {
                final double yn = nextYn;
                nextYn += invRadiusY;
                double nextZn = 0;
                forZ:
                for (int z = 0; z <= radiusZ; ++z) {
                    final double zn = nextZn;
                    nextZn += invRadiusZ;

                    double distanceSq = Mth.lengthSquared(xn, yn, zn);
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break forZ;
                    }

                    if (Mth.lengthSquared(nextXn, yn, zn) <= 1
                            && Mth.lengthSquared(xn, nextYn, zn) <= 1
                            && Mth.lengthSquared(xn, yn, nextZn) <= 1) {
                        continue;
                    }

                    for (Octant octant : octantsList)
                        shapeable.setBlock(x * octant.x, y * octant.y, z * octant.z);
                }
            }
        }
    }

    public static void makeEllipsoid(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, IShapeable shapeable, Set<Octant> octants) {
        {
            final int centerX = (minX + maxX) / 2;
            final int centerY = (minY + maxY) / 2;
            final int centerZ = (minZ + maxZ) / 2;

            final IShapeable prevShapeable = shapeable;
            shapeable = (x, y, z) -> prevShapeable.setBlock(centerX + x, centerY + y, centerZ + z);
        }

        final int radiusX;
        final int radiusY;
        final int radiusZ;

        // Cutting middle of shape == terrible hack. No idea if it works in any case
        // Anyone now better algorithm for ellipsoids with non-integer axis?
        {
            final int diffX = maxX - minX;
            if ((diffX & 1) == 0) {
                radiusX = diffX / 2;
            } else {
                radiusX = diffX / 2 + 1;
                shapeable = skipMiddleX(shapeable);
            }
        }

        {
            final int diffY = maxY - minY;
            if ((diffY & 1) == 0) {
                radiusY = diffY / 2;
            } else {
                radiusY = diffY / 2 + 1;
                shapeable = skipMiddleY(shapeable);
            }
        }

        {
            final int diffZ = maxZ - minZ;
            if ((diffZ & 1) == 0) {
                radiusZ = diffZ / 2;
            } else {
                radiusZ = diffZ / 2 + 1;
                shapeable = skipMiddleZ(shapeable);
            }
        }

        makeEllipsoid(radiusX, radiusY, radiusZ, shapeable, octants);
    }

    public static void makeEllipse(int radiusX, int radiusZ, int y, IShapeable shapeable, Set<Quadrant> quadrants) {
        final double invRadiusX = 1.0 / (radiusX + 0.5);
        final double invRadiusZ = 1.0 / (radiusZ + 0.5);

        final List<Quadrant> quadrantsList = ImmutableList.copyOf(quadrants);

        double nextXn = 0;
        forX:
        for (int x = 0; x <= radiusX; ++x) {
            final double xn = nextXn;
            nextXn += invRadiusX;
            double nextZn = 0;
            forZ:
            for (int z = 0; z <= radiusZ; ++z) {
                final double zn = nextZn;
                nextZn += invRadiusZ;

                double distanceSq = Mth.lengthSquared(xn, zn);
                if (distanceSq > 1) {
                    if (z == 0) {
                        break forX;
                    }
                    break forZ;
                }

                if (Mth.lengthSquared(nextXn, zn) <= 1 && Mth.lengthSquared(xn, nextZn) <= 1) {
                    continue;
                }

                for (Quadrant quadrant : quadrantsList)
                    shapeable.setBlock(x * quadrant.x, y, z * quadrant.z);
            }
        }
    }

    public static void makeEllipse(int minX, int minZ, int maxX, int maxZ, int y, IShapeable shapeable, Set<Quadrant> quadrants) {
        {
            final int centerX = (minX + maxX) / 2;
            final int centerZ = (minZ + maxZ) / 2;

            final IShapeable prevShapeable = shapeable;
            shapeable = (x, y1, z) -> prevShapeable.setBlock(centerX + x, y1, centerZ + z);
        }

        final int radiusX;
        final int radiusZ;

        {
            final int diffX = maxX - minX;
            if ((diffX & 1) == 0) {
                radiusX = diffX / 2;
            } else {
                radiusX = diffX / 2 + 1;
                shapeable = skipMiddleX(shapeable);
            }
        }

        {
            final int diffY = maxZ - minZ;
            if ((diffY & 1) == 0) {
                radiusZ = diffY / 2;
            } else {
                radiusZ = diffY / 2 + 1;
                shapeable = skipMiddleZ(shapeable);
            }
        }

        makeEllipse(radiusX, radiusZ, y, shapeable, quadrants);
    }

    private static IShapeable skipMiddleX(final IShapeable shapeable) {
        return (x, y, z) -> {
            if (x != 0) {
                if (x < 0) shapeable.setBlock(x + 1, y, z);
                else shapeable.setBlock(x, y, z);
            }
        };
    }

    private static IShapeable skipMiddleY(final IShapeable shapeable) {
        return (x, y, z) -> {
            if (y != 0) {
                if (y < 0) shapeable.setBlock(x, y + 1, z);
                else shapeable.setBlock(x, y, z);
            }
        };
    }

    private static IShapeable skipMiddleZ(final IShapeable shapeable) {
        return (x, y, z) -> {
            if (z != 0) {
                if (z < 0) shapeable.setBlock(x, y, z + 1);
                else shapeable.setBlock(x, y, z);
            }
        };
    }

    public static void line2D(int y, int x0, int z0, int x1, int z1, IShapeable shapeable) {
        final int dx = Math.abs(x1 - x0);
        final int sx = x0 < x1? 1 : -1;
        final int dy = -Math.abs(z1 - z0);
        final int sy = z0 < z1? 1 : -1;
        int err = dx + dy;

        while (true) {
            shapeable.setBlock(x0, y, z0);
            if (x0 == x1 && z0 == z1) break;
            final int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            } /* e_xy+e_x > 0 */
            if (e2 <= dx) {
                err += dx;
                z0 += sy;
            } /* e_xy+e_y < 0 */
        }
    }

    public static void line3D(Coord start, Coord end, IShapeable shapeable) {
        line3D(start.x, start.y, start.z, end.x, end.y, end.z, shapeable);
    }

    public static void line3D(Vec3i start, Vec3i end, IShapeable shapeable) {
        line3D(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ(), shapeable);
    }

    public static void line3D(final int startX, final int startY, final int startZ, final int endX, final int endY, final int endZ, IShapeable shapeable) {
        final int dx = endX - startX;
        final int dy = endY - startY;
        final int dz = endZ - startZ;

        final int ax = Math.abs(dx) << 1;
        final int ay = Math.abs(dy) << 1;
        final int az = Math.abs(dz) << 1;

        final int signx = Integer.signum(dx);
        final int signy = Integer.signum(dy);
        final int signz = Integer.signum(dz);

        int x = startX;
        int y = startY;
        int z = startZ;

        int deltax, deltay, deltaz;
        if (ax >= Math.max(ay, az)) {
            deltay = ay - (ax >> 1);
            deltaz = az - (ax >> 1);
            while (true) {
                shapeable.setBlock(x, y, z);
                if (x == endX) return;

                if (deltay >= 0) {
                    y += signy;
                    deltay -= ax;
                }

                if (deltaz >= 0) {
                    z += signz;
                    deltaz -= ax;
                }

                x += signx;
                deltay += ay;
                deltaz += az;
            }
        } else if (ay >= Math.max(ax, az)) {
            deltax = ax - (ay >> 1);
            deltaz = az - (ay >> 1);
            while (true) {
                shapeable.setBlock(x, y, z);
                if (y == endY) return;

                if (deltax >= 0) {
                    x += signx;
                    deltax -= ay;
                }

                if (deltaz >= 0) {
                    z += signz;
                    deltaz -= ay;
                }

                y += signy;
                deltax += ax;
                deltaz += az;
            }
        } else if (az >= Math.max(ax, ay)) {
            deltax = ax - (az >> 1);
            deltay = ay - (az >> 1);
            while (true) {
                shapeable.setBlock(x, y, z);
                if (z == endZ) return;

                if (deltax >= 0) {
                    x += signx;
                    deltax -= az;
                }

                if (deltay >= 0) {
                    y += signy;
                    deltay -= az;
                }

                z += signz;
                deltax += ax;
                deltay += ay;
            }
        }
    }

    public static double normalizeAngle(double angle) {
        while (angle > 180.0)
            angle -= 360.0;
        while (angle < -180.0)
            angle += 360.0;
        return angle;
    }

    public static double compareAngles(double current, double target) {
        current = normalizeAngle(current);
        target = normalizeAngle(target);
        return Math.signum(target - current);
    }

    public static double getAngleDistance(double current, double target) {
        double result = target - current;
        return Math.abs(result) > 180? 180 - result : result;
    }
}