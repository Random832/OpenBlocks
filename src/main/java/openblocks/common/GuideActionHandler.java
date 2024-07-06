package openblocks.common;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import openblocks.common.blockentity.TileEntityGuide;
import openblocks.events.GuideActionEvent;

public class GuideActionHandler {
    public static void onEvent(GuideActionEvent evt, IPayloadContext context) {
        final Level world = context.player().getServer().getLevel(evt.dimension()); // TODO ugh

        if (world.isLoaded(evt.blockPos())) {
            final BlockEntity te = world.getBlockEntity(evt.blockPos());
            if (te instanceof TileEntityGuide guide)
                guide.onCommand(context.player(), evt.name());
        }
    }
}
