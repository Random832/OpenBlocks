package openblocks.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import openblocks.OpenBlocks;
import openblocks.common.PedometerHandler;
import openblocks.common.PedometerHandler.PedometerData;
import openblocks.common.PedometerHandler.PedometerState;
import openblocks.lib.utils.Units.DistanceUnit;
import openblocks.lib.utils.Units.SpeedUnit;

import javax.annotation.Nonnull;

public class PedometerItem extends Item {
    public PedometerItem(Properties pProperties) {
        super(pProperties);
    }

    private static void send(Player player, String key, Object... args) {
        player.sendSystemMessage(Component.translatable(key, args));
    }

    private static final SpeedUnit speedUnit = SpeedUnit.KM_PER_H;
    private static final DistanceUnit distanceUnit = DistanceUnit.M;

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (world.isClientSide) {
            if (player.isSecondaryUseActive()) {
                PedometerHandler.getProperty(player).reset();
                send(player, "openblocks.misc.pedometer.tracking_reset");
            } else {
                PedometerState state = PedometerHandler.getProperty(player);
                if (state.isRunning()) {
                    showPedometerData(player, state);
                } else {
                    state.init(player, world);
                    send(player, "openblocks.misc.pedometer.tracking_started");
                }
            }
        } else {
            world.playSound(null, player.blockPosition(), OpenBlocks.SOUND_PEDOMETER_USE.get(), SoundSource.PLAYERS, 1F, 1F);
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), world.isClientSide);
    }

    protected void showPedometerData(Player player, PedometerState state) {
        PedometerData result = state.getData();
        if (result == null) return;
        player.sendSystemMessage(Component.empty());
        send(player, "openblocks.misc.pedometer.start_point", String.format("%.1f %.1f %.1f", result.startingPoint.x, result.startingPoint.y, result.startingPoint.z));

        send(player, "openblocks.misc.pedometer.speed", speedUnit.format(result.currentSpeed));
        send(player, "openblocks.misc.pedometer.avg_speed", speedUnit.format(result.averageSpeed()));
        send(player, "openblocks.misc.pedometer.total_distance", distanceUnit.format(result.totalDistance));

        send(player, "openblocks.misc.pedometer.straight_line_distance", distanceUnit.format(result.straightLineDistance));
        send(player, "openblocks.misc.pedometer.straight_line_speed", speedUnit.format(result.straightLineSpeed()));

        send(player, "openblocks.misc.pedometer.last_check_speed", speedUnit.format(result.lastCheckSpeed()));
        send(player, "openblocks.misc.pedometer.last_check_distance", distanceUnit.format(result.lastCheckDistance));
        send(player, "openblocks.misc.pedometer.last_check_time", result.lastCheckTime);

        send(player, "openblocks.misc.pedometer.total_time", result.totalTime);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pLevel.isClientSide && pSlotId < 9) {
            PedometerState state = PedometerHandler.getProperty(pEntity);
            if (state.isRunning()) state.update(pEntity);
        }
    }
}