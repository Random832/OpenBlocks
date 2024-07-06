package openblocks.client.renderer.blockentity.tank;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import openblocks.lib.geometry.Diagonal;

public class TankRenderUtils {

	private static final float UPPER_CLAMP_LIMIT = 0.95f;

	private static final float UPPER_CLAMP_VALUE = 1.0f;

	private static final float LOWER_CLAMP_LIMIT = 0.05f;

	public static final float LOWER_CLAMP_VALUE = 0.05f;

	private static final float PHASE_PER_DISTANCE = 0.5f;

	private static final float WAVE_AMPLITUDE = 0.025f;

	private static final float WAVE_FREQUENCY = 0.1f;

	public static float clampLevel(float level) {
		if (level <= LOWER_CLAMP_LIMIT) { return LOWER_CLAMP_VALUE; }
		if (level >= UPPER_CLAMP_LIMIT) { return UPPER_CLAMP_VALUE; }
		return level;
	}

	public static float calculatePhase(int x, int y, int z, Diagonal diagonal) {
		float posX = x + diagonal.offsetX / 2.0f;
		float posY = y + diagonal.offsetY / 2.0f;
		float posZ = z + diagonal.offsetZ / 2.0f;
		return (posX + posY + posZ) * PHASE_PER_DISTANCE;
	}

	public static float calculatePhase(int x, int y, int z) {
		return (x + y + z) * PHASE_PER_DISTANCE;
	}

	private static float calculateWaveAmplitude(float time, float phase) {
		return Mth.sin(time * WAVE_FREQUENCY + phase) * WAVE_AMPLITUDE;
	}

	public static float calculateRenderHeight(float time, float phase, float level) {
		// prevent seams when tank in in the middle of column
		if (level >= 1.0f) { return 1.0f; }
		level = clampLevel(level) + calculateWaveAmplitude(time, phase);

		if (level <= 0.0f) { return 0.0f; }
		if (level >= 1.0f) { return 1.0f; }
		return level;
	}

	@Nullable
	public static BlockEntity getTileEntitySafe(Level world, BlockPos pos) {
		if (world.isLoaded(pos)) {
			LevelChunk chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
			return chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
		}

		return null;
	}

}
