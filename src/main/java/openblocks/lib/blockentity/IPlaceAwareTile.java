package openblocks.lib.blockentity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IPlaceAwareTile {
    void onBlockPlacedBy(BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack);
}
