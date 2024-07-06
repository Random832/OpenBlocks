package openblocks.client.renderer.blockentity.tank;

import net.minecraft.core.Direction;
import openblocks.lib.geometry.Diagonal;

public interface ITankConnections {

	VerticalConnection getTopConnection();

	VerticalConnection getBottomConnection();

	HorizontalConnection getHorizontalConnection(Direction dir);

	DiagonalConnection getDiagonalConnection(Diagonal dir);
}