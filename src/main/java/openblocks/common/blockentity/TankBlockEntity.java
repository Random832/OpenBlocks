package openblocks.common.blockentity;

import static openblocks.common.item.TankItem.TANK_TAG;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import openblocks.Config;
import openblocks.OpenBlocks;
import openblocks.lib.blockentity.ICustomBreakDrops;
import openblocks.lib.blockentity.INeighbourAwareTile;
import openblocks.lib.blockentity.IPlaceAwareTile;
import openblocks.lib.blockentity.OpenTileEntity;
import openblocks.lib.model.variant.VariantModelState;
import openblocks.lib.utils.EnchantmentUtils;
import openblocks.lib.utils.FluidXpUtils;
import openblocks.client.renderer.blockentity.tank.ITankConnections;
import openblocks.client.renderer.blockentity.tank.ITankRenderFluidData;
import openblocks.client.renderer.blockentity.tank.NeighbourMap;
import openblocks.client.renderer.blockentity.tank.TankRenderLogic;
import openblocks.lib.utils.CompatibilityUtils;

public class TankBlockEntity extends OpenTileEntity implements UsableBlockEntity, IPlaceAwareTile, INeighbourAwareTile, ICustomBreakDrops {

	private final TankRenderLogic renderLogic;

	@Override
	public void clearRemoved() {
		super.clearRemoved();

		needsNeighbourRecheck = true;
		if (level.isClientSide) renderLogic.initialize(level, worldPosition);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (level.isClientSide) renderLogic.invalidateConnections();
	}

	@Nullable
	protected TankBlockEntity getNeighbourTank(BlockPos pos) {
		if (!level.isLoaded(pos)) return null;

		LevelChunk chunk = level.getChunkAt(pos);
		BlockEntity te = chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
		return (te instanceof TankBlockEntity) ? (TankBlockEntity) te : null;
	}

	private static final int SYNC_THRESHOLD = 8;
	private static final int UPDATE_THRESHOLD = 20;

	FluidTank tank = new FluidTank(getTankCapacity()) {
		@Override
		protected void onContentsChanged() {
			markContentsUpdated();
		}
	};

	private boolean hasPendingFluidTransfers = true;

	private int ticksSinceLastSync = hashCode() % SYNC_THRESHOLD;

	private boolean needsSync;

	private int ticksSinceLastNeighbourUpdate = hashCode() % UPDATE_THRESHOLD;

	private boolean hasPendingNeighbourUpdate;

	private boolean needsNeighbourRecheck;

	private boolean needsSignalUpdate;

	public TankBlockEntity(BlockPos pPos, BlockState pBlockState) {
		super(OpenBlocks.TANK_BE.get(), pPos, pBlockState);
		renderLogic = new TankRenderLogic(tank);
	}

	public double getFluidRatio() {
		return (double) tank.getFluidAmount() / (double) tank.getCapacity();
	}

	public static int getTankCapacity() {
		return FluidType.BUCKET_VOLUME * Config.bucketsPerTank;
	}

	public int getFluidLightLevel() {
		FluidStack stack = tank.getFluid();
		Fluid fluid = stack.getFluid();
		return fluid.getFluidType().getLightLevel();
	}

	@Nullable
	public ITankRenderFluidData getRenderFluidData() {
		return renderLogic.getTankRenderData();
	}

	public ITankConnections getTankConnections() {
		return renderLogic.getTankConnections();
	}

	@Override
	public ModelData getModelData() {
		// TODO maybe cache?
		VariantModelState state = new NeighbourMap(level, worldPosition, tank.getFluid()).getState();
		return ModelData.builder().with(VariantModelState.PROPERTY, () -> state).build();
	}

	private void updateModelState() {
		requestModelDataUpdate();
	}

	public boolean accepts(FluidStack liquid) {
		if (liquid.isEmpty()) return true; // ugh, needed for renderlogic
		final FluidStack ownFluid = tank.getFluid();
		return ownFluid.isEmpty() || FluidStack.isSameFluidSameComponents(ownFluid, liquid);
	}

	boolean containsFluid(FluidStack liquid) {
		if (liquid.isEmpty()) return false;
		final FluidStack ownFluid = tank.getFluid();
		return !ownFluid.isEmpty() && FluidStack.isSameFluidSameComponents(ownFluid, liquid);
	}

	public FluidTank getTank() {
		return tank;
	}

	public CompoundTag getItemNBT() {
		CompoundTag nbt = new CompoundTag();
		tank.writeToNBT(level.registryAccess(), nbt);
		return nbt;
	}

	@Override
	public void onNeighbourChanged(BlockPos neighbourPos, Block neighbourBlock) {
		hasPendingFluidTransfers = true;
		needsNeighbourRecheck = true;
	}

	@Override
	public void onBlockPlacedBy(BlockState state, LivingEntity placer, ItemStack stack) {
		CompatibilityUtils.updateTankFromStack(stack, this.tank);
	}


	@Nullable
	private static TankBlockEntity getValidTank(final BlockEntity neighbor) {
		return (neighbor instanceof TankBlockEntity && !neighbor.isRemoved()) ? (TankBlockEntity) neighbor : null;
	}

	@Nullable
	private TankBlockEntity getTankInDirection(Direction direction) {
		final BlockEntity neighbor = getTileInDirection(direction);
		return getValidTank(neighbor);
	}

	@Nullable
	public TankBlockEntity getTankInDirection(int dx, int dy, int dz) {
		final BlockEntity neighbor = getTileEntity(this.worldPosition.offset(dx, dy, dz));
		return getValidTank(neighbor);
	}


	@Override
	public ItemInteractionResult useItemOn(Player player, InteractionHand hand, BlockHitResult hit, ItemStack heldItem) {
		if (FluidUtil.interactWithFluidHandler(player, hand, tank))
			return ItemInteractionResult.SUCCESS;
		else
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public InteractionResult useWithoutItem(Player player, BlockHitResult hit) {
		final FluidStack fluid = tank.getFluid();
		final Optional<FluidXpUtils.IFluidXpConverter> maybeConverter = FluidXpUtils.getConverter(fluid);
		if (maybeConverter.isPresent()) {
			final FluidXpUtils.IFluidXpConverter converter = maybeConverter.get();
			int requiredXp = Mth.ceil(player.getXpNeededForNextLevel() * (1 - player.experienceProgress));
			int requiredXpFluid = converter.xpToFluid(requiredXp);

			IFluidHandler handler = CompatibilityUtils.getFluidHandler(level, worldPosition, null);
			FluidStack drained = handler.drain(requiredXpFluid, IFluidHandler.FluidAction.SIMULATE);
			if (!drained.isEmpty()) {
				int xp = converter.fluidToXp(drained.getAmount());
				if (xp > 0) {
					int actualDrain = converter.xpToFluid(xp);
					EnchantmentUtils.addPlayerXP(player, xp);
					handler.drain(actualDrain, IFluidHandler.FluidAction.EXECUTE);
					return InteractionResult.SUCCESS;
				}
			}
		}
		return InteractionResult.CONSUME;
	}

	public static void tickerTick(Level pLevel, BlockPos pPos, BlockState pState, TankBlockEntity pBlockEntity) {
		pBlockEntity.tick();
	}

	public void tick() {
		ticksSinceLastSync++;
		ticksSinceLastNeighbourUpdate++;

		if (Config.shouldTanksUpdate && !level.isClientSide && hasPendingFluidTransfers) {
			hasPendingFluidTransfers = false;

			FluidStack contents = tank.getFluid();
			if (!contents.isEmpty() && worldPosition.getY() > level.getMinBuildHeight()) {
				tryFillBottomTank(contents);
				contents = tank.getFluid();
			}

			if (!contents.isEmpty()) {
				tryBalanceNeighbors(contents);
			}

			needsSync = true;
			markUpdated();
		}

		if (needsSync && !level.isClientSide && ticksSinceLastSync > SYNC_THRESHOLD) {
			needsSync = false;
			sync();
		}

		if (hasPendingNeighbourUpdate && ticksSinceLastNeighbourUpdate > UPDATE_THRESHOLD) {
			hasPendingNeighbourUpdate = false;
			ticksSinceLastNeighbourUpdate = 0;
			level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
		}

		if (level.isClientSide)
			renderLogic.validateConnections(level, worldPosition);

		if (needsNeighbourRecheck) {
			// tank.updateNeighbours(level, worldPosition); // checked twice: this code didn't actually *do* anything
			needsNeighbourRecheck = false;
			if (level.isClientSide) {
				updateModelState();
				level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
			}
		}
	}

	private boolean tryGetNeighbor(List<TankBlockEntity> result, FluidStack fluid, Direction side) {
		TankBlockEntity neighbor = getTankInDirection(side);
		if (neighbor != null && neighbor.accepts(fluid)) {
			result.add(neighbor);
			return true;
		} else {
			return false;
		}
	}

	private void tryBalanceNeighbors(FluidStack contents) {
		List<TankBlockEntity> neighbors = Lists.newArrayList();
		tryGetNeighbor(neighbors, contents, Direction.NORTH);
		tryGetNeighbor(neighbors, contents, Direction.SOUTH);
		tryGetNeighbor(neighbors, contents, Direction.EAST);
		tryGetNeighbor(neighbors, contents, Direction.WEST);

		final int count = neighbors.size();
		if (count == 0) return;

		int sum = contents.getAmount();
		for (TankBlockEntity n : neighbors)
			sum += n.tank.getFluidAmount();

		final int suggestedAmount = sum / (count + 1);
		if (Math.abs(suggestedAmount - contents.getAmount()) < Config.tankFluidUpdateThreshold)
			return; // Don't balance small amounts to reduce server load

		FluidStack suggestedStack = contents.copy();
		suggestedStack.setAmount(suggestedAmount);

		for (TankBlockEntity n : neighbors) {
			int amount = n.tank.getFluidAmount();
			int diff = amount - suggestedAmount;
			if (diff != 1 && diff != 0 && diff != -1) {
				n.tank.setFluid(suggestedStack.copy());
				n.tankChanged();
				sum -= suggestedAmount;
				n.hasPendingFluidTransfers = true;
			} else {
				sum -= amount;
			}
		}

		FluidStack s = tank.getFluid();
		if (sum != s.getAmount()) {
			s.setAmount(sum);
			tankChanged();
		}
	}

	private void notifyNeigbours() {
		hasPendingNeighbourUpdate = true;
	}

	private void tankChanged() {
		notifyNeigbours();
		needsSync = true;
	}

	private void markContentsUpdated() {
		notifyNeigbours();
		hasPendingFluidTransfers = true;
	}

	private void tryFillBottomTank(FluidStack fluid) {
		BlockEntity te = level.getBlockEntity(worldPosition.below());
		if (te instanceof TankBlockEntity) {
			int amount = ((TankBlockEntity) te).internalFill(fluid, IFluidHandler.FluidAction.EXECUTE);
			if (amount > 0) internalDrain(amount, IFluidHandler.FluidAction.EXECUTE);
		}
	}

	private FluidStack internalDrain(int amount, IFluidHandler.FluidAction action) {
		FluidStack drained = tank.drain(amount, action);
		if (!drained.isEmpty() && action.execute())
			markContentsUpdated();
		return drained;
	}

	private void drainFromColumn(FluidStack needed, IFluidHandler.FluidAction action) {
		if (!containsFluid(needed) || needed.isEmpty())
			return;

		if (worldPosition.getY() < 255) {
			BlockEntity te = level.getBlockEntity(worldPosition.above());
			if (te instanceof TankBlockEntity tt) tt.drainFromColumn(needed, action);
		}

		if (needed.isEmpty()) return;

		FluidStack drained = internalDrain(needed.getAmount(), action);
		if (drained.isEmpty()) return;

		needed.shrink(drained.getAmount());
	}

	private int internalFill(FluidStack resource, IFluidHandler.FluidAction action) {
		int amount = tank.fill(resource, action);
		if (amount > 0 && action.execute())
			markContentsUpdated();
		return amount;
	}

	private void fillColumn(FluidStack resource, IFluidHandler.FluidAction action) {
		if (!accepts(resource) || resource.isEmpty())
			return;

		int amount = internalFill(resource, action);

		resource.shrink(amount);

		if (!resource.isEmpty() && worldPosition.getY() < 255) {
			BlockEntity te = level.getBlockEntity(worldPosition.above());
			if (te instanceof TankBlockEntity tt) tt.fillColumn(resource, action);
		}
	}


	@Override
	public List<ItemStack> getDrops(List<ItemStack> originalDrops) {
		ItemStack stack = new ItemStack(OpenBlocks.TANK_BLOCK);

		if (tank.getFluidAmount() > 0)
			CompatibilityUtils.saveTankToStack(tank, stack);

		return List.of(stack);
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
		super.saveAdditional(pTag, pRegistries);
		CompoundTag tankTag = tank.writeToNBT(pRegistries, new CompoundTag());
		pTag.put(TANK_TAG, tankTag);
	}

	@Override
	protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
		super.loadAdditional(pTag, pRegistries);
		if (pTag.contains(TANK_TAG, CompoundTag.TAG_COMPOUND)) {
			tank.readFromNBT(pRegistries, pTag.getCompound(TANK_TAG));
		}
		if (level != null && level.isClientSide) {
			renderLogic.updateFluid(tank.getFluid());
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
		CompoundTag tankTag = tank.writeToNBT(pRegistries, new CompoundTag());
		CompoundTag beTag = new CompoundTag();
		beTag.put(TANK_TAG, tankTag);
		return beTag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
		loadAdditional(tag, lookupProvider);
	}

	void sync() {
		if (level.isClientSide) return;
		ChunkPos chunkPos = new ChunkPos(worldPosition);
		Packet<ClientGamePacketListener> packet = getUpdatePacket();
		for (ServerPlayer player : ((ServerLevel) level).getChunkSource().chunkMap.getPlayers(chunkPos, false)) {
			player.connection.send(packet);
		}
	}

	public void initializeForBewlr() {
		renderLogic.initialize(null, BlockPos.ZERO);
	}

	public class ColumnFluidHandler implements IFluidHandler {
		@Override
		public int getTanks() {
			return 1;
		}

		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tankId) {
			return TankBlockEntity.this.tank.getFluidInTank(tankId);
		}

		@Override
		public int getTankCapacity(int tankId) {
			return TankBlockEntity.this.tank.getTankCapacity(tankId);
		}

		@Override
		public boolean isFluidValid(int tankId, @Nonnull FluidStack stack) {
			return TankBlockEntity.this.tank.isFluidValid(tankId, stack);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			if (resource.isEmpty())
				return 0;
			FluidStack copy = resource.copy();
			fillColumn(copy, action);

			return resource.getAmount() - copy.getAmount();
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			if (maxDrain <= 0)
				return FluidStack.EMPTY;

			FluidStack contents = TankBlockEntity.this.tank.getFluid();
			if (contents.isEmpty())
				return FluidStack.EMPTY;

			FluidStack needed = contents.copy();
			needed.setAmount(maxDrain);

			drainFromColumn(needed, action);

			needed.setAmount(maxDrain - needed.getAmount());
			return needed;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			if (resource.isEmpty())
				return FluidStack.EMPTY;

			FluidStack needed = resource.copy();
			drainFromColumn(needed, action);

			needed.setAmount(resource.getAmount() - needed.getAmount());
			return needed;
		}
	}
}