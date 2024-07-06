package openblocks.common.support;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import openblocks.common.block.ImaginaryBlock;
import openblocks.lib.geometry.Hitbox;
import openblocks.lib.geometry.HitboxSupplier;

public record ImaginaryPlacementMode(Holder<Block> block, boolean inverted) {
    public static final Codec<ImaginaryPlacementMode> CODEC = RecordCodecBuilder.<ImaginaryPlacementMode>mapCodec(
            instance -> instance.group(
                    BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter(ImaginaryPlacementMode::block),
                    Codec.BOOL.fieldOf("inverted").forGetter(ImaginaryPlacementMode::inverted)
            ).apply(instance, ImaginaryPlacementMode::new)
    ).codec();

    int cost() {
        if(block.value() instanceof ImaginaryBlock ib)
            return ib.getDurabilityCost();
        else return 4;
    }

    public Component name() {
        String key = block.value().getDescriptionId() + ".placement";
        return Component.translatable(inverted ? key + ".inverted" : key);
    }
}
