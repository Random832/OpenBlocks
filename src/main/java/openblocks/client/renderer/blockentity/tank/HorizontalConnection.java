package openblocks.client.renderer.blockentity.tank;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;

public class HorizontalConnection extends GridConnection {

	private FluidStack fluidA;

	private FluidStack fluidB;

	private boolean isConnected;

	public HorizontalConnection(DoubledCoords coords) {
		super(coords);
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	public void updateFluid(Direction direction, FluidStack stack) {
		if (direction == Direction.NORTH || direction == Direction.WEST) this.fluidA = stack.copy();
		else this.fluidB = stack.copy();

		this.isConnected = fluidA != null && fluidB != null && FluidStack.isSameFluidSameComponents(fluidA, fluidB);
	}

	public void clearFluid(Direction direction) {
		if (direction == Direction.NORTH || direction == Direction.WEST) this.fluidA = null;
		else this.fluidB = null;

		this.isConnected = false;
	}
}