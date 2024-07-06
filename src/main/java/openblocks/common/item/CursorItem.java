package openblocks.common.item;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import openblocks.Config;
import openblocks.ModTags;
import openblocks.OpenBlocks;
import openblocks.client.ClientProxy;
import openblocks.lib.utils.EnchantmentUtils;
import openblocks.lib.utils.TranslationUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class CursorItem extends Item {
    public CursorItem(Properties props) {
        super(props);
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity entity) {
        // what? is this meant to be a cooldown?
        return 50;
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if(!pContext.isSecondaryUseActive())
            return InteractionResult.PASS;
        final ItemStack stack = pContext.getItemInHand();
        final Level level = pContext.getLevel();
        final BlockPos pos = pContext.getClickedPos();
        stack.set(OpenBlocks.TARGET_POS_COMPONENT, GlobalPos.of(level.dimension(), pos));
        stack.set(OpenBlocks.TARGET_FACE_COMPONENT, pContext.getClickedFace());
        BlockState state = level.getBlockState(pos);
        MenuProvider menuProvider = state.getMenuProvider(level, pos);
        if(menuProvider != null)
            stack.set(OpenBlocks.TARGET_NAME_COMPONENT, menuProvider.getDisplayName());
        else if(level.getBlockEntity(pos) instanceof Nameable nameable)
            stack.set(OpenBlocks.TARGET_NAME_COMPONENT, nameable.getDisplayName());
        else
            stack.set(OpenBlocks.TARGET_NAME_COMPONENT, state.getBlock().getName());
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return pStack.has(OpenBlocks.TARGET_POS_COMPONENT);
    }

    public int getBarWidth(ItemStack pStack) {
        return Math.min(13, Math.round(13 * getUses(ClientProxy.getPlayer(), pStack) / (float) Config.cursorDurabilityBar));
    }

    public int getBarColor(ItemStack pStack) {
        float full = Math.max(0, getUses(ClientProxy.getPlayer(), pStack) / (float) Config.cursorDurabilityBar);
        return Mth.hsvToRgb(full / 3, 1, 1);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        final ItemStack heldStack = player.getItemInHand(hand);

        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(heldStack);

        if (!world.isClientSide) {
            @Nullable GlobalPos gpos = heldStack.get(OpenBlocks.TARGET_POS_COMPONENT);
            if (gpos != null && gpos.dimension() == world.dimension() && world.isLoaded(gpos.pos())) {
                Direction side = heldStack.getOrDefault(OpenBlocks.TARGET_FACE_COMPONENT, Direction.UP);
                performRemoteClick(world, player, hand, gpos.pos(), side);
            }
        }

        return InteractionResultHolder.sidedSuccess(heldStack, world.isClientSide);
    }

    private void performRemoteClick(Level world, Player player, InteractionHand hand, BlockPos pos, Direction side) {
        final BlockState state = world.getBlockState(pos);
        if (state.isAir())
            return;
        double distance = Math.sqrt(player.distanceToSqr(Vec3.atCenterOf(pos)));
        if (distance <= Config.cursorDistanceLimit) {
            final int cost = (int)Math.max(0, (distance - 4) * Config.cursorCostPerMeter);
            final int playerExperience = EnchantmentUtils.getPlayerXP(player);
            if (cost <= playerExperience || player.hasInfiniteMaterials()) {
                InteractionResult result;
                MenuProvider menuProvider = state.getMenuProvider(world, pos);
                if(menuProvider != null && (Config.enableCursorMenus || state.is(ModTags.CURSOR_ENABLE_MENU)) && !state.is(ModTags.CURSOR_DISABLE_MENU)) {
                    MenuProvider remoteMenuProvider = new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return menuProvider.getDisplayName();
                        }

                        @Nullable
                        @Override
                        public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
                            final AbstractContainerMenu menu = menuProvider.createMenu(pContainerId, pPlayerInventory, pPlayer);
                            if (menu != null)
                                return new RemoteMenuWapper(menu, hand, world, Vec3.atCenterOf(pos));
                            else
                                return null;
                        }
                    };
                    player.getItemInHand(hand).set(OpenBlocks.TARGET_NAME_COMPONENT, menuProvider.getDisplayName());
                    player.openMenu(remoteMenuProvider);
                    result = InteractionResult.CONSUME;
                } else {
                    if(world.getBlockEntity(pos) instanceof Nameable nameable)
                        player.getItemInHand(hand).set(OpenBlocks.TARGET_NAME_COMPONENT, nameable.getDisplayName());
                    else
                        player.getItemInHand(hand).set(OpenBlocks.TARGET_NAME_COMPONENT, state.getBlock().getName());
                    BlockHitResult fakeHit = new BlockHitResult(Vec3.atCenterOf(pos), side, pos, false);
                    result = state.useWithoutItem(world, player, fakeHit);
                }
                if(result.consumesAction() && !player.hasInfiniteMaterials())
                    EnchantmentUtils.addPlayerXP(player, -cost);
            }
        }
    }

    private int getUses(Player player, ItemStack pStack) {
        @Nullable GlobalPos pos = pStack.get(OpenBlocks.TARGET_POS_COMPONENT);
        if(pos == null) return -1;
        if(pos.dimension() != player.level().dimension()) return -1;
        double distance = Math.sqrt(player.distanceToSqr(Vec3.atCenterOf(pos.pos())));
        int cost = (int)Math.max(0, (distance - 4) * Config.cursorCostPerMeter);
        return cost == 0 ? Integer.MAX_VALUE : EnchantmentUtils.getPlayerXP(player) / cost;
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        @Nullable GlobalPos pos = pStack.get(OpenBlocks.TARGET_POS_COMPONENT);
        @Nullable Direction face = pStack.get(OpenBlocks.TARGET_FACE_COMPONENT);
        @Nullable Component name = pStack.get(OpenBlocks.TARGET_NAME_COMPONENT);

        if(pos != null) {
            pTooltipComponents.add(Component.translatable("openblocks.misc.target_pos", TranslationUtils.formatBlockPos(pos.pos())));
            pTooltipComponents.add(Component.translatable("openblocks.misc.target_dim", TranslationUtils.getName(pos.dimension())));
            if(EffectiveSide.get().isClient()) {
                Level level = ClientProxy.getLevel();
                Player player = ClientProxy.getPlayer();

                if(level.dimension() == pos.dimension()) {
                    double distance = Math.sqrt(player.distanceToSqr(Vec3.atCenterOf(pos.pos())));
                    int cost = (int)Math.max(0, (distance - 4) * Config.cursorCostPerMeter);
                    if(cost <= EnchantmentUtils.getPlayerXP(player)) {
                        float costLevels = EnchantmentUtils.getLevelAsFloat(player) - EnchantmentUtils.getLevelIfApplied(player, -cost).getLevelAsFloat();
                        pTooltipComponents.add(Component.translatable("openblocks.misc.xp_cost", cost, String.format("%.2f", costLevels)));
                    } else {
                        // calculate what level you need to be to barely have enough xp
                        int xpLevel = EnchantmentUtils.getLevelForExperience(cost);
                        int remainder = cost - EnchantmentUtils.getExperienceForLevel(xpLevel);
                        float costLevels = xpLevel + (float) remainder / EnchantmentUtils.getXpToNextLevel(xpLevel);
                        pTooltipComponents.add(Component.translatable("openblocks.misc.xp_cost", cost, String.format("%.2f", costLevels)));
                    }
                }
            }
        }
        if(face != null)
            pTooltipComponents.add(Component.translatable("openblocks.misc.target_face", TranslationUtils.getName(face)));
        if(name != null)
            pTooltipComponents.add(Component.translatable("openblocks.misc.target_name", name));
    }

    class RemoteMenuWapper extends AbstractContainerMenu {
        private final AbstractContainerMenu wrapped;
        private final InteractionHand validateSlot;
        private final Vec3 position;
        private final FakePlayer fakePlayer;

        private static final GameProfile FAKE_PLAYER_NAME = new GameProfile(UUID.fromString("56314a39-6af6-4862-9db0-233fbd744eb6"), "OpenBlocks Cursor");

        protected RemoteMenuWapper(AbstractContainerMenu wrapped, InteractionHand validateSlot, Level level, Vec3 position) {
            super(wrapped.getType(), wrapped.containerId);
            this.wrapped = wrapped;
            this.validateSlot = validateSlot;
            this.position = position;
            this.fakePlayer = FakePlayerFactory.get((ServerLevel) level, FAKE_PLAYER_NAME);
            wrapped.slots.forEach(this::addSlot);
            getDataSlots(wrapped).forEach(this::addDataSlot);
        }

        @SuppressWarnings("unchecked")
        private static List<DataSlot> getDataSlots(AbstractContainerMenu wrapped) {
            // TODO clean up reflection
            try {
                Field dataSlotsField = AbstractContainerMenu.class.getDeclaredField("dataSlots");
                dataSlotsField.setAccessible(true);
                return ((List<DataSlot>) dataSlotsField.get(wrapped));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void setSynchronizer(ContainerSynchronizer pSynchronizer) {
            wrapped.setSynchronizer(pSynchronizer);
        }

        @Override
        public void broadcastChanges() {
            wrapped.broadcastChanges();
        }

        @Override
        public void broadcastFullState() {
            wrapped.broadcastFullState();
        }

        @Override
        public boolean clickMenuButton(Player pPlayer, int pId) {
            return wrapped.clickMenuButton(pPlayer, pId);
        }

        @Override
        public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
            return wrapped.quickMoveStack(pPlayer, pIndex);
        }

        @Override
        public void transferState(AbstractContainerMenu pMenu) {
            super.transferState(pMenu);
        }

        @Override
        public void setCarried(ItemStack pStack) {
            wrapped.setCarried(pStack);
        }

        @Override
        public ItemStack getCarried() {
            return wrapped.getCarried();
        }

        @Override
        public boolean stillValid(Player pPlayer) {
            if(!pPlayer.getItemInHand(validateSlot).is(CursorItem.this))
                return false;
            // this is hilariously stupid but it works
            fakePlayer.setPosRaw(position.x, position.y, position.z);
            return wrapped.stillValid(fakePlayer);
        }
    }
}