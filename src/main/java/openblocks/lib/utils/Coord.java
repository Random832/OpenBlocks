package openblocks.lib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class Coord implements Cloneable {
    public final int x;
    public final int y;
    public final int z;

    public Coord(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Coord(double x, double y, double z) {
        this.x = Mth.floor(x);
        this.y = Mth.floor(y);
        this.z = Mth.floor(z);
    }

    public Coord(int[] coords) {
        this(coords[0], coords[1], coords[2]);
    }

    public Coord(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public Coord(Vec3 vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Coord offset(int ox, int oy, int oz) {
        return new Coord(x + ox, y + oy, z + oz);
    }

    @Override
    public int hashCode() {
        return (x + 128) << 16 | (y + 128) << 8 | (z + 128);
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof Coord)) { return false; }
        Coord otherCoord = (Coord)that;
        return otherCoord.x == x && otherCoord.y == y && otherCoord.z == z;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s", x, y, z);
    }

    @Override
    public Coord clone() {
        return new Coord(x, y, z);
    }

    public BlockPos asBlockPos() {
        return new BlockPos(x, y, z);
    }

    public Vec3 asVector() {
        return new Vec3(x, y, z);
    }

    public Coord add(Coord other) {
        return new Coord(x + other.x, y + other.y, z + other.z);
    }

    public Coord substract(Coord other) {
        return new Coord(x - other.x, y - other.y, z - other.z);
    }

    public int lengthSq() {
        return x * x + y * y + z * z;
    }

    public double length() {
        return Math.sqrt(lengthSq());
    }

    public boolean isAbove(Coord pos) {
        return pos != null? y > pos.y : false;
    }

    public boolean isBelow(Coord pos) {
        return pos != null? y < pos.y : false;
    }

    public boolean isNorthOf(Coord pos) {
        return pos != null? z < pos.z : false;
    }

    public boolean isSouthOf(Coord pos) {
        return pos != null? z > pos.z : false;
    }

    public boolean isEastOf(Coord pos) {
        return pos != null? x > pos.x : false;
    }

    public boolean isWestOf(Coord pos) {
        return pos != null? x < pos.x : false;
    }

    public boolean isXAligned(Coord pos) {
        return pos != null? x == pos.x : false;
    }

    public boolean isYAligned(Coord pos) {
        return pos != null? y == pos.y : false;
    }

    public boolean isZAligned(Coord pos) {
        return pos != null? z == pos.z : false;
    }
}