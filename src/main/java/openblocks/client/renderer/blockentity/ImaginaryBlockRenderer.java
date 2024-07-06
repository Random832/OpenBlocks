package openblocks.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;
import openblocks.Config;
import openblocks.common.blockentity.ImaginaryBlockEntity;

public class ImaginaryBlockRenderer implements BlockEntityRenderer<ImaginaryBlockEntity> {
    private final BlockModelShaper shaper;

    public ImaginaryBlockRenderer(BlockEntityRendererProvider.Context c) {
        shaper = c.getBlockRenderDispatcher().getBlockModelShaper();
    }

    @Override
    public void render(ImaginaryBlockEntity te, float partialTicks, PoseStack pPoseStack, MultiBufferSource pMultiBufferSource, int pPackedLight, int pPackedOverlay) {
        if (!(Minecraft.getInstance().cameraEntity instanceof LivingEntity cameraEntity)) return;
        boolean isVisible = te.is(ImaginaryBlockEntity.Property.VISIBLE, cameraEntity);

        if (isVisible && te.opacity < 1) te.opacity = Math.min(te.opacity + Config.imaginaryFadingSpeed, 1);
        else if (!isVisible && te.opacity > 0) te.opacity = Math.max(te.opacity - Config.imaginaryFadingSpeed, 0);

        if (te.opacity <= 0) return;

        final float r;
        final float g;
        final float b;

        int color = te.getTintColor();
        r = ((color >> 16) & 0xFF) / 255.0f;
        g = ((color >> 8) & 0xFF) / 255.0f;
        b = ((color >> 0) & 0xFF) / 255.0f;

        final float a = te.opacity;
        // the posestack should take care of our x y z

        BlockState state = te.getBlockState();

        //if (!(state.getBlock() instanceof ImaginaryBlock)) return;
        // The old mod had a whole model cache system that I didn't port
        // Are baked models not baked enough?

        BakedModel model = shaper.getBlockModel(state);

        RenderType blockRenderType = RenderType.translucent();

        RandomSource rand = RandomSource.create();
        rand.setSeed(42);
        VertexConsumer buffer = pMultiBufferSource.getBuffer(RenderTypeHelper.getEntityRenderType(blockRenderType, false));

        for (Direction value : Direction.values()) {
            for (BakedQuad quad : model.getQuads(state, value, rand, ModelData.EMPTY, blockRenderType))
                buffer.putBulkData(pPoseStack.last(), quad, r, g, b, a, pPackedLight, pPackedOverlay);
            rand.setSeed(42);
            for (BakedQuad quad : model.getQuads(state, null, rand, ModelData.EMPTY, blockRenderType)) {
                buffer.putBulkData(pPoseStack.last(), quad, r, g, b, a, pPackedLight, pPackedOverlay);
            }
        }
    }
}