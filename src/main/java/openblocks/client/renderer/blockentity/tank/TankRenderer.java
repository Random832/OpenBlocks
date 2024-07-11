package openblocks.client.renderer.blockentity.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import openblocks.common.blockentity.TankBlockEntity;
import openblocks.lib.geometry.Diagonal;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TankRenderer implements BlockEntityRenderer<TankBlockEntity> {
	public TankRenderer(BlockEntityRendererProvider.Context c) {
	}

	@Override
	public void render(TankBlockEntity tankTile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
		final ITankRenderFluidData data = tankTile.getRenderFluidData();

		if (data != null && data.hasFluid()) {
			final Level world = tankTile.getLevel();
			final float time = world.getGameTime() + partialTicks;
			VertexConsumer output = buffer.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
			renderFluid(matrixStack, output, data, time, combinedLightIn, combinedOverlayIn);
		}
	}

	private static void addVertex(VertexConsumer wr, Matrix4f matrix, float x, float y, float z, float u, float v, int r, int g, int b, int a, int lightmap, int overlay, Vector3f normal) {
		wr.addVertex(matrix, x, y, z)
				.setColor(r, g, b, a)
				.setUv(u, v)
				.setOverlay(overlay)
				.setLight(lightmap)
				.setNormal(normal.x, normal.y, normal.z);
	}

	private static void renderFluid(PoseStack poseStack, final VertexConsumer wr, final ITankRenderFluidData data, float time, int combinedLights, int combinedOverlay) {
		final PoseStack.Pose pose = poseStack.last();
		final Matrix4f matrix = pose.pose();

		float cy = data.getCenterFluidLevel(time);

		final FluidStack fluid = data.getFluid();
		IClientFluidTypeExtensions attributes = IClientFluidTypeExtensions.of(fluid.getFluid());
		final TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(attributes.getStillTexture(fluid));
		final int color = attributes.getTintColor(fluid);
		final int r = ((color >> 16) & 0xFF);
		final int g = ((color >> 8) & 0xFF);
		final int b = ((color >> 0) & 0xFF);
		final int a = ((color >> 24) & 0xFF);

		final float uMin = texture.getU0();
		final float uMax = texture.getU1();
		final float vMin = texture.getV0();
		final float vMax = texture.getV1();

		final float vHeight = vMax - vMin;

		// set the fluid face back from the model space slightly to avoid z-fighting
		// classic frame is 1/80 thick, so 1/160 in and out of the block space.
		final float EPSILON = 1/256f;

		boolean doT = data.shouldRenderFluidWall(Direction.UP);
		float nw, ne, se, sw;
		if(doT) {
			nw = data.getCornerFluidLevel(Diagonal.NW, time) * (1 - EPSILON);
			ne = data.getCornerFluidLevel(Diagonal.NE, time) * (1 - EPSILON);
			sw = data.getCornerFluidLevel(Diagonal.SW, time) * (1 - EPSILON);
			se = data.getCornerFluidLevel(Diagonal.SE, time) * (1 - EPSILON);
		} else {
			nw = ne = se = sw = 1;
		}

		boolean doB = data.shouldRenderFluidWall(Direction.DOWN);
		float by = doB ? EPSILON : 0;

		boolean doN = data.shouldRenderFluidWall(Direction.NORTH) && (nw > 0 || ne > 0);
		boolean doS = data.shouldRenderFluidWall(Direction.SOUTH) && (se > 0 || sw > 0);
		boolean doW = data.shouldRenderFluidWall(Direction.WEST) && (sw > 0 || nw > 0);
		boolean doE = data.shouldRenderFluidWall(Direction.EAST) && (ne > 0 || se > 0);

		float nz = doN ? EPSILON : 0;
		float sz = doS ? 1 - EPSILON : 1;
		float wx = doW ? EPSILON : 0;
		float ex = doE ? 1 - EPSILON : 1;

		Vector3f norm = new Vector3f();

		if (doN) {
			pose.transformNormal(0, 0, -1, norm);
			addVertex(wr, matrix, wx, by, nz, uMin, vMin, r, g, b, a, combinedLights, combinedOverlay, norm);
			addVertex(wr, matrix, wx, nw, nz, uMin, vMin + (vHeight * nw), r, g, b, a, combinedLights, combinedOverlay, norm);
			addVertex(wr, matrix, ex, ne, nz, uMax, vMin + (vHeight * ne), r, g, b, a, combinedLights, combinedOverlay, norm);
			addVertex(wr, matrix, ex, by, nz, uMax, vMin, r, g, b, a, combinedLights, combinedOverlay, norm);
		}

		if (doS) {
			pose.transformNormal(0, 0, 1, norm);
			addVertex(wr, matrix, ex, by, sz, uMin, vMin, r, g, b, a, combinedLights, combinedOverlay, norm);
			addVertex(wr, matrix, ex, se, sz, uMin, vMin + (vHeight * se), r, g, b, a, combinedLights, combinedOverlay, norm);
			addVertex(wr, matrix, wx, sw, sz, uMax, vMin + (vHeight * sw), r, g, b, a, combinedLights, combinedOverlay, norm);
			addVertex(wr, matrix, wx, by, sz, uMax, vMin, r, g, b, a, combinedLights, combinedOverlay, norm);
		}

		if (doW) {
			pose.transformNormal(-1, 0, 0, norm);
			addVertex(wr, matrix, wx, by, sz, uMin, vMin, r, g, b, a, combinedLights, combinedOverlay, norm);
			addVertex(wr, matrix, wx, sw, sz, uMin, vMin + (vHeight * sw), r, g, b, a, combinedLights, combinedOverlay, norm);
			addVertex(wr, matrix, wx, nw, nz, uMax, vMin + (vHeight * nw), r, g, b, a, combinedLights, combinedOverlay, norm);
			addVertex(wr, matrix, wx, by, nz, uMax, vMin, r, g, b, a, combinedLights, combinedOverlay, norm);
		}

		if (doE) {
			pose.transformNormal( 1, 0, 0, norm);
			addVertex(wr, matrix, ex, by, nz, uMin, vMin, r, g, b, a, combinedLights, combinedOverlay, norm);
			addVertex(wr, matrix, ex, ne, nz, uMin, vMin + (vHeight * ne), r, g, b, a, combinedLights, combinedOverlay, norm);
			addVertex(wr, matrix, ex, se, sz, uMax, vMin + (vHeight * se), r, g, b, a, combinedLights, combinedOverlay, norm);
			addVertex(wr, matrix, ex, by, sz, uMax, vMin, r, g, b, a, combinedLights, combinedOverlay, norm);
		}

		if (doB) {
			pose.transformNormal(0, -1,0, norm);
			addVertex(wr, matrix, ex, by, sz, uMin, vMin, r, g, b, a, combinedLights, combinedOverlay, norm);
			addVertex(wr, matrix, wx, by, sz, uMin, vMax, r, g, b, a, combinedLights, combinedOverlay, norm);
			addVertex(wr, matrix, wx, by, nz, uMax, vMax, r, g, b, a, combinedLights, combinedOverlay, norm);
			addVertex(wr, matrix, ex, by, nz, uMax, vMin, r, g, b, a, combinedLights, combinedOverlay, norm);
		}

		if (doT) {
			pose.transformNormal(0, 1, 0,norm); // Normals are approximate
			cy *= 1 - EPSILON;

			if (Mth.abs(cy - (nw + ne + sw + se) / 4) < 0.25) {
				// translucency sorting doesn't like the two quad version, so
				// try to render as one quad unless it's going to be very misleading
				addVertex(wr, matrix, wx, sw, sz, uMin, vMax, r, g, b, a, combinedLights, combinedOverlay, norm);
				addVertex(wr, matrix, ex, se, sz, uMin, vMin, r, g, b, a, combinedLights, combinedOverlay, norm);
				addVertex(wr, matrix, ex, ne, nz, uMax, vMin, r, g, b, a, combinedLights, combinedOverlay, norm);
				addVertex(wr, matrix, wx, nw, nz, uMax, vMax, r, g, b, a, combinedLights, combinedOverlay, norm);
			} else {
				final float uMid = (uMax + uMin) / 2;
				final float vMid = (vMax + vMin) / 2;

				addVertex(wr, matrix, 0.5f, cy, 0.5f, uMid, vMid, r, g, b, a, combinedLights, combinedOverlay, norm);
				addVertex(wr, matrix, ex, se, sz, uMax, vMin, r, g, b, a, combinedLights, combinedOverlay, norm);
				addVertex(wr, matrix, ex, ne, nz, uMin, vMin, r, g, b, a, combinedLights, combinedOverlay, norm);
				addVertex(wr, matrix, wx, nw, nz, uMin, vMax, r, g, b, a, combinedLights, combinedOverlay, norm);

				addVertex(wr, matrix, wx, sw, sz, uMax, vMax, r, g, b, a, combinedLights, combinedOverlay, norm);
				addVertex(wr, matrix, ex, se, sz, uMax, vMin, r, g, b, a, combinedLights, combinedOverlay, norm);
				addVertex(wr, matrix, 0.5f, cy, 0.5f, uMid, vMid, r, g, b, a, combinedLights, combinedOverlay, norm);
				addVertex(wr, matrix, wx, ne, nz, uMin, vMax, r, g, b, a, combinedLights, combinedOverlay, norm);
			}
		}
	}
}