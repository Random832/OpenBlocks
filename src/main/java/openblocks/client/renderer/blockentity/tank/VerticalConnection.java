package openblocks.client.renderer.blockentity.tank;

import net.neoforged.neoforge.fluids.FluidStack;

public class VerticalConnection extends GridConnection {

	private FluidStack fluidTop;

	private FluidStack fluidBottom;

	private boolean bottomIsFull;

	private boolean isConnected;

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	public void updateTopFluid(FluidStack stack) {
		this.fluidTop = stack.copy();
		updateConnection();
	}

	public void clearTopFluid() {
		this.fluidTop = null;
		this.isConnected = false;
	}

	public void updateBottomFluid(FluidStack stack, boolean isFull) {
		this.fluidBottom = stack.copy();
		this.bottomIsFull = isFull;
		updateConnection();
	}

	public void clearBottomFluid() {
		this.fluidBottom = null;
		this.bottomIsFull = false;
		this.isConnected = false;
	}

	private void updateConnection() {
		boolean sameLiquid = fluidTop != null && fluidBottom != null && FluidStack.isSameFluidSameComponents(fluidTop, fluidBottom);
		this.isConnected = sameLiquid && bottomIsFull;
	}

	public VerticalConnection(DoubledCoords coords) {
		super(coords);
	}
}