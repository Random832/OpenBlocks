package openblocks.client.renderer.blockentity.tank;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import openblocks.lib.geometry.Diagonal;

public interface ITankRenderFluidData {
	void updateFluid(FluidStack fluid);

	FluidStack getFluid();

	boolean hasFluid();

	boolean shouldRenderFluidWall(Direction side);

	float getCornerFluidLevel(Diagonal diagonal, float time);

	float getCenterFluidLevel(float time);
}