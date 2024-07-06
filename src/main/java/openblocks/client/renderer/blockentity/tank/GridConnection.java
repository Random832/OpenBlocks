package openblocks.client.renderer.blockentity.tank;

public abstract class GridConnection extends RenderConnection {

	public GridConnection(DoubledCoords coords) {
		super(coords);
	}

	public abstract boolean isConnected();
}