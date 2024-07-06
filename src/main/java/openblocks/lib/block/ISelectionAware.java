package openblocks.lib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

public interface ISelectionAware {
    @OnlyIn(Dist.CLIENT)
    boolean onSelected(Level world, BlockPos blockPos, RenderHighlightEvent evt);
}