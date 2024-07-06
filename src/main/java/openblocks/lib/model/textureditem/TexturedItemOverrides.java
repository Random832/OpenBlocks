package openblocks.lib.model.textureditem;

import com.google.common.collect.Maps;
import com.mojang.math.Transformation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.SimpleModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import openblocks.lib.model.IItemTexture;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class TexturedItemOverrides extends ItemOverrides {
    private final Map<ResourceLocation, BakedModel> cache = Maps.newHashMap(); // contains all the baked models since they'll never change
    private final ItemOverrides nested;
    private final ModelBaker baker;
    private final IGeometryBakingContext owner;
    private final ResourceLocation model;
    private final Set<ResourceLocation> placeholders;

    public TexturedItemOverrides(ItemOverrides nested, ModelBaker baker, IGeometryBakingContext owner, ResourceLocation model, Set<ResourceLocation> placeholders) {
        this.nested = nested;
        this.baker = baker;
        this.owner = owner;
        this.model = model;
        this.placeholders = placeholders;
    }

    @Override
    public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        BakedModel overridden = nested.resolve(originalModel, stack, level, entity, seed);
        if (overridden != originalModel) return overridden;
        Optional<ResourceLocation> optTexture = getTexture(stack);
        return optTexture
                .map(texture -> {
                    if (!cache.containsKey(texture)) {
                        @Nullable BakedModel bakedModel = baker.bake(model, new SimpleModelState(Transformation.identity()),
                                s -> placeholders.contains(s.texture())
                                        ? new Material(s.atlasLocation(), texture).sprite()
                                        : s.sprite());
                        if(bakedModel == null)
                            bakedModel = originalModel;
                        cache.put(texture, bakedModel);
                        return bakedModel;
                    }

                    return cache.get(texture);
                })
                .orElse(originalModel);
    }

    public static Optional<ResourceLocation> getTexture(ItemStack stack) {
        if(stack.getItem() instanceof IItemTexture it)
            return it.getTexture();
        else return Optional.empty();
    }
}
