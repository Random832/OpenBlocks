package openblocks.client.renderer.blockentity.tank;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import openblocks.common.blockentity.TankBlockEntity;

public class VerticalConnection extends GridConnection {

	private FluidStack fluidTop = FluidStack.EMPTY;

	private FluidStack fluidBottom = FluidStack.EMPTY;

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
		this.fluidTop = FluidStack.EMPTY;
		this.isConnected = false;
	}

	public void updateBottomFluid(FluidStack stack, boolean isFull) {
		this.fluidBottom = stack.copy();
		this.bottomIsFull = isFull;
		updateConnection();
	}

	public void clearBottomFluid() {
		this.fluidBottom = FluidStack.EMPTY;
		this.bottomIsFull = false;
		this.isConnected = false;
	}

	private void updateConnection() {
		boolean sameLiquid = FluidStack.isSameFluidSameComponents(fluidTop, fluidBottom);
		this.isConnected = sameLiquid && bottomIsFull;
	}

	public VerticalConnection(DoubledCoords coords) {
		super(coords);
	}

	public void updateFluid(Direction direction, FluidStack fluid) {
		switch(direction) {
			case UP -> updateTopFluid(fluid);
			case DOWN -> updateBottomFluid(fluid, fluid.getAmount() >= TankBlockEntity.getTankCapacity());
		}
	}
}