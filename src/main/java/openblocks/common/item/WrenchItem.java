package openblocks.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LeverBlock;
import openblocks.lib.block.OpenBlock;

import java.util.HashSet;
import java.util.Set;

public class WrenchItem extends Item {
    private final Set<Class<? extends Block>> sneakOnly = new HashSet<>();

    public WrenchItem(Properties pProperties) {
        super(pProperties);
        sneakOnly.add(LeverBlock.class);
        sneakOnly.add(ButtonBlock.class);
        sneakOnly.add(ChestBlock.class);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }

    private boolean requiresSneaking(final Block block) {
        return sneakOnly.stream().anyMatch(input -> input.isInstance(block));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        final Block block = context.getLevel().getBlockState(context.getClickedPos()).getBlock();

        if (requiresSneaking(block) && !context.isSecondaryUseActive()) return InteractionResult.FAIL;

        if(block instanceof OpenBlock.ToolRotatable rb) {
            if(rb.rotateWithTool(context))
                return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        return InteractionResult.FAIL;
    }

}
