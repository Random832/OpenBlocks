package openblocks.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public class JadedLadderBlock extends TrapDoorBlock {
	public JadedLadderBlock(final Properties properties) {
		super(BlockSetType.OAK, properties);
	}

	// NOTE vanilla's ladder provides similar capability, but only when bottom block is actual ladder, so this is still useful
	@Override
	public boolean isLadder(BlockState state, LevelReader world, BlockPos pos, LivingEntity entity) {
		return state.getValue(OPEN);
	}
}