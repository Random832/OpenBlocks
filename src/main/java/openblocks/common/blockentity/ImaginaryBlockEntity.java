package openblocks.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import openblocks.OpenBlocks;
import openblocks.common.item.ImaginationGlassesItem;
import openblocks.common.support.ImaginaryPlacementMode;
import openblocks.lib.blockentity.ICustomPickItem;
import openblocks.lib.blockentity.IPlaceAwareTile;
import openblocks.lib.blockentity.OpenTileEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ImaginaryBlockEntity extends OpenTileEntity implements ICustomPickItem, IPlaceAwareTile {
    public enum Property {
        VISIBLE, SELECTABLE, SOLID
    }

    @Nullable
    public DyeColor color = null;
    public boolean inverted = false;
    public float opacity; // for renderer

    public ImaginaryBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(OpenBlocks.IMAGINARY_BE.get(), pPos, pBlockState);
    }

    public boolean isInverted() {
        return inverted;
    }

    public boolean isPencil() {
        return color == null;
    }

    public int getTintColor() {
        return color == null ? 0xffffff : color.getTextureDiffuseColor();
    }

    public boolean is(Property property, LivingEntity le) {
        ItemStack helmet = le.getItemBySlot(EquipmentSlot.HEAD);
        Item item = helmet.getItem();
        if (item instanceof ImaginationGlassesItem glasses)
            return glasses.checkBlock(property, helmet, this);
        return isInverted();
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        pTag.putBoolean("inverted", inverted);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        inverted = pTag.getBoolean("inverted");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, pRegistries);
        return tag;
    }

    @Override
    public ItemStack getPickBlock(Player player) {
        ItemStack stack;
        if(isPencil()) {
            stack = OpenBlocks.PENCIL.get().getDefaultInstance();
        } else {
            stack = OpenBlocks.CRAYON.get().getDefaultInstance();
            stack.set(OpenBlocks.IMAGINARY_COLOR, color);
        }
        stack.set(OpenBlocks.IMAGINARY_MODE, new ImaginaryPlacementMode(getBlockState().getBlockHolder(), inverted));
        // TODO try to locate a matching crayon with durability or enchantments from survival player's inventory
        return stack;
    }

    @Override
    public void onBlockPlacedBy(BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        color = stack.get(OpenBlocks.IMAGINARY_COLOR);
        final ImaginaryPlacementMode mode = stack.get(OpenBlocks.IMAGINARY_MODE);
        inverted = mode != null && mode.inverted();
    }
}
