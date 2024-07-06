package openblocks.client.renderer.blockentity.tank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import openblocks.lib.geometry.Diagonal;

public class RenderConnection {
	private final DoubledCoords coords;

	public RenderConnection(DoubledCoords coords) {
		this.coords = coords;
	}

	public boolean isPositionEqualTo(int x, int y, int z, Direction dir) {
		return coords.isSameAs(x, y, z, dir);
	}

	public boolean isPositionEqualTo(BlockPos pos, Direction dir) {
		return coords.isSameAs(pos, dir);
	}

	public boolean isPositionEqualTo(int x, int y, int z, Diagonal dir) {
		return coords.isSameAs(x, y, z, dir);
	}

	public boolean isPositionEqualTo(BlockPos pos, Diagonal dir) {
		return coords.isSameAs(pos, dir);
	}
}