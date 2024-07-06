package openblocks.lib.geometry;

import com.google.common.collect.Maps;
import java.util.Map;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public enum Diagonal {
    NW(Direction.NORTH, Direction.WEST),
    NE(Direction.NORTH, Direction.EAST),
    SW(Direction.SOUTH, Direction.WEST),
    SE(Direction.SOUTH, Direction.EAST);

    public final int offsetX;
    public final int offsetY;
    public final int offsetZ;

    private Diagonal(Direction a, Direction b) {
        Vec3i va = a.getNormal();
        Vec3i vb = b.getNormal();
        this.offsetX = va.getX() + vb.getX();
        this.offsetY = va.getY() + vb.getY();
        this.offsetZ = va.getZ() + vb.getZ();
    }

    private static final Map<Diagonal, Diagonal> OPPOSITES = Maps.newEnumMap(Diagonal.class);

    private static final Map<Diagonal, Diagonal> CW = Maps.newEnumMap(Diagonal.class);

    private static final Map<Diagonal, Diagonal> CCW = Maps.newEnumMap(Diagonal.class);

    static {
        OPPOSITES.put(NW, SE);
        OPPOSITES.put(SE, NW);
        OPPOSITES.put(NE, SW);
        OPPOSITES.put(SW, NE);

        CW.put(NW, SW);
        CW.put(SW, SE);
        CW.put(SE, NE);
        CW.put(NE, NW);

        CCW.put(NW, NE);
        CCW.put(NE, SE);
        CCW.put(SE, SW);
        CCW.put(SW, NW);
    }

    public Diagonal getOpposite() {
        return OPPOSITES.get(this);
    }

    public Diagonal rotateCW() {
        return CW.get(this);
    }

    public Diagonal rotateCCW() {
        return CCW.get(this);
    }

    public static final Diagonal[] VALUES = values();
}