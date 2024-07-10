package openblocks.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class DebugProbeItem extends Item {
    public DebugProbeItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        final BlockPos pos = pContext.getClickedPos();
        final Level level = pContext.getLevel();
        final Player player = pContext.getPlayer();
        if(player == null) return InteractionResult.FAIL;
        if(level.isClientSide)
            return InteractionResult.SUCCESS;
        @Nullable IFluidHandler cap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, pContext.getClickedFace());
        if(cap != null) {
            if(cap.getTanks() == 0)
                player.sendSystemMessage(Component.empty().append(level.getBlockState(pos).getBlock().getName()).append(" has a fluid handler but no slots."));
            else
                for(int i=0; i<cap.getTanks(); i++) {
                    FluidStack fluid = cap.getFluidInTank(i);
                    player.sendSystemMessage(Component.literal("Slot" + i + ": " + fluid.getAmount() + " mb ").append(fluid.getHoverName()));
                }
        }
        return InteractionResult.CONSUME;
    }
}
