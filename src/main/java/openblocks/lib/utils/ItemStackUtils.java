package openblocks.lib.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import openblocks.Config;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ItemStackUtils {
    public static void damageItem(ItemStack stack, int amount, Player pPlayer, @Nullable InteractionHand pHand) {
        switch(pHand) {
            case OFF_HAND -> stack.hurtAndBreak(amount, pPlayer, EquipmentSlot.OFFHAND);
            case MAIN_HAND -> stack.hurtAndBreak(amount, pPlayer, EquipmentSlot.MAINHAND);
            default -> {
                if(pPlayer.level() instanceof ServerLevel serverLevel)
                    stack.hurtAndBreak(amount, serverLevel, pPlayer, item -> {});
            }
        }
    }

    @SuppressWarnings("OptionalAssignedToNull")
    public static int configurableMaxDamage(ItemStack stack, int config) {
        final Optional<? extends Integer> componentValue = stack.getComponentsPatch().get(DataComponents.MAX_DAMAGE);
        if (componentValue == null)
            return config;
        return componentValue.map(Integer::intValue).orElse(0);
    }

    public static EquipmentSlot handToSlot(InteractionHand pHand) {
        return switch (pHand) {
            case OFF_HAND -> EquipmentSlot.OFFHAND;
            case MAIN_HAND -> EquipmentSlot.MAINHAND;
        };
    }
}
