package openblocks.lib.geometry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Hitbox {

    static final Codec<Hitbox> CODEC = RecordCodecBuilder.<Hitbox>mapCodec(
            instance -> instance.group(
                    Vec3.CODEC.fieldOf("to").forGetter(b -> b.to),
                    Vec3.CODEC.fieldOf("from").forGetter(b -> b.from),
                    Codec.STRING.fieldOf("name").forGetter(b -> b.name)
            ).apply(instance, Hitbox::new)
    ).codec();

    Hitbox(Vec3 to, Vec3 from, String name) {
        this.to=to;
        this.from=from;
        this.name = name;
    }

    public String name;

    public Vec3 from;

    public Vec3 to;

    @Nullable
    private transient AABB scaledBoundingBox;
    @Nullable
    private transient AABB rawBoundingBox;

    public AABB getScaledBoundingBox() {
        // for some damn reason it's 0..16 in the data and we actually need it as 0..1 in the code
        if (scaledBoundingBox == null)
            scaledBoundingBox = new AABB(from.scale(0.0625), to.scale(0.0625));

        return scaledBoundingBox;
    }

    public AABB getRawBoundingBox() {
        // for some damn reason the transform logic needs it in the original format for render highlight to work correctly
        if (rawBoundingBox == null)
            rawBoundingBox = new AABB(from, to);

        return rawBoundingBox;
    }
}