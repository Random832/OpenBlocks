package openblocks.common.block;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockBuilderGuide extends BlockGuide {

	public BlockBuilderGuide(final Block.Properties properties) {
		super(properties);
	}

	@Override
    public boolean areButtonsActive(Entity entity) {
		if (entity instanceof Player player) {
			final ItemStack heldItem = player.getMainHandItem();
			return heldItem.isEmpty() || !(heldItem.getItem() instanceof BlockItem);
		}
		return false;
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
		final float x = pos.getX() + 0.5f;
		final float y = pos.getY() + 0.7f;
		final float z = pos.getZ() + 0.5f;

		world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
		if(world.isClientSide) ClientMiniProxy.spawnGreenFlameParticle(x, y, z);
	}

	private static class ClientMiniProxy {
		private static void spawnGreenFlameParticle(float x, float y, float z) {
			final Minecraft mc = Minecraft.getInstance();
			if (mc.options.particles().get() == ParticleStatus.ALL) {
				final Particle flame = mc.particleEngine.createParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
				if (flame != null) {
					flame.setColor(0, 1, 1);
				}
			}
		}
	}
}
