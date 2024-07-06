package openblocks.client.renderer.blockentity;

import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.ModelEvent;
import openblocks.OpenBlocks;
import openblocks.common.blockentity.TileEntityGuide;
import openblocks.lib.geometry.CoordShape;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class GuideRenderer<T extends TileEntityGuide> implements BlockEntityRenderer<T> {

    protected final ModelHolder holder;

    public GuideRenderer(BlockEntityRendererProvider.Context context, ModelHolder holder) {
        //super(dispatcher);
        this.holder = holder;
    }

    @Override
    public boolean shouldRenderOffScreen(T te) {
        return false;
        //return te.shouldRender();
        // using that option causes glitches due to MC-112730
        // i don't know if this is the same glitch but it also doesn't clean up when removed -r
    }

    @Override
    public int getViewDistance() {
        return 512; // TODO figure out a more reasonable value
    }

    @Override
    public AABB getRenderBoundingBox(T blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }

    @Override
    public void render(T guide, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferGroup, int combinedLight, int combinedOverlay) {
        if (!guide.shouldRender()) return;
        VertexConsumer buffer = bufferGroup.getBuffer(RenderType.translucent());
        final float scaleDelta = guide.getTimeSinceChange();
        final Vector4f selfPose = new Vector4f(0, 0, 0, 1);
        selfPose.mul(matrixStack.last().pose());
        Vector3f pos = new Vector3f(selfPose.x, selfPose.y, selfPose.z);
        renderShape(buffer, matrixStack, guide.getShape(), guide.getColor(), scaleDelta, pos);
        if (scaleDelta < 1.0) {
            renderShape(buffer, matrixStack, guide.getPreviousShape(), guide.getColor(), 1.0f - scaleDelta, pos);
        }
    }

    private static float distanceFromOrigin(final Vector3f base, final BlockPos delta) {
        final float x = base.x + delta.getX();
        final float y = base.y + delta.getY();
        final float z = base.z + delta.getZ();

        return x * x + y * y + z * z;
    }

    private void renderShape(VertexConsumer bufferBuilder, final PoseStack stack, @Nullable CoordShape shape, int color, float scale, final Vector3f pos) {
        if (shape == null) {
            return;
        }

        final float red = ((color >> 16) & 0xFF) / 255.0f;
        final float green = ((color >> 8) & 0xFF) / 255.0f;
        final float blue = ((color >> 0) & 0xFF) / 255.0f;
        float[] colorMuls = { 1.0f, 1.0f, 1.0f, 1.0F };
        int combinedLightIn = LightTexture.pack(15, 15);
        int[] light = { combinedLightIn, combinedLightIn, combinedLightIn, combinedLightIn };


        // Forge removed TE buffer sorting, do it ourselves...
        shape.getCoords().stream().sorted(Comparator.comparing((BlockPos b) -> distanceFromOrigin(pos, b)).reversed()).forEach(coord -> {
            stack.pushPose();
            stack.translate(coord.getX(), coord.getY(), coord.getZ());
            stack.scale(scale, scale, scale);
            for (BakedQuad markerQuad : holder.getMarkerQuads()) {
                bufferBuilder.putBulkData(stack.last(), markerQuad, colorMuls, red, green, blue, 1, light, OverlayTexture.NO_OVERLAY, false);
            }
            stack.popPose();
        });
    }

    public static class ModelHolder {
        public static final ModelResourceLocation MARKER_MODEL_LOCATION = ModelResourceLocation.standalone(OpenBlocks.modLoc("block/guide_marker"));
        public static final ModelResourceLocation BIT_MODEL_LOCATION = ModelResourceLocation.standalone(OpenBlocks.modLoc("block/guide_bit"));

        private List<BakedQuad> markerQuads;
        private List<BakedQuad> bitQuads;

        public void onModelBake(ModelEvent.BakingCompleted evt) {
            markerQuads = getModel(evt, MARKER_MODEL_LOCATION);
            bitQuads = getModel(evt, BIT_MODEL_LOCATION);
        }

        private static List<BakedQuad> getModel(ModelEvent.BakingCompleted evt, ModelResourceLocation id) {
            BakedModel marker = evt.getModelManager().getModel(id);
            if (marker == null) {
                marker = evt.getModelManager().getMissingModel();
            }

            final RandomSource rand = RandomSource.create(12);
            final List<BakedQuad> quads = Lists.newArrayList();
            for (Direction enumfacing : Direction.values()) {
                rand.setSeed(12);
                quads.addAll(marker.getQuads(null, enumfacing, rand));
            }
            rand.setSeed(12);
            quads.addAll(marker.getQuads(null, null, rand));
            return ImmutableList.copyOf(quads);
        }

        public List<BakedQuad> getMarkerQuads() {
            return markerQuads;
        }

        public List<BakedQuad> getBitQuads() {
            return bitQuads;
        }

        public void onModelRegister(ModelEvent.RegisterAdditional evt) {
            evt.register(MARKER_MODEL_LOCATION);
            evt.register(BIT_MODEL_LOCATION);
        }
    }
}