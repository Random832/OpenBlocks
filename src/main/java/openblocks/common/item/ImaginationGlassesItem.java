package openblocks.common.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import openblocks.OpenBlocks;
import openblocks.common.blockentity.ImaginaryBlockEntity;
import openblocks.lib.item.ICreativeVariantsItem;
import org.jetbrains.annotations.Nullable;

public abstract class ImaginationGlassesItem extends Item {
    public ImaginationGlassesItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    public abstract boolean checkBlock(ImaginaryBlockEntity.Property property, ItemStack stack, ImaginaryBlockEntity te);

    public static class Pencil extends ImaginationGlassesItem {
        public Pencil(Properties pProperties) {
            super(pProperties);
        }

        public boolean checkBlock(ImaginaryBlockEntity.Property property, ItemStack stack, ImaginaryBlockEntity te) {
            return te.isPencil() ^ te.isInverted();
        }
    }

    public static class Crayon extends ImaginationGlassesItem implements ICreativeVariantsItem {
        public Crayon(Properties pProperties) {
            super(pProperties);
        }

        public boolean checkBlock(ImaginaryBlockEntity.Property property, ItemStack stack, ImaginaryBlockEntity te) {
            return (!te.isPencil() && getGlassesColor(stack) == te.color) ^ te.isInverted();
        }

        @Nullable
        public DyeColor getGlassesColor(ItemStack stack) {
            return stack.get(OpenBlocks.IMAGINARY_COLOR);
        }

        @Override
        public void fillItemGroup(CreativeModeTab.Output pOutput) {
            for (DyeColor color : DyeColor.values()) {
                ItemStack stack = new ItemStack(this);
                stack.set(OpenBlocks.IMAGINARY_COLOR, color);
                pOutput.accept(stack);
            }
        }
    }


    public static class Technicolor extends ImaginationGlassesItem {
        public Technicolor(Properties pProperties) {
            super(pProperties);
        }

        public boolean checkBlock(ImaginaryBlockEntity.Property property, ItemStack stack, ImaginaryBlockEntity te) {
            if (property == ImaginaryBlockEntity.Property.VISIBLE) return true;
            return te.isInverted();
        }
    }

    public static class Bastard extends ImaginationGlassesItem {
        public Bastard(Properties pProperties) {
            super(pProperties);
        }

        public boolean checkBlock(ImaginaryBlockEntity.Property property, ItemStack stack, ImaginaryBlockEntity te) {
            return true;
        }
    }

}
