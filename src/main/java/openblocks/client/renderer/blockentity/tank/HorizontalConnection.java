package openblocks.client.renderer.blockentity.tank;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;

public class HorizontalConnection extends GridConnection {

	private FluidStack fluidA = FluidStack.EMPTY;

	private FluidStack fluidB = FluidStack.EMPTY;

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

		this.isConnected = !fluidA.isEmpty() && !fluidB.isEmpty() && FluidStack.isSameFluidSameComponents(fluidA, fluidB);
	}

	public void clearFluid(Direction direction) {
		if (direction == Direction.NORTH || direction == Direction.WEST) this.fluidA = FluidStack.EMPTY;
		else this.fluidB = FluidStack.EMPTY;

		this.isConnected = false;
	}
}