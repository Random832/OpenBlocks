package openblocks.client.renderer.blockentity.tank;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.FluidStack;
import openblocks.common.blockentity.TankBlockEntity;
import openblocks.lib.model.variant.VariantModelState;

import java.util.Set;

public class NeighbourMap {
	private final VariantModelState state;

	private static boolean testNeighbour(Set<String> result, Level world, FluidStack ownFluid, int x, int y, int z, String id) {
		final BlockEntity be = TankRenderUtils.getTileEntitySafe(world, new BlockPos(x, y, z));
		if (be instanceof TankBlockEntity tbe && tbe.accepts(ownFluid)) {
			//result.add(id);
			return true;
		} else {
			return false;
		}
	}

	static boolean edgeT(boolean a, boolean b, boolean diag) {
		return (a == b) & !diag;
	}

	static boolean edgeB(boolean a, boolean b, boolean diag) {
		return (!b & !a) | (b & !diag & a);
	}

	public NeighbourMap(Level world, BlockPos pos, FluidStack fluid) {
		if (world == null) {
			this.state = VariantModelState.EMPTY;
		} else {
			final int x = pos.getX();
			final int y = pos.getY();
			final int z = pos.getZ();

			final Set<String> neighbours = Sets.newHashSet();
			boolean n_t =  testNeighbour(neighbours, world, fluid, x + 0, y + 1, z + 0, "n_t");
			boolean n_b =  testNeighbour(neighbours, world, fluid, x + 0, y - 1, z + 0, "n_b");
			boolean n_e =  testNeighbour(neighbours, world, fluid, x + 1, y + 0, z + 0, "n_e");
			boolean n_w =  testNeighbour(neighbours, world, fluid, x - 1, y + 0, z + 0, "n_w");
			boolean n_s =  testNeighbour(neighbours, world, fluid, x + 0, y + 0, z + 1, "n_s");
			boolean n_n =  testNeighbour(neighbours, world, fluid, x + 0, y + 0, z - 1, "n_n");
			boolean n_te = testNeighbour(neighbours, world, fluid, x + 1, y + 1, z + 0, "n_te");
			boolean n_tw = testNeighbour(neighbours, world, fluid, x - 1, y + 1, z + 0, "n_tw");
			boolean n_ts = testNeighbour(neighbours, world, fluid, x + 0, y + 1, z + 1, "n_ts");
			boolean n_tn = testNeighbour(neighbours, world, fluid, x + 0, y + 1, z - 1, "n_tn");
			boolean n_be = testNeighbour(neighbours, world, fluid, x + 1, y - 1, z + 0, "n_be");
			boolean n_bw = testNeighbour(neighbours, world, fluid, x - 1, y - 1, z + 0, "n_bw");
			boolean n_bs = testNeighbour(neighbours, world, fluid, x + 0, y - 1, z + 1, "n_bs");
			boolean n_bn = testNeighbour(neighbours, world, fluid, x + 0, y - 1, z - 1, "n_bn");
			boolean n_nw = testNeighbour(neighbours, world, fluid, x - 1, y + 0, z - 1, "n_nw");
			boolean n_sw = testNeighbour(neighbours, world, fluid, x - 1, y + 0, z + 1, "n_sw");
			boolean n_se = testNeighbour(neighbours, world, fluid, x + 1, y + 0, z + 1, "n_se");
			boolean n_ne = testNeighbour(neighbours, world, fluid, x + 1, y + 0, z - 1, "n_ne");

			/*
			 * Can I interest you in Karnaugh maps? -b
			 *
			 * n_x is present when block has connected neigbour on side x, n_xy is present when
			 * neigbour is on diagonal between x and y Whole logic stuff and how I got those equations
			 * would be really hard to document here. Sorry.
			 *
			 * Symmetry of equations is broken by special casing for blocks on diagonal (to prevent edge
			 * from being drawn twice) */

			/* cool expression parser but it seems a bit extra. you know what else
			 * has an expression parser built in? the java compiler. -r */

			setEdgeState(neighbours, "bs", edgeT(n_b, n_s, n_bs));
			setEdgeState(neighbours, "bn", edgeT(n_b, n_n, n_bn));
			setEdgeState(neighbours, "ts", edgeB(n_t, n_s, n_ts));
			setEdgeState(neighbours, "tn", edgeB(n_t, n_n, n_tn));
			setEdgeState(neighbours, "ne", edgeT(n_n, n_e, n_ne));
			setEdgeState(neighbours, "nw", edgeT(n_n, n_w, n_nw));
			setEdgeState(neighbours, "se", edgeB(n_s, n_e, n_se));
			setEdgeState(neighbours, "sw", edgeB(n_s, n_w, n_sw));
			setEdgeState(neighbours, "be", edgeT(n_b, n_e, n_be));
			setEdgeState(neighbours, "bw", edgeT(n_b, n_w, n_bw));
			setEdgeState(neighbours, "te", edgeB(n_t, n_e, n_te));
			setEdgeState(neighbours, "tw", edgeB(n_t, n_w, n_tw));

			this.state = VariantModelState.create().withKeys(neighbours);
		}
	}

	public static ModelData getDataForBEWLR() {
		return ModelData.builder()
				.with(VariantModelState.PROPERTY, () -> VariantModelState.create().withKeys(
						Set.of("bs", "bn", "ts", "tn",
								"ne", "nw", "se", "sw",
								"be", "bw", "te", "tw")))
				.build();
	}

	private void setEdgeState(Set<String> neighbours, String key, boolean condition) {
		if(condition) neighbours.add(key);
	}


	public VariantModelState getState() {
		return this.state;
	}
}