package openblocks.lib.blockentity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

@Deprecated
public interface IActivateAwareTile {
    InteractionResult onBlockActivated(Player player, InteractionHand hand, BlockHitResult hit);
}