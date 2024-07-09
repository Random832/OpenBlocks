package openblocks.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import openblocks.Config;
import openblocks.ModTags;
import openblocks.common.SpongeHandler;
import openblocks.common.block.SpongeBlock;
import openblocks.lib.utils.ItemStackUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SpongeStickItem extends Item {
    private final TagKey<Fluid> tag;
    private final TagKey<Fluid> burnTag;

    public SpongeStickItem(Properties props, TagKey<Fluid> tag, @Nullable TagKey<Fluid> burnTag) {
        super(props);
        this.tag = tag;
        this.burnTag = burnTag;
    }

    public static SpongeStickItem makeWaterSponge(Properties props) {
        return new SpongeStickItem(props, ModTags.SPONGE_EFFECTIVE, ModTags.SPONGE_BURNS);
    }

    public static SpongeStickItem makeLavaSponge(Properties props) {
        return new SpongeStickItem(props, ModTags.LAVA_SPONGE_EFFECTIVE, null);
    }

    public int getMaxDamage(ItemStack stack) {
        return ItemStackUtils.configurableMaxDamage(stack, Config.spongeStickMaxDamage);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.ANY);
        if (blockhitresult.getType() != HitResult.Type.BLOCK)
            return InteractionResultHolder.pass(stack);

        BlockPos pos = blockhitresult.getBlockPos();

        if(pLevel instanceof ServerLevel serverLevel) {
            SpongeHandler.CleanupResult result = SpongeHandler.doCleanup(serverLevel, pos, Config.spongeStickRange, tag, burnTag, Config.spongeStickBlockUpdate);
            if(!result.empty())
                ItemStackUtils.damageItem(stack, 1, pPlayer, pHand);
            if(result.burn()) {
                pPlayer.igniteForSeconds(6);
                if (!stack.isEmpty() && !pPlayer.hasInfiniteMaterials()) {
                    stack.setCount(0);
                    pPlayer.onEquippedItemBroken(this, ItemStackUtils.handToSlot(pHand));
                    return InteractionResultHolder.consume(ItemStack.EMPTY);
                }
            }
            return InteractionResultHolder.consume(stack);
        } else return InteractionResultHolder.success(stack);
    }
}