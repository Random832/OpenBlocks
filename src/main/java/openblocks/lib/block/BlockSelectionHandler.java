package openblocks.lib.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import openblocks.common.block.BlockGuide;
import openblocks.lib.geometry.BlockSpaceTransform;
import openblocks.lib.geometry.Hitbox;
import openblocks.lib.geometry.Orientation;
import org.jetbrains.annotations.Nullable;

import static java.awt.ComponentOrientation.getOrientation;

@EventBusSubscriber(Dist.CLIENT )
public class BlockSelectionHandler {
    @Nullable
    public static MutableComponent text;

    @SubscribeEvent
    public static void drawButtonName(RenderGuiLayerEvent.Post event) {
        if(text != null && event.getName().equals(VanillaGuiLayers.CROSSHAIR)) {
            final Font font = Minecraft.getInstance().font;
            final int width = event.getGuiGraphics().guiWidth();
            final int height = event.getGuiGraphics().guiHeight();
            event.getGuiGraphics().drawString(font, text,
                    (width - font.width(text)) / 2,
                    height / 2 + font.lineHeight,
                    0xffffffff);
        }
    }

    @SubscribeEvent
    public static void onHighlightDraw(RenderHighlightEvent.Block evt) {
        final Level level = Minecraft.getInstance().level;
        final BlockPos blockPos = evt.getTarget().getBlockPos();
        final Block block = level.getBlockState(blockPos).getBlock();

        text = null;
        if (block instanceof BlockGuide gb) {
            evt.setCanceled(handleGuide(gb, level, blockPos, evt));
        }
    }


    public static boolean handleGuide(BlockGuide gb, Level world, BlockPos pos, RenderHighlightEvent evt) {
        if (gb.areButtonsActive(evt.getCamera().getEntity())) {
            final Vec3 hitVec = evt.getTarget().getLocation();

            final Orientation orientation = gb.getOrientation(world, pos);
            final Vec3 localHit = BlockSpaceTransform.instance.mapWorldToBlock(orientation, hitVec.x - pos.getX(), hitVec.y - pos.getY(), hitVec.z - pos.getZ());
            final Hitbox clickBox = gb.findClickBox(localHit);
            if (clickBox != null) {
                // TODO does this need the raw bounding box? it *seemed* to work when it had it
                AABB selection = BlockSpaceTransform.instance.mapBlockToWorld(orientation, clickBox.getRawBoundingBox());
                double minX = selection.minX / 16;
                double minY = selection.minY / 16;
                double minZ = selection.minZ / 16;
                double maxX = selection.maxX / 16;
                double maxY = selection.maxY / 16;
                double maxZ = selection.maxZ / 16;

                double offsetX = pos.getX() - evt.getCamera().getPosition().x();
                double offsetY = pos.getY() - evt.getCamera().getPosition().y();
                double offsetZ = pos.getZ() - evt.getCamera().getPosition().z();

                final PoseStack poseStack = evt.getPoseStack();
                poseStack.pushPose();
                poseStack.translate(offsetX, offsetY, offsetZ);
                LevelRenderer.renderLineBox(
                        poseStack,
                        evt.getMultiBufferSource().getBuffer(RenderType.lines()),
                        minX, minY, minZ, maxX, maxY, maxZ,
                        0, 0, 0, 1);
                text = Component.literal(clickBox.name);
                poseStack.popPose();
            }
        }

        return false;
    }
}