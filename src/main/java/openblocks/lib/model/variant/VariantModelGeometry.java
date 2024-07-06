package openblocks.lib.model.variant;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class VariantModelGeometry implements IUnbakedGeometry<VariantModelGeometry> {
    private final UnbakedModel base;
    private final List<Pair<Predicate<VariantModelState>, UnbakedModel>> parts;

    public VariantModelGeometry(UnbakedModel base, List<Pair<Predicate<VariantModelState>, UnbakedModel>> parts) {
        this.base = base;
        this.parts = parts;
    }

    private static class VariantBakedModel extends BakedModelWrapper<BakedModel> {
        private final LoadingCache<VariantModelState, Collection<BakedModel>> cache;

        public VariantBakedModel(BakedModel originalModel, List<Pair<Predicate<VariantModelState>, BakedModel>> parts) {
            super(originalModel);

            cache = CacheBuilder.newBuilder()
                    .expireAfterAccess(5, TimeUnit.MINUTES)
                    .build(
                            new CacheLoader<VariantModelState, Collection<BakedModel>>() {
                                @Override
                                public Collection<BakedModel> load(VariantModelState state) {
                                    //final VariantModelState full = state.expand(evaluator);
                                    return parts.stream().filter(e -> e.getFirst().test(state)).map(Pair::getSecond).collect(ImmutableSet.toImmutableSet());
                                }
                            });
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extState, @Nullable RenderType renderType) {
            final VariantModelState modelState = getModelSelectors(extState);

            final List<BakedQuad> result = Lists.newArrayList(originalModel.getQuads(state, side, rand, extState, renderType));

            for (final BakedModel part : cache.getUnchecked(modelState)) {
                result.addAll(part.getQuads(state, side, rand, extState, renderType));
            }
            return result;
        }

        private static VariantModelState getModelSelectors(ModelData state) {
            if (state != null) {
                final Supplier<VariantModelState> data = state.get(VariantModelState.PROPERTY);
                if (data != null) {
                    return data.get();
                }
            }

            return VariantModelState.EMPTY;
        }
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides) {
        final BakedModel base = this.base.bake(bakery, spriteGetter, modelTransform);
        List<Pair<Predicate<VariantModelState>, BakedModel>> parts = this.parts.stream().map(p -> Pair.of(p.getFirst(), p.getSecond().bake(bakery, spriteGetter, modelTransform))).collect(Collectors.toList());
        return new VariantBakedModel(base, parts);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        base.resolveParents(modelGetter);
        for (Pair<Predicate<VariantModelState>, UnbakedModel> part : parts) {
            part.getSecond().resolveParents(modelGetter);
        }
    }

    //    @Override
//    public Collection<Material> getTextures(IGeometryBakingContext owner, Function<ResourceLocation, BakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
//        return Stream.concat(
//                base.getTextures(modelGetter, missingTextureErrors).stream(),
//                parts.stream().flatMap(p -> p.getSecond().getTextures(modelGetter, missingTextureErrors).stream())
//        ).collect(Collectors.toSet());
//    }
}