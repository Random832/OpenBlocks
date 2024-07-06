package openblocks.client.renderer.blockentity.tank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import openblocks.lib.geometry.Diagonal;

public class DoubledCoords {
	private final int x;
	private final int y;
	private final int z;

	private DoubledCoords(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public DoubledCoords(int baseX, int baseY, int baseZ, Direction dir) {
		this(2 * baseX + dir.getStepX(), 2 * baseY + dir.getStepY(), 2 * baseZ + dir.getStepZ());
	}

	public DoubledCoords(BlockPos pos, Direction dir) {
		this(pos.getX(), pos.getY(), pos.getZ(), dir);
	}

	public DoubledCoords(int baseX, int baseY, int baseZ, Diagonal dir) {
		this(2 * baseX + dir.offsetX, 2 * baseY + dir.offsetY, 2 * baseZ + dir.offsetZ);
	}

	public DoubledCoords(BlockPos pos, Diagonal dir) {
		this(pos.getX(), pos.getY(), pos.getZ(), dir);
	}

	public boolean isSameAs(int baseX, int baseY, int baseZ, Direction dir) {
		return (x == 2 * baseX + dir.getStepX()) &&
				(y == 2 * baseY + dir.getStepY()) &&
				(z == 2 * baseZ + dir.getStepZ());
	}

	public boolean isSameAs(BlockPos pos, Direction dir) {
		return isSameAs(pos.getX(), pos.getY(), pos.getZ(), dir);
	}

	public boolean isSameAs(int baseX, int baseY, int baseZ, Diagonal dir) {
		return (x == 2 * baseX + dir.offsetX) &&
				(y == 2 * baseY + dir.offsetY) &&
				(z == 2 * baseZ + dir.offsetZ);
	}

	public boolean isSameAs(BlockPos pos, Diagonal dir) {
		return isSameAs(pos.getX(), pos.getY(), pos.getZ(), dir);
	}
}