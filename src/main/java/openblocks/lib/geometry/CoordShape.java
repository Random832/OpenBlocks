package openblocks.lib.geometry;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


public class CoordShape {
    @OnlyIn(Dist.CLIENT)
    private int vbo;

    private final List<BlockPos> coords;

    public CoordShape(List<BlockPos> coords) {
        this.coords = coords;
    }

//    @OnlyIn(Dist.CLIENT)
//    public int bindVBO() {
//        if (vbo == 0) {
//            ByteBuffer data = BufferUtils.createByteBuffer(coords.size() * 3 * 4);
//            for (BlockPos c : coords) { data.putFloat(c.getX() - 0.5f).putFloat(c.getY()).putFloat(c.getZ() - 0.5f); }
//            data.flip();
//
//            vbo = BufferHelper.methods().glGenBuffers();
//            BufferHelper.methods().glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
//            BufferHelper.methods().glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
//        } else {
//            BufferHelper.methods().glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
//        }
//
//        return vbo;
//    }

    public List<BlockPos> getCoords() {
        return coords;
    }

    public int size() {
        return coords.size();
    }

//    @OnlyIn(Dist.CLIENT)
//    public void destroy() {
//        if (vbo != 0) {
//            BufferHelper.methods().glDeleteBuffers(vbo);
//        }
//        vbo = 0;
//    }
}