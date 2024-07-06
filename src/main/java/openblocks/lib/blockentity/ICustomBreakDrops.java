package openblocks.lib.blockentity;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ICustomBreakDrops {
    List<ItemStack> getDrops(List<ItemStack> originalDrops);
}