package openblocks.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;
import openblocks.OpenBlocks;
import openblocks.client.ClientProxy;

public class SpongeAirBlock extends AirBlock {
    public SpongeAirBlock(Properties p_48756_) {
        super(p_48756_);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        Player player = ClientProxy.getPlayer();
        if(player.getItemBySlot(EquipmentSlot.HEAD).is(OpenBlocks.BASTARD_GLASSES)) {
            pLevel.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, pState), pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5, 0.0, 0.0, 0.0);
        }
    }
}
