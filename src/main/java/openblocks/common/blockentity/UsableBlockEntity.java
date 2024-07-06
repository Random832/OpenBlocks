package openblocks.common.blockentity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

public interface UsableBlockEntity {
    ItemInteractionResult useItemOn(Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult, ItemStack pStack);
    InteractionResult useWithoutItem(Player pPlayer, BlockHitResult pHitResult);
}
