package openblocks.common.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import openblocks.common.block.BlockGuide;
import openblocks.common.blockentity.TileEntityGuide;
import openblocks.common.support.GuideShape;

import java.util.List;

public class ItemGuide extends BlockItem {
    public ItemGuide(BlockGuide block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext pContext, List<Component> result, TooltipFlag pTooltipFlag) {
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null) {
            CompoundTag tag = data.getUnsafe();
            final int posX = tag.getInt(TileEntityGuide.TAG_POS_X);
            final int posY = tag.getInt(TileEntityGuide.TAG_POS_Y);
            final int posZ = tag.getInt(TileEntityGuide.TAG_POS_Z);

            final int negX = -tag.getInt(TileEntityGuide.TAG_NEG_X);
            final int negY = -tag.getInt(TileEntityGuide.TAG_NEG_Y);
            final int negZ = -tag.getInt(TileEntityGuide.TAG_NEG_Z);

            result.add(Component.translatable("openblocks.misc.box", negX, negY, negZ, posX, posY, posZ));
            if (tag.contains(TileEntityGuide.TAG_COLOR)) {
                result.add(Component.translatable("openblocks.misc.color", Component.literal(String.format("%06X", tag.getInt(TileEntityGuide.TAG_COLOR)))));
            }

            if (tag.contains(TileEntityGuide.TAG_SHAPE)) {
                String mode = tag.getString(TileEntityGuide.TAG_SHAPE);
                try {
                    GuideShape shape = GuideShape.valueOf(mode);
                    result.add(Component.translatable("openblocks.misc.shape", shape.getLocalizedName()));
                } catch (IllegalArgumentException e) {}
            }
        }
    }

}
