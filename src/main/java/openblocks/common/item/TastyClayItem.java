package openblocks.common.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import openblocks.OpenBlocks;
import openblocks.rubbish.BrickManager;
import openblocks.rubbish.BrickManager.BowelContents;

public class TastyClayItem extends Item {
    public TastyClayItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if(!pLevel.isClientSide) {
            BowelContents contents = BrickManager.getProperty(pLivingEntity);
            contents.brickCount++;
        }
        return super.finishUsingItem(pStack, pLevel, pLivingEntity);
    }
}