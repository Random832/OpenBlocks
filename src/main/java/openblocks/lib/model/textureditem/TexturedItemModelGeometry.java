package openblocks.lib.model.textureditem;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.Set;
import java.util.function.Function;

public class TexturedItemModelGeometry implements IUnbakedGeometry<TexturedItemModelGeometry> {
    private final ResourceLocation untexturedModel;
    private final ResourceLocation texturedModel;
    private final Set<ResourceLocation> placeholders;

    public TexturedItemModelGeometry(ResourceLocation untexturedModel, ResourceLocation texturedModel, Set<ResourceLocation> placeholders) {
        this.untexturedModel = untexturedModel;
        this.texturedModel = texturedModel;
        this.placeholders = placeholders;
    }

    private static class Wrapper extends BakedModelWrapper<BakedModel> {
        private final ItemOverrides overrides;

        public Wrapper(BakedModel originalModel, ItemOverrides overrides) {
            super(originalModel);
            this.overrides = overrides;
        }

        @Override
        public ItemOverrides getOverrides() {
            return overrides;
        }
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        TexturedItemOverrides newOverrides = new TexturedItemOverrides(overrides, baker, context, texturedModel, placeholders);
        return new Wrapper(baker.bake(untexturedModel, modelState, spriteGetter), newOverrides);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        modelGetter.apply(untexturedModel).resolveParents(modelGetter);
        modelGetter.apply(texturedModel).resolveParents(modelGetter);
    }
}
