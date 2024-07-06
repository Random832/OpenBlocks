package openblocks.common.blockentity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.component.CustomData;
import openblocks.lib.blockentity.ICustomBreakDrops;
import openblocks.lib.utils.CollectionUtils;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import openblocks.OpenBlocks;
import openblocks.lib.geometry.CoordShape;
import openblocks.common.support.GuideShape;
import openblocks.lib.Log;
import openblocks.lib.blockentity.INeighbourAwareTile;
import openblocks.lib.blockentity.ITickableTileEntity;
import openblocks.lib.blockentity.OpenTileEntity;
import openblocks.lib.geometry.HalfAxis;
import openblocks.lib.geometry.Orientation;
import openblocks.lib.shapes.IShapeGenerator;
import openblocks.lib.shapes.IShapeable;
import openblocks.lib.utils.CompatibilityUtils;

public class TileEntityGuide extends OpenTileEntity implements INeighbourAwareTile, ITickableTileEntity, ICustomBreakDrops {
    private interface IShapeManipulator {
        boolean activate(TileEntityGuide te, ServerPlayer player);
    }

    private static IShapeManipulator createHalfAxisIncrementer(final HalfAxis halfAxis) {
        return (te, player) -> te.incrementHalfAxis(halfAxis, player);
    }

    private static IShapeManipulator createHalfAxisDecrementer(final HalfAxis halfAxis) {
        return (te, player) -> te.decrementHalfAxis(halfAxis, player);
    }

    private static IShapeManipulator createHalfAxisCopier(final HalfAxis halfAxis) {
        return (te, player) -> te.copyHalfAxis(halfAxis, halfAxis.negate(), player);
    }

    private static IShapeManipulator createRotationManipulator(final HalfAxis ha) {
        return (te, player) -> {
            //final Level world = te.level;
            //final BlockPos pos = te.worldPosition;
            //final BlockState state = te.getBlockState();
            //final Block block = state.getBlock();
            //if (block instanceof OpenBlock) {
            //    final Property<Orientation> orientationProperty = ((OpenBlock.Orientable)block).getOrientationProperty();
            //    final Orientation orientation = state.get(orientationProperty);
            //    final Orientation newOrientation = orientation.rotateAround(ha);
            //    world.setBlock(pos, state.setValue(orientationProperty, newOrientation));
            //    return true;
            //}

            return false;
        };
    }

    private static final Map<String, IShapeManipulator> COMMANDS;

    static {
        ImmutableMap.Builder<String, IShapeManipulator> commands = ImmutableMap.builder();

        for (HalfAxis ha : HalfAxis.VALUES) {
            final String name = ha.name().toLowerCase(Locale.ROOT);
            commands.put("inc_" + name, createHalfAxisIncrementer(ha));
            commands.put("dec_" + name, createHalfAxisDecrementer(ha));
            commands.put("copy_" + name, createHalfAxisCopier(ha));
        }

        commands.put("rotate_ccw", createRotationManipulator(HalfAxis.NEG_Y));
        commands.put("rotate_cw", createRotationManipulator(HalfAxis.POS_Y));

        commands.put("inc_mode", (te, player) -> {
            te.incrementMode(player);
            return true;
        });

        commands.put("dec_mode", (te, player) -> {
            te.decrementMode(player);
            return true;
        });

        COMMANDS = commands.build();
    }

    private static final Comparator<BlockPos> COMPARATOR = Comparator
            .<BlockPos, Integer>comparing(BlockPos::getY)    // first, go from bottom to top
            .thenComparing(o -> Math.atan2(o.getZ(), o.getX())) // then sort by angle, to make placement more intuitive
            .thenComparing(Comparator.comparing((BlockPos o) -> Mth.lengthSquared(o.getX(), o.getZ())).reversed()) // then sort by distance, far ones first
            .thenComparing(BlockPos::getX)        // then sort by x and z to make all unique BlockPosinates are included
            .thenComparing(BlockPos::getZ);

    @Nullable
    private CoordShape shape;
    @Nullable
    private CoordShape previousShape;
    @Nullable
    private CoordShape toDeleteShape;

    private float timeSinceChange = 0;
    private AABB renderAABB;

    protected IntBox posX = new IntBox(8);
    protected IntBox posY = new IntBox(8);
    protected IntBox posZ = new IntBox(8);
    protected IntBox negX = new IntBox(8);
    protected IntBox negY = new IntBox(8);
    protected IntBox negZ = new IntBox(8);
    protected GuideShape mode = GuideShape.Sphere;
    protected int color = 0xFFFFFF;
    protected boolean active = false;

    private final Map<HalfAxis, IntBox> axisDimensions = Maps.newEnumMap(HalfAxis.class);

    public TileEntityGuide(BlockPos blockPos, BlockState state) {
            this(OpenBlocks.GUIDE_BE.get(), blockPos, state);
    }

    public TileEntityGuide(final BlockEntityType<? extends TileEntityGuide> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        axisDimensions.put(HalfAxis.NEG_X, negX);
        axisDimensions.put(HalfAxis.NEG_Y, negY);
        axisDimensions.put(HalfAxis.NEG_Z, negZ);
        axisDimensions.put(HalfAxis.POS_X, posX);
        axisDimensions.put(HalfAxis.POS_Y, posY);
        axisDimensions.put(HalfAxis.POS_Z, posZ);
    }

    public int getColor() {
        return color & 0x00FFFFFF;
    }

    public GuideShape getCurrentMode() {
        return mode;
    }

    private boolean incrementHalfAxis(HalfAxis axis, ServerPlayer player) {
        final IntBox v = axisDimensions.get(axis);
        v.modify(+1);
        afterDimensionsChange(player);
        return true;

    }

    private boolean decrementHalfAxis(HalfAxis axis, ServerPlayer player) {
        final IntBox v = axisDimensions.get(axis);
        if (v.get() > 0) {
            v.modify(-1);
            afterDimensionsChange(player);
            return true;
        }
        return false;
    }

    private boolean copyHalfAxis(HalfAxis from, HalfAxis to, ServerPlayer player) {
        final IntBox fromV = axisDimensions.get(from);
        final IntBox toV = axisDimensions.get(to);
        toV.set(fromV.get());
        afterDimensionsChange(player);
        return true;
    }

    private void incrementMode(Player player) {
        incrementMode();

        displayModeChange(player);
        displayBlockCount(player);
    }

    private void decrementMode(Player player) {
        decrementMode();

        displayModeChange(player);
        displayBlockCount(player);
    }

    private void displayModeChange(Player player) {
        player.sendSystemMessage(Component.translatable("openblocks.misc.change_mode", getCurrentMode().getLocalizedName()));
    }

    private void displayBlockCount(Player player) {
        player.sendSystemMessage(Component.translatable("openblocks.misc.total_blocks", shape.size()));
    }

    public boolean shouldRender() {
        if (isRemoved()) return false; // for some reason we don't get cleaned up from the global renderers until some other block is placed here
        return true; // Config.guideRedstone == 0 || ((Config.guideRedstone < 0) ^ active.get()); TODO
    }

    @Override
    public void tick() {
        if (level.isClientSide) {
            if (timeSinceChange < 1.0) {
                timeSinceChange = (float)Math.min(1.0f, timeSinceChange + 0.1);
            }
        }
    }

    public float getTimeSinceChange() {
        return timeSinceChange;
    }

    private void recreateShape() {
        toDeleteShape = previousShape;
        previousShape = shape;
        shape = new CoordShape(generateShape());
        renderAABB = null;
    }

    private List<BlockPos> generateShape() {
        final IShapeGenerator generator = getCurrentMode().generator;

        final Set<BlockPos> uniqueResults = Sets.newHashSet();
        final IShapeable collector = (x, y, z) -> {
            if (canAddCoord(x, y, z)) {
                uniqueResults.add(new BlockPos(x, y, z));
            }
        };
        generator.generateShape(-negX.get(), -negY.get(), -negZ.get(), posX.get(), posY.get(), posZ.get(), collector);

        final List<BlockPos> sortedResults = Lists.newArrayList(uniqueResults);
        sortedResults.sort(COMPARATOR);

        final List<BlockPos> rotatedResult = Lists.newArrayList();
        final Orientation orientation = Orientation.XP_YP; //getOrientation();

        for (BlockPos c : sortedResults) {
            final int tx = orientation.transformX(c.getX(), c.getY(), c.getZ());
            final int ty = orientation.transformY(c.getX(), c.getY(), c.getZ());
            final int tz = orientation.transformZ(c.getX(), c.getY(), c.getZ());

            rotatedResult.add(new BlockPos(tx, ty, tz));
        }

        return ImmutableList.copyOf(rotatedResult);
    }

    protected boolean canAddCoord(int x, int y, int z) {
        return (x != 0) || (y != 0) || (z != 0);
    }

    @Nullable
    public CoordShape getShape() {
        return shape;
    }

    @Nullable
    public CoordShape getPreviousShape() {
        return previousShape;
    }

    @Nullable
    public CoordShape getAndDeleteShape() {
        CoordShape toDel = toDeleteShape;
        toDeleteShape = null;
        return toDel;
    }

    //@Override
    //public void updateContainingBlockInfo() {
    //    super.updateContainingBlockInfo();
    //    // remote world will be updated by desctiption packet from block rotate
    //    if (!world.isRemote) {
    //        recreateShape();
    //    }
    //}

    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        if (renderAABB == null) {
            renderAABB = createRenderAABB();
        }
        return renderAABB;
    }

    private AABB createRenderAABB() {
        double minX = 0;
        double minY = 0;
        double minZ = 0;

        double maxX = 1;
        double maxY = 1;
        double maxZ = 1;

        if (shape != null) {
            for (BlockPos c : shape.getCoords()) {
                {
                    final int x = c.getX();
                    if (maxX < x) {
                        maxX = x;
                    }
                    if (minX > x) {
                        minX = x;
                    }
                }

                {
                    final int y = c.getY();
                    if (maxY < y) {
                        maxY = y;
                    }
                    if (minY > y) {
                        minY = y;
                    }
                }

                {
                    final int z = c.getZ();
                    if (maxZ < z) {
                        maxZ = z;
                    }
                    if (minZ > z) {
                        minZ = z;
                    }
                }
            }

        }

        minX += worldPosition.getX();
        minY += worldPosition.getX();
        minZ += worldPosition.getX();
        maxX += worldPosition.getX();
        maxY += worldPosition.getX();
        maxZ += worldPosition.getX();
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public GuideShape incrementMode() {
        mode = CollectionUtils.cycle(GuideShape.VALUES, mode, false);
        recreateShape();
        sync();
        return mode;
    }

    public GuideShape decrementMode() {
        mode = CollectionUtils.cycle(GuideShape.VALUES, mode, true);
        recreateShape();
        sync();
        return mode;
    }

    private void notifyPlayer(Player player) {
        player.sendSystemMessage(Component.translatable("openblocks.misc.change_box_size",
                        -negX.get(), -negY.get(), -negZ.get(),
                        +posX.get(), +posY.get(), +posZ.get()));
        displayBlockCount(player);
    }

    private void afterDimensionsChange(Player player) {
        recreateShape();

        sync();
        notifyPlayer(player);
    }

//    @Override
//    public void onSync(Set<ISyncableObject> changes) {
//        if (changes.contains(negX) || changes.contains(negY) || changes.contains(negZ) ||
//                changes.contains(posX) || changes.contains(posY) || changes.contains(posZ) ||
//                changes.contains(mode)) {
//            recreateShape();
//            timeSinceChange = 0;
//        }
//    }

    private void updateRedstone() {
        //if (Config.guideRedstone != 0) {
        //    boolean redstoneState = level.hasNeighborSignal(worldPosition);
        //    active = redstoneState;
        //    sync();
        //}
    }

    @Override
    public void onNeighbourChanged(BlockPos neighbourPos, Block neighbourBlock) {
        updateRedstone();
    }

    @Override
    public void onLoad() {
        updateRedstone();
    }

    protected CoordShape getShapeSafe() {
        if (shape == null) {
            recreateShape();
        }
        return shape;
    }

    public boolean onItemUse(ServerPlayer player, ItemStack heldStack, final BlockHitResult hit) {
        @Nullable DyeColor dye = CompatibilityUtils.colorFromStack(heldStack);
        if (dye != null) {
            color = dye.getTextureDiffuseColor();
            sync();
            return true;
        }

        return false;
    }

    public void onCommand(Player sender, String commandId) {
        if (sender instanceof ServerPlayer serverPlayer) {
            final IShapeManipulator command = COMMANDS.get(commandId);
            if (command != null) {
                command.activate(this, serverPlayer);
            } else {
                Log.info("Player %s tried to send invalid command '%s' to guide %s", sender, commandId, this);
            }
        }
    }

    private class IntBox {
        int value;

        public IntBox(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }

        public void set(int value) {
            this.value = value;
        }

        public void modify(int i) {
            value += i;
        }
    }


    public static final String TAG_POS_X = "PosX";
    public static final String TAG_NEG_X = "NegX";
    public static final String TAG_POS_Y = "PosY";
    public static final String TAG_NEG_Y = "NegY";
    public static final String TAG_POS_Z = "PosZ";
    public static final String TAG_NEG_Z = "NegZ";
    public static final String TAG_COLOR = "Color";
    public static final String TAG_SHAPE = "Mode";

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        posX.set(tag.getInt(TAG_POS_X));
        negX.set(tag.getInt(TAG_NEG_X));
        posY.set(tag.getInt(TAG_POS_Y));
        negY.set(tag.getInt(TAG_NEG_Y));
        posZ.set(tag.getInt(TAG_POS_Z));
        negZ.set(tag.getInt(TAG_NEG_Z));
        color = tag.getInt(TAG_COLOR);
        mode = GuideShape.valueOf(tag.getString(TAG_SHAPE));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(TAG_POS_X, posX.get());
        tag.putInt(TAG_NEG_X, negX.get());
        tag.putInt(TAG_POS_Y, posY.get());
        tag.putInt(TAG_NEG_Y, negY.get());
        tag.putInt(TAG_POS_Z, posZ.get());
        tag.putInt(TAG_NEG_Z, negZ.get());
        tag.putInt(TAG_COLOR, color);
        tag.putString(TAG_SHAPE, mode.name());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        recreateShape();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        recreateShape();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, pRegistries);
        return tag;
    }

    @Override
    public List<ItemStack> getDrops(List<ItemStack> originalDrops) {
        // TODO
        ItemStack stack = new ItemStack(getBlockState().getBlock());
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, level.registryAccess());
        stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
        return List.of(stack);
    }

    private void sync() {
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

}