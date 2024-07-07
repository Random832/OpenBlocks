package openblocks.lib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

public class CompatibilityUtils {
    @Nullable
    public static DyeColor colorFromStack(ItemStack heldStack) {
        if(heldStack.getItem() instanceof DyeItem di) {
            return di.getDyeColor();
        } else {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    public static Holder<Block> asHolder(Block block) {
        return block.builtInRegistryHolder();
    }
}
