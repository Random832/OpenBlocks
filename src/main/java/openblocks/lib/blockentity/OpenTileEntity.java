package openblocks.lib.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import openblocks.lib.utils.BlockUtils;

public abstract class OpenTileEntity extends BlockEntity {
    public OpenTileEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    /** Place for TE specific setup. Called once upon creation */
    public void setup() {}

/*    public Orientation getOrientation() {
        return getOrientation(getBlockState());
    }

    public Orientation getOrientation(BlockState state) {
        final Block block = state.getBlock();
        if (!(block instanceof OpenBlock)) return Orientation.XP_YP;
        final OpenBlock openBlock = (OpenBlock)block;
        return openBlock.getOrientation(state);
    }

    public IBlockRotationMode getRotationMode() {
        final BlockState state = world.getBlockState(pos);
        return getRotationMode(state);
    }

    public IBlockRotationMode getRotationMode(BlockState state) {
        final Block block = state.getBlock();
        if (!(block instanceof OpenBlock.Orientable)) return BlockRotationMode.NONE;
        final OpenBlock.Orientable openBlock = (OpenBlock.Orientable)block;
        return openBlock.getRotationMode();
    }

    public Direction getFront() {
        final BlockState state = world.getBlockState(pos);
        return getFront(state);
    }

    public Direction getFront(BlockState state) {
        final Block block = state.getBlock();
        if (!(block instanceof OpenBlock)) return Direction.NORTH;
        final OpenBlock openBlock = (OpenBlock)block;
        return openBlock.getFront(state);
    }

    public Direction getBack() {
        return getFront().getOpposite();
    }

    public LocalDirections getLocalDirections() {
        final BlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        if (!(block instanceof OpenBlock)) return LocalDirections.fromFrontAndTop(Direction.NORTH, Direction.UP);
        final OpenBlock openBlock = (OpenBlock)block;
        return openBlock.getLocalDirections(state);
    }

    public boolean isAddedToWorld() {
        return world != null;
    }*/

    protected BlockEntity getTileEntity(BlockPos blockPos) {
        return (level != null && level.isLoaded(blockPos))? level.getBlockEntity(blockPos) : null;
    }

    public BlockEntity getTileInDirection(Direction direction) {
        return getTileEntity(worldPosition.relative(direction));
    }

   /* public boolean isAirBlock(Direction direction) {
        return level != null && world.isAirBlock(getPos().offset(direction));
    }*/

    protected void playSoundAtBlock(SoundEvent sound, SoundSource category, float volume, float pitch) {
        BlockUtils.playSoundAtPos(level, worldPosition, sound, category, volume, pitch);
    }

    protected void playSoundAtBlock(SoundEvent sound, float volume, float pitch) {
        playSoundAtBlock(sound, SoundSource.BLOCKS, volume, pitch);
    }

    /*protected void spawnParticle(IParticleData particle, double dx, double dy, double dz, double vx, double vy, double vz) {
        world.addParticle(particle, pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz, vx, vy, vz);
    }

    protected void spawnParticle(IParticleData particle, double vx, double vy, double vz) {
        spawnParticle(particle, 0.5, 0.5, 0.5, vx, vy, vz);
    }

    public void sendBlockEvent(int event, int param) {
        world.addBlockEvent(pos, getBlockState().getBlock(), event, param);
    }*/

    public AABB getBB() {
        return new AABB(worldPosition);
    }

 /*   @Override
    public IRpcTarget createRpcTarget() {
        return new TileEntityRpcTarget(this);
    }

    public <T> T createProxy(final PacketDistributor.PacketTarget sender, Class<? extends T> mainIntf, Class<?>... extraIntf) {
        TypeUtils.isInstance(this, mainIntf, extraIntf);
        return RpcCallDispatcher.instance().createProxy(createRpcTarget(), sender, mainIntf, extraIntf);
    }

    public <T> T createClientRpcProxy(Class<? extends T> mainIntf, Class<?>... extraIntf) {
        return createProxy(PacketDistributor.SERVER.noArg(), mainIntf, extraIntf);
    }

    public <T> T createServerRpcProxy(Class<? extends T> mainIntf, Class<?>... extraIntf) {
        final ChunkManager chunkManager = ((ServerWorld)getWorld()).getChunkProvider().chunkManager;
        return createProxy(PacketDistributor.NMLIST.with(() ->
                        chunkManager.getTrackingPlayers(new ChunkPos(pos), false)
                                .map(p -> p.connection.netManager)
                                .collect(Collectors.toList())),
                mainIntf, extraIntf);
    }*/

    public void markUpdated() {
        setChanged();
    }

    /*protected IInventoryCallback createInventoryCallback() {
        return (inventory, slotNumber) -> markUpdated();
    }

    protected GenericInventory registerInventoryCallback(GenericInventory inventory) {
        return inventory.addCallback(createInventoryCallback());
    }*/

    public boolean isValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }
}