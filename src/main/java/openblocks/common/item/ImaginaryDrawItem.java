package openblocks.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.registries.DeferredBlock;
import openblocks.OpenBlocks;
import openblocks.common.block.ImaginaryBlock;
import openblocks.common.support.ImaginaryPlacementMode;
import openblocks.lib.item.ICreativeVariantsItem;
import openblocks.lib.utils.CollectionUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ImaginaryDrawItem extends Item implements ICreativeVariantsItem {
    List<ImaginaryPlacementMode> placementModes = new ArrayList<>();

    public ImaginaryDrawItem(Properties pProperties, DeferredBlock<?>... blocks) {
        super(pProperties);
        for (Holder<Block> block : blocks)
            placementModes.add(new ImaginaryPlacementMode(block, false));
        for (Holder<Block> block : blocks)
            placementModes.add(new ImaginaryPlacementMode(block, true));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if(context.isSecondaryUseActive() && context.getPlayer() != null)
            return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
        else return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(stack);

        int uses = stack.getMaxDamage() - stack.getDamageValue();
        if (uses <= 0) {
            stack.setCount(0);
        } else if (player.isSecondaryUseActive()) {
            ImaginaryPlacementMode mode1 = stack.getOrDefault(OpenBlocks.IMAGINARY_MODE, placementModes.getFirst());
            ImaginaryPlacementMode mode2 = CollectionUtils.cycle(placementModes, mode1, false);
            if(mode1 != mode2) {
                stack.set(OpenBlocks.IMAGINARY_MODE, mode2);
                if(!world.isClientSide)
                    player.displayClientMessage(Component.translatable("openblocks.misc.mode", mode2.name()), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPlaceContext bc = new BlockPlaceContext(context);
        if (!bc.canPlace()) return InteractionResult.FAIL;
        Player player = bc.getPlayer();
        Level level = bc.getLevel();
        BlockPos pos = bc.getClickedPos();
        ItemStack stack = bc.getItemInHand();
        ImaginaryPlacementMode mode = stack.getOrDefault(OpenBlocks.IMAGINARY_MODE, placementModes.getFirst());
        Block block = mode.block().value();
        BlockState state0 = block.getStateForPlacement(bc);
        if(state0 == null) return InteractionResult.FAIL;
        if (!bc.getLevel().setBlock(bc.getClickedPos(), state0, Block.UPDATE_ALL_IMMEDIATE)) return InteractionResult.FAIL;

        BlockState state1 = level.getBlockState(pos);
        if (state1.is(state0.getBlock())) {
            state1.getBlock().setPlacedBy(level, pos, state1, player, stack);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, pos, stack);
            }
        }

        SoundType soundtype = state1.getSoundType(level, pos, context.getPlayer());
        level.playSound(player, pos, state1.getSoundType(level, pos, player).getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, state1));

        if(context.getPlayer() != null) {
            EquipmentSlot slot = switch(context.getHand()) { case OFF_HAND -> EquipmentSlot.OFFHAND; case MAIN_HAND -> EquipmentSlot.MAINHAND; };
            context.getItemInHand().hurtAndBreak(getCost(state1), context.getPlayer(), slot);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private int getCost(BlockState blockstate) {
        if(blockstate.getBlock() instanceof ImaginaryBlock ib) {
            return ib.getDurabilityCost();
        } else return 4;
    }

    @Nullable
    private BlockState getPlacementState(Block block, BlockPlaceContext pContext) {
        BlockState blockstate = block.getStateForPlacement(pContext);
        Player player = pContext.getPlayer();
        CollisionContext collisioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
        return blockstate != null && (blockstate.canSurvive(pContext.getLevel(), pContext.getClickedPos()))
                && pContext.getLevel().isUnobstructed(blockstate, pContext.getClickedPos(), collisioncontext) ? blockstate : null;
    }

    @Override
    public void fillItemGroup(CreativeModeTab.Output pOutput) {
        if(this == OpenBlocks.CRAYON.get()) {
            for (DyeColor color : DyeColor.values()) {
                ItemStack stack = new ItemStack(this);
                stack.set(OpenBlocks.IMAGINARY_COLOR, color);
                pOutput.accept(stack);
            }
        } else pOutput.accept(this);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        @Nullable DyeColor color = pStack.get(OpenBlocks.IMAGINARY_COLOR);
        ImaginaryPlacementMode mode = pStack.getOrDefault(OpenBlocks.IMAGINARY_MODE, placementModes.getFirst());
        int uses = pStack.getMaxDamage() - pStack.getDamageValue();
        pTooltipComponents.add(Component.translatable("openblocks.misc.uses", String.format("%.2f", uses / 4f)));

        if (color != null)
            pTooltipComponents.add(Component.translatable("openblocks.misc.color", String.format("#%06X", color.getTextureDiffuseColor())));

        pTooltipComponents.add(Component.translatable("openblocks.misc.mode", mode.name()));
    }
}
