package openblocks.lib.geometry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HitboxSupplier implements Iterable<Hitbox> {
    static final Codec<HitboxSupplier> CODEC = RecordCodecBuilder.<HitboxSupplier>mapCodec(
            instance -> instance.group(
                    Hitbox.CODEC.listOf().fieldOf("boxes").forGetter(s -> s.list)
            ).apply(instance, HitboxSupplier::new)
    ).codec();

    private final List<Hitbox> list;

    HitboxSupplier(List<Hitbox> list) {
        this.list = list;
        //this.map = list.stream().collect(Collectors.toMap(h -> h.name, h -> h));
    }

    @NotNull
    @Override
    public Iterator<Hitbox> iterator() {
        return list.iterator();
    }

//    public Map<String, Hitbox> asMap() {
//    }
}
