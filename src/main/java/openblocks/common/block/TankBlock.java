package openblocks.common.block;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TintedGlassBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import openblocks.Config;
import openblocks.OpenBlocks;
import openblocks.common.blockentity.TankBlockEntity;
import openblocks.lib.block.OpenEntityBlock;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

public class TankBlock extends OpenEntityBlock<TankBlockEntity> {
	private final Set<Vector3i> OFFSETS = ImmutableSet.of(
			new Vector3i(-1, -1, 0),
			new Vector3i(-1, +1, 0),
			new Vector3i(+1, -1, 0),
			new Vector3i(+1, +1, 0),

			new Vector3i(-1, 0, -1),
			new Vector3i(+1, 0, -1),
			new Vector3i(+1, 0, +1),
			new Vector3i(-1, 0, +1),

			new Vector3i(0, -1, -1),
			new Vector3i(0, -1, +1),
			new Vector3i(0, +1, +1),
			new Vector3i(0, +1, -1)
	);

	public TankBlock(final Block.Properties properties) {
		super(properties, OpenBlocks.TANK_BE);
	}

	@Override
	public boolean hasDynamicLightEmission(BlockState state) {
		return true;
	}

	@Override
	public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
		if (!Config.tanksEmitLight) return 0;
		AuxiliaryLightManager manager = world.getAuxLightManager(pos);
		return manager != null ? manager.getLightAt(pos) : 0;
    }

	// TODO do we want to make these dynamic?
	@Override
	protected boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return true;
	}

	@Override
	protected float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return 0.8f;
	}

	@Override
	protected VoxelShape getVisualShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return Shapes.empty();
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader world, BlockPos pos, Player player) {
		ItemStack result = new ItemStack(this);
		TankBlockEntity tile = getBlockEntity(world, pos);
		if (tile != null) {
			FluidTank tank = tile.getTank();
			if (tank.getFluidAmount() > 0)
				result.set(OpenBlocks.FLUID_COMPONENT, SimpleFluidContent.copyOf(tank.getFluid()));
		}
		return result;
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState pState) {
		return true;
	}

    @Override
	protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		TankBlockEntity tile = getBlockEntity(world, pos);
		if(tile == null) return 0;
		double value = tile.getFluidRatio() * 15;
		if (value == 0) {
			return 0;
		}
		int trunc = Mth.floor(value);
		return Math.max(trunc, 1);
	}

	@Override
	protected BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
		if (facingState.isAir() || facingState.is(OpenBlocks.TANK_BLOCK)) {
			TankBlockEntity tile = getBlockEntity(world, pos);
			if (tile != null) {
				tile.requestModelDataUpdate();
			}
		}
		return super.updateShape(stateIn, facing, facingState, world, pos, facingPos);
	}

	@Override
	protected void updateIndirectNeighbourShapes(BlockState state, LevelAccessor worldIn, BlockPos pos, int flags, int recursionLeft) {
		final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (final Vector3i offset : OFFSETS) {
			final BlockState diagonal = worldIn.getBlockState(cursor.setWithOffset(pos, offset.x, offset.y, offset.z));
			if (diagonal.is(OpenBlocks.TANK_BLOCK)) {
				TankBlockEntity tile = getBlockEntity(worldIn, cursor);
				if (tile != null) {
					tile.requestModelDataUpdate();
				}
			}
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new TankBlockEntity(pPos, pState);
	}

	@Override
	protected @Nullable BlockEntityTicker<TankBlockEntity> makeTicker(Level level, BlockState state) {
		return level.isClientSide ? TankBlockEntity::clientTick : TankBlockEntity::serverTick;
	}

	@Override
	protected boolean skipRendering(BlockState pState, BlockState pAdjacentState, Direction pDirection) {
		return pAdjacentState.is(this);
	}
}
