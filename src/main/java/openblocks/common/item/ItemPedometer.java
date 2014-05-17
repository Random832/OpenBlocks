package openblocks.common.item;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import openblocks.Config;
import openblocks.OpenBlocks;
import openblocks.common.PedometerHandler;
import openblocks.common.PedometerHandler.PedometerData;
import openmods.utils.Units.DistanceUnit;
import openmods.utils.Units.SpeedUnit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPedometer extends Item {

	public ItemPedometer() {
		super(Config.itemPedometer);
		setMaxStackSize(1);
		setCreativeTab(OpenBlocks.tabOpenBlocks);
	}

	private Icon pedometerMoving;
	private Icon pedometerStill;

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister registry) {
		pedometerMoving = registry.registerIcon("openblocks:pedometer_moving");
		itemIcon = pedometerStill = registry.registerIcon("openblocks:pedometer_still");
	}

	private static void send(EntityPlayer player, String format, Object... args) {
		player.sendChatToPlayer(ChatMessageComponent.createFromTranslationWithSubstitutions(format, args));
	}

	private SpeedUnit speedUnit = SpeedUnit.M_PER_TICK;
	private DistanceUnit distanceUnit = DistanceUnit.M;

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (world.isRemote) {
			if (player.isSneaking()) {
				PedometerHandler.reset(world, player);
				player.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("openblocks.misc.pedometer.tracking_started"));
			} else {
				PedometerData result = PedometerHandler.getPedometerData(player);
				if (result != null) {
					player.sendChatToPlayer(ChatMessageComponent.createFromText(""));
					send(player, "openblocks.misc.pedometer.start_point", String.format("%.1f %.1f %.1f", result.startingPoint.xCoord, result.startingPoint.yCoord, result.startingPoint.zCoord));

					send(player, "openblocks.misc.pedometer.avg_speed", speedUnit.format(result.averageSpeed()));
					send(player, "openblocks.misc.pedometer.straigh_line_speed", speedUnit.format(result.straightLineSpeed()));
					send(player, "openblocks.misc.pedometer.speed", speedUnit.format(result.currentSpeed));

					send(player, "openblocks.misc.pedometer.total_distance", distanceUnit.format(result.totalDistance));
					send(player, "openblocks.misc.pedometer.straght_line", distanceUnit.format(result.straightLineDistance));

					send(player, "openblocks.misc.pedometer.total_time", result.totalTime);
				} else {
					player.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("openblocks.misc.pedometer.tracking_not_started"));
				}
			}
		}
		return stack;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slotId, boolean isSelected) {
		if (world.isRemote) PedometerHandler.updatePedometerData(entity);
	}

	@Override
	public Icon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
		if (player.motionX * player.motionX + player.motionY * player.motionY + player.motionZ * player.motionZ > 0.01) return pedometerMoving;
		return pedometerStill;
	}

}
