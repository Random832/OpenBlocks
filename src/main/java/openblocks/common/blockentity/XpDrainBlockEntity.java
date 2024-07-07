package openblocks.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import openblocks.lib.blockentity.OpenTileEntity;
import openblocks.lib.utils.EnchantmentUtils;
import openblocks.lib.utils.FluidXpUtils;
import openblocks.OpenBlocks;
import openblocks.lib.utils.BlockUtils;

import java.util.List;

public class XpDrainBlockEntity extends OpenTileEntity {
    public XpDrainBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(OpenBlocks.DRAIN_BE.get(), pPos, pBlockState);
    }

    public void tick() {
        if (!level.isClientSide) {
            final List<ExperienceOrb> xpOrbsOnGrid = getXPOrbsOnGrid();
            final List<Player> playersOnGrid = getPlayersOnGrid();

            if (!xpOrbsOnGrid.isEmpty() || !playersOnGrid.isEmpty()) {
                final BlockPos down = getBlockPos().below();

                if (level.isLoaded(down)) {
                    IFluidHandler maybeHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, down, Direction.UP);

                    if (maybeHandler != null) {
                        for (ExperienceOrb orb : xpOrbsOnGrid)
                            tryConsumeOrb(maybeHandler, orb);

                        for (Player player : playersOnGrid)
                            tryDrainPlayer(maybeHandler, player);
                    }
                }
            }
        }
    }

    protected void tryDrainPlayer(IFluidHandler tank, Player player) {
        int playerXP = EnchantmentUtils.getPlayerXP(player);
        if (playerXP <= 0) return;

        int maxDrainedXp = Math.min(4, playerXP);

        int acceptedXP = FluidXpUtils.insertAnyXpFluid(tank, maxDrainedXp, IFluidHandler.FluidAction.EXECUTE);

        if (acceptedXP <= 0) return;

        if (level.getGameTime() % 4 == 0) {
            playSoundAtBlock(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.1F, 0.5F * ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.8F));
        }

        EnchantmentUtils.addPlayerXP(player, -acceptedXP);
    }

    protected void tryConsumeOrb(IFluidHandler tank, ExperienceOrb orb) {
        if (!orb.isRemoved()) {
            int xpAmount = orb.getValue();
            int filled = FluidXpUtils.insertAnyXpFluid(tank, xpAmount, IFluidHandler.FluidAction.SIMULATE);
            if (filled == xpAmount) {
                FluidXpUtils.insertAnyXpFluid(tank, xpAmount, IFluidHandler.FluidAction.EXECUTE);
                orb.discard();
            }
        }
    }

    protected List<Player> getPlayersOnGrid() {
        return level.getEntitiesOfClass(Player.class, BlockUtils.singleBlock(worldPosition));
    }

    protected List<ExperienceOrb> getXPOrbsOnGrid() {
        return level.getEntitiesOfClass(ExperienceOrb.class, BlockUtils.aabbOffset(worldPosition, 0, 0, 0, 1, 0.3, 1));
    }

    {
    }
}