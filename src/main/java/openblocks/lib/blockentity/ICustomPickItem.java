package openblocks.lib.blockentity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ICustomPickItem {
    ItemStack getPickBlock(Player player);
}
