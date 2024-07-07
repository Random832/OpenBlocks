package openblocks.client.renderer.blockentity.tank;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import openblocks.OpenBlocks;
import openblocks.common.blockentity.TankBlockEntity;
import openblocks.common.item.TankItem;

import javax.annotation.Nullable;

public class TankBEWLR extends BlockEntityWithoutLevelRenderer {
    private @Nullable TankBlockEntity TANK = null;
    public static @Nullable TankRenderer tankRenderer;
    ModelData modelData = NeighbourMap.getDataForBEWLR();

    public TankBEWLR() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if(tankRenderer == null) return;
        BlockState state = OpenBlocks.TANK_BLOCK.get().defaultBlockState();
        if (TANK == null) {
            TANK = new TankBlockEntity(BlockPos.ZERO, state);
            TANK.initializeForBewlr();
        }
        TANK.setLevel(Minecraft.getInstance().level);
        TANK.getTank().setFluid(pStack.getOrDefault(OpenBlocks.FLUID_COMPONENT, FluidStack.EMPTY));

        // this isn't available at init for some reason
        BlockModelShaper blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
        BakedModel model = blockModelShaper.getBlockModel(state);

        // yeah this is a dirty way to do it, if the model ever changes it'll need to be fixed
        RandomSource rand = RandomSource.create(42);
        if(pDisplayContext == ItemDisplayContext.GUI)
            // i don't know why the model doesn't do it
            Lighting.setupFor3DItems();
        VertexConsumer buf = pBuffer.getBuffer(RenderTypeHelper.getEntityRenderType(RenderType.SOLID, false));
        for (BakedQuad quad : model.getQuads(state, null, rand, modelData, RenderType.SOLID))
            buf.putBulkData(pPoseStack.last(), quad, 1, 1, 1, 1, pPackedLight, pPackedOverlay, false);
        tankRenderer.render(TANK, 1, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }
}
