package openblocks.lib.shapes;

import openblocks.lib.utils.render.GeometryUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShapeEquilateral2dGenerator extends DefaultShapeGenerator {

    private static class Trig {
        final double sin;
        final double cos;

        public Trig(double sin, double cos) {
            this.sin = sin;
            this.cos = cos;
        }
    }

    private static class Point {
        final int x;
        final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "[" + x + "," + y + "]";
        }

    }

    private enum Symmetry {
        TwoFold(Math.PI) {
            @Override
            public IShapeable createMirroredShapeable(final IShapeable shapeable) {
                return (x, y, z) -> {
                    if (z >= 0) {
                        shapeable.setBlock(x, y, +z);
                        shapeable.setBlock(x, y, -z);
                    }
                };
            }

            @Override
            public Point mirrorLastPoint(Point point) {
                return new Point(point.x, -point.y);
            }
        },
        FourFold(Math.PI / 2) {
            @Override
            public IShapeable createMirroredShapeable(final IShapeable shapeable) {
                return (x, y, z) -> {
                    if (x >= 0 && z >= 0) {
                        shapeable.setBlock(+x, y, -z);
                        shapeable.setBlock(-x, y, -z);
                        shapeable.setBlock(-x, y, +z);
                        shapeable.setBlock(+x, y, +z);
                    }
                };
            }

            @Override
            public Point mirrorLastPoint(Point point) {
                return new Point(-point.x, point.y);
            }
        },
        EightFold(Math.PI / 4) {
            @Override
            public IShapeable createMirroredShapeable(final IShapeable shapeable) {
                return (x, y, z) -> {
                    if (x >= z) {
                        shapeable.setBlock(+x, y, -z);
                        shapeable.setBlock(-x, y, -z);
                        shapeable.setBlock(-x, y, +z);
                        shapeable.setBlock(+x, y, +z);

                        shapeable.setBlock(+z, y, -x);
                        shapeable.setBlock(-z, y, -x);
                        shapeable.setBlock(-z, y, +x);
                        shapeable.setBlock(+z, y, +x);
                    }
                };
            }

            @Override
            public Point mirrorLastPoint(Point point) {
                return new Point(point.y, point.x);
            }
        };

        public abstract IShapeable createMirroredShapeable(IShapeable shapeable);

        public abstract Point mirrorLastPoint(Point point);

        public final double angleLimit;

        Symmetry(double angleLimit) {
            this.angleLimit = angleLimit;
        }
    }

    private static Symmetry findSymmetry(int sides) {
        if (sides % 4 == 0) return Symmetry.EightFold;
        if (sides % 2 == 0) return Symmetry.FourFold;
        return Symmetry.TwoFold;
    }

    private final Symmetry symmetry;

    private final Trig[] angles;

    public ShapeEquilateral2dGenerator(int sides) {
        this.symmetry = findSymmetry(sides);

        final List<Trig> angles = new ArrayList<>();
        for (int i = 0; i < sides; i++) {
            final double d = 2 * Math.PI * i / sides;
            if (d > this.symmetry.angleLimit) break;
            angles.add(new Trig(Math.sin(d), Math.cos(d)));
        }

        this.angles = angles.toArray(new Trig[0]);

    }

    @Override
    public void generateShape(int minX, final int minY, int minZ, int maxX, final int maxY, int maxZ, final IShapeable shapeable) {
        final IShapeable columnShapeable = (x, ingored, z) -> {
            for (int y = minY; y <= maxY; y++)
                shapeable.setBlock(x, y, z);
        };

        final double middleX = (maxX + minX) / 2.0;
        final double radiusX = (maxX - minX) / 2.0;

        final double middleZ = (maxZ + minZ) / 2.0;
        final double radiusZ = (maxZ - minZ) / 2.0;

        final Point[] points = Arrays.stream(angles).map(input -> {
            int x = (int)Math.round(middleX + radiusX * input.cos);
            int z = (int)Math.round(middleZ + radiusZ * input.sin);
            return new Point(x, z);
        }).toArray(Point[]::new);

        final IShapeable mirroredShapeable = symmetry.createMirroredShapeable(columnShapeable);

        Point prevPoint = points[0];

        for (int i = 1; i < points.length; i++) {
            final Point point = points[i];
            GeometryUtils.line2D(0, prevPoint.x, prevPoint.y, point.x, point.y, mirroredShapeable);
            prevPoint = point;
        }

        final Point lastPoint = symmetry.mirrorLastPoint(prevPoint);
        GeometryUtils.line2D(0, prevPoint.x, prevPoint.y, lastPoint.x, lastPoint.y, mirroredShapeable);
    }
}