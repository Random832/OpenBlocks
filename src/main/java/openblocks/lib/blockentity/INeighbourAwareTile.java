package openblocks.lib.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public interface INeighbourAwareTile {
    void onNeighbourChanged(BlockPos neighbourPos, Block neigbourBlock);
}