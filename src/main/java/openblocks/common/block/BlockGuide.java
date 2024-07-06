package openblocks.common.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import openblocks.lib.block.*;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import openblocks.OpenBlocks;
import openblocks.client.ClientProxy;
import openblocks.common.block.rotation.BlockRotationMode;
import openblocks.common.blockentity.TileEntityGuide;
import openblocks.events.GuideActionEvent;
import openblocks.lib.geometry.BlockSpaceTransform;
import openblocks.lib.geometry.Hitbox;
import openblocks.lib.geometry.Orientation;

public class BlockGuide extends OpenEntityBlock<TileEntityGuide> {
	public BlockGuide(final Block.Properties properties) {
		super(properties, OpenBlocks.GUIDE_BE);
		//setPlacementMode(BlockPlacementMode.SURFACE);
	}

	//public BlockRotationMode getRotationMode() {
	//	return BlockRotationMode.THREE_FOUR_DIRECTIONS;
	//}

	@Override
	protected VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return Shapes.block();
	}

	@Nullable
    public Hitbox findClickBox(Vec3 pos) {
		for (Hitbox h : ClientProxy.getHitboxes(OpenBlocks.modLoc("guide_buttons"))) {
			if (h.getScaledBoundingBox().contains(pos)) {
				return h;
			}
		}

		return null;
	}

	public boolean areButtonsActive(Entity entity) {
		return true;
	}

	public Orientation getOrientation(Level world, BlockPos pos) {
		// TODO this stuff doesn't seem to work, lock it to default orientation for now
		return Orientation.XP_YP;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState pState, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (world.isClientSide) {
			if (areButtonsActive(player)) {
				final Orientation orientation = getOrientation(world, pos);
				Vec3 hitVec = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
				Vec3 localHit = BlockSpaceTransform.instance.mapWorldToBlock(orientation, hitVec.x, hitVec.y, hitVec.z);
				final Hitbox clickBox = findClickBox(localHit);
				if (clickBox != null) {
					PacketDistributor.sendToServer(new GuideActionEvent(world.dimension(), pos, clickBox.name));
				}
			}
			return InteractionResult.SUCCESS;
		}
		return super.useWithoutItem(pState, world, pos, player, hit);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack heldStack, BlockState pState, Level world, BlockPos pos, Player player, InteractionHand pHand, BlockHitResult hit) {
		if (pHand != InteractionHand.MAIN_HAND) // ???
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (player instanceof ServerPlayer serverPlayer) {
			if (!heldStack.isEmpty()) {
				final TileEntityGuide guide = getBlockEntity(world, pos);
				if (guide.onItemUse(serverPlayer, heldStack, hit)) {
					return ItemInteractionResult.SUCCESS;
				}
			}
		}

		return super.useItemOn(heldStack, pState, world, pos, player, pHand, hit);
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
		final float x = pos.getX() + 0.5f;
		final float y = pos.getY() + 0.7f;
		final float z = pos.getZ() + 0.5f;

		world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
		world.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
	}

	@Override
	protected @Nullable BlockEntityTicker<TileEntityGuide> makeTicker(Level level, BlockState state) {
		return (pLevel, pPos, pState, pBlockEntity) -> pBlockEntity.tick();
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext pContext) {
		// the rotation stuff doesn't work right, just hide it for now.
		return defaultBlockState();
	}
}
