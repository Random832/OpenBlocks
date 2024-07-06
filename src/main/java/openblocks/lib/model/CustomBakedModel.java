package openblocks.lib.model;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;

public abstract class CustomBakedModel implements IDynamicBakedModel {
    protected final boolean isAmbientOcclusion;
    protected final boolean isGui3d;
    protected final boolean isSideLit;
    protected final TextureAtlasSprite particle;
    protected final ItemTransforms transforms;

    protected CustomBakedModel(final IGeometryBakingContext owner, final Function<Material, TextureAtlasSprite> spriteGetter, final ItemTransforms transforms) {
        isAmbientOcclusion = owner.useAmbientOcclusion();
        isGui3d = owner.isGui3d();
        isSideLit = owner.useBlockLight();
        this.transforms = transforms;

        Material particleLocation = owner.getMaterial("particle");
        particle = spriteGetter.apply(particleLocation);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return isAmbientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return isGui3d;
    }

    @Override
    public boolean usesBlockLight() {
        return isSideLit;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }
}