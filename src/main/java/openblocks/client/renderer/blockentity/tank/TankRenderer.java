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
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import openblocks.common.blockentity.TankBlockEntity;
import openblocks.lib.geometry.Diagonal;
import org.joml.Matrix4f;

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
			renderFluid(output, matrixStack.last().pose(), data, time, combinedLightIn, combinedOverlayIn);
		}
	}

	private static void addVertex(VertexConsumer wr, Matrix4f matrix, float x, float y, float z, float u, float v, int r, int g, int b, int a, int lightmap, int overlay, Direction normal) {
		wr.addVertex(matrix, x, y, z)
				.setColor(r, g, b, a)
				.setUv(u, v)
				.setOverlay(overlay)
				.setLight(lightmap)
				.setNormal(normal.getStepX(), normal.getStepY(), normal.getStepZ());
	}

	private static void renderFluid(final VertexConsumer wr, final Matrix4f matrix, final ITankRenderFluidData data, float time, int combinedLights, int combinedOverlay) {
		float se = data.getCornerFluidLevel(Diagonal.SE, time);
		float ne = data.getCornerFluidLevel(Diagonal.NE, time);
		float sw = data.getCornerFluidLevel(Diagonal.SW, time);
		float nw = data.getCornerFluidLevel(Diagonal.NW, time);

		final float center = data.getCenterFluidLevel(time);

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

        // dirty way to avoid z-fighting

		final float EPSILON = 0.00625f; // 0.03125f;

		if (data.shouldRenderFluidWall(Direction.NORTH) && (nw > 0 || ne > 0)) {
			addVertex(wr, matrix, 1f, 0f, EPSILON, uMax, vMin, r, g, b, a, combinedLights, combinedOverlay, Direction.NORTH);
			addVertex(wr, matrix, 0f, 0f, EPSILON, uMin, vMin, r, g, b, a, combinedLights, combinedOverlay, Direction.NORTH);
			addVertex(wr, matrix, 0f, nw, EPSILON, uMin, vMin + (vHeight * nw), r, g, b, a, combinedLights, combinedOverlay, Direction.NORTH);
			addVertex(wr, matrix, 1f, ne, EPSILON, uMax, vMin + (vHeight * ne), r, g, b, a, combinedLights, combinedOverlay, Direction.NORTH);
		}

		if (data.shouldRenderFluidWall(Direction.SOUTH) && (se > 0 || sw > 0)) {
			addVertex(wr, matrix, 1f, 0f, 1-EPSILON, uMin, vMin, r, g, b, a, combinedLights, combinedOverlay, Direction.SOUTH);
			addVertex(wr, matrix, 1f, se, 1-EPSILON, uMin, vMin + (vHeight * se), r, g, b, a, combinedLights, combinedOverlay, Direction.SOUTH);
			addVertex(wr, matrix, 0f, sw, 1-EPSILON, uMax, vMin + (vHeight * sw), r, g, b, a, combinedLights, combinedOverlay, Direction.SOUTH);
			addVertex(wr, matrix, 0f, 0f, 1-EPSILON, uMax, vMin, r, g, b, a, combinedLights, combinedOverlay, Direction.SOUTH);
		}

		if (data.shouldRenderFluidWall(Direction.EAST) && (ne > 0 || se > 0)) {
			addVertex(wr, matrix, 1-EPSILON, 0f, 0f, uMin, vMin, r, g, b, a, combinedLights, combinedOverlay, Direction.EAST);
			addVertex(wr, matrix, 1-EPSILON, ne, 0f, uMin, vMin + (vHeight * ne), r, g, b, a, combinedLights, combinedOverlay, Direction.EAST);
			addVertex(wr, matrix, 1-EPSILON, se, 1f, uMax, vMin + (vHeight * se), r, g, b, a, combinedLights, combinedOverlay, Direction.EAST);
			addVertex(wr, matrix, 1-EPSILON, 0f, 1f, uMax, vMin, r, g, b, a, combinedLights, combinedOverlay, Direction.EAST);
		}

		if (data.shouldRenderFluidWall(Direction.WEST) && (sw > 0 || nw > 0)) {
			addVertex(wr, matrix, EPSILON, 0f, 1f, uMin, vMin, r, g, b, a, combinedLights, combinedOverlay, Direction.WEST);
			addVertex(wr, matrix, EPSILON, sw, 1f, uMin, vMin + (vHeight * sw), r, g, b, a, combinedLights, combinedOverlay, Direction.WEST);
			addVertex(wr, matrix, EPSILON, nw, 0f, uMax, vMin + (vHeight * nw), r, g, b, a, combinedLights, combinedOverlay, Direction.WEST);
			addVertex(wr, matrix, EPSILON, 0f, 0f, uMax, vMin, r, g, b, a, combinedLights, combinedOverlay, Direction.WEST);
		}

		if (data.shouldRenderFluidWall(Direction.UP)) {
			final float uMid = (uMax + uMin) / 2;
			final float vMid = (vMax + vMin) / 2;

			float se2 = Math.min(se, 1-EPSILON);
			float sw2 = Math.min(sw, 1-EPSILON);
			float ne2 = Math.min(ne, 1-EPSILON);
			float nw2 = Math.min(nw, 1-EPSILON);
			float cc2 = Math.min(nw, 1-EPSILON);

			// Normals are approximate
			addVertex(wr, matrix, 0.5f, cc2, 0.5f, uMid, vMid, r, g, b, a, combinedLights, combinedOverlay, Direction.UP);
			addVertex(wr, matrix, 1f, se2, 1f, uMax, vMin, r, g, b, a, combinedLights, combinedOverlay, Direction.UP);
			addVertex(wr, matrix, 1f, ne2, 0f, uMin, vMin, r, g, b, a, combinedLights, combinedOverlay, Direction.UP);
			addVertex(wr, matrix, 0f, nw2, 0f, uMin, vMax, r, g, b, a, combinedLights, combinedOverlay, Direction.UP);

			addVertex(wr, matrix, 0f, sw2, 1f, uMax, vMax, r, g, b, a, combinedLights, combinedOverlay, Direction.UP);
			addVertex(wr, matrix, 1f, se2, 1f, uMax, vMin, r, g, b, a, combinedLights, combinedOverlay, Direction.UP);
			addVertex(wr, matrix, 0.5f, cc2, 0.5f, uMid, vMid, r, g, b, a, combinedLights, combinedOverlay, Direction.UP);
			addVertex(wr, matrix, 0f, nw2, 0f, uMin, vMax, r, g, b, a, combinedLights, combinedOverlay, Direction.UP);
		}

		if (data.shouldRenderFluidWall(Direction.DOWN)) {
			addVertex(wr, matrix, 1f, EPSILON, 0f, uMax, vMin, r, g, b, a, combinedLights, combinedOverlay, Direction.DOWN);
			addVertex(wr, matrix, 1f, EPSILON, 1f, uMin, vMin, r, g, b, a, combinedLights, combinedOverlay, Direction.DOWN);
			addVertex(wr, matrix, 0f, EPSILON, 1f, uMin, vMax, r, g, b, a, combinedLights, combinedOverlay, Direction.DOWN);
			addVertex(wr, matrix, 0f, EPSILON, 0f, uMax, vMax, r, g, b, a, combinedLights, combinedOverlay, Direction.DOWN);
		}
	}
}
