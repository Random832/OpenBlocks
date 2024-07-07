package openblocks.common.item;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import net.minecraft.ChatFormatting;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import openblocks.Config;
import openblocks.OpenBlocks;
import openblocks.common.blockentity.TankBlockEntity;
import openblocks.lib.item.ICreativeVariantsItem;
import openblocks.lib.model.IItemTexture;
import openblocks.client.renderer.blockentity.tank.TankBEWLR;

public class TankItem extends BlockItem implements ICreativeVariantsItem {
	@OnlyIn(Dist.CLIENT)
	public static class ColorHandler implements ItemColor {
		@Override
		public int getColor(@Nonnull ItemStack stack, int tintIndex) {
			if (tintIndex == 0) {
				final FluidTank tank = readTank(stack);
				final FluidStack fluid = tank.getFluid();
				if (!fluid.isEmpty()) {
					IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
				}
			}

			return 0xFFFFFFFF;
		}
	}

	public static final String TANK_TAG = "tank";

	public TankItem(Block block, final Item.Properties properties) {
		super(block, properties);
	}

	public static class FluidHandler implements IFluidHandlerItem {
		private final ItemStack container;

		public FluidHandler(ItemStack container) {
			this.container = container;
		}

		private FluidStack adjustSize(FluidStack stack) {
			if (!stack.isEmpty()) {
				stack = stack.copy();
				stack.setAmount(stack.getAmount() * container.getCount());
			}
			return stack;
		}

		@Override
		public int getTanks() {
			return 1;
		}

		@Override
		public FluidStack getFluidInTank(int id) {
			return adjustSize(readTank(container).getFluidInTank(id));
		}

		@Override
		public int getTankCapacity(int tank) {
			return readTank(container).getTankCapacity(tank) * container.getCount();
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
			return readTank(container).isFluidValid(tank, stack);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			FluidTank tank = readTank(container);
			final int count = container.getCount();
			if (count == 0) {
				return 0;
			}

			final int amountPerTank = resource.getAmount() / count;
			if (amountPerTank == 0) {
				return 0;
			}

			FluidStack resourcePerTank = resource.copy();
			resourcePerTank.setAmount(amountPerTank);

			int filledPerTank = tank.fill(resourcePerTank, action);
			if (action.execute()) {
				saveTank(container, tank);
			}
			return filledPerTank * count;
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			if (resource.isEmpty()) {
				return FluidStack.EMPTY;
			}

			FluidTank tank = readTank(container);

            if (!FluidStack.isSameFluidSameComponents(resource, tank.getFluid())) {
				return FluidStack.EMPTY;
			}

			return drain(resource.getAmount(), action);
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			if (maxDrain <= 0) {
				return FluidStack.EMPTY;
			}

			FluidTank tank = readTank(container);
			return drain(tank, maxDrain, action);
		}

		private FluidStack drain(FluidTank tank, int maxDrain, FluidAction action) {
			final int count = container.getCount();
			if (count == 0) {
				return FluidStack.EMPTY;
			}

			final int amountPerTank = maxDrain / count;
			if (amountPerTank == 0) {
				return FluidStack.EMPTY;
			}

			FluidStack drained = tank.drain(amountPerTank, action);
			if (action.execute()) {
				saveTank(container, tank);
			}

			return adjustSize(drained);
		}

		@Override
		public ItemStack getContainer() {
			return container;
		}

	}

	private static class ItemTexture implements IItemTexture {

		private final ItemStack container;

		public ItemTexture(ItemStack container) {
			this.container = container;
		}

		@Override
		public Optional<ResourceLocation> getTexture() {
			return getFluidTexture();
		}

		private Optional<ResourceLocation> getFluidTexture() {
			FluidTank tank = readTank(container);
			final FluidStack stack = tank.getFluid();
			if (stack.isEmpty()) {
				return Optional.empty();
			}

			final Fluid fluid = stack.getFluid();

			return Optional.of(IClientFluidTypeExtensions.of(fluid).getStillTexture(stack));
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> result, TooltipFlag flag) {
		FluidTank fakeTank = readTank(stack);
		FluidStack fluidStack = fakeTank.getFluid();
		final int amount = fluidStack.getAmount();
		if (amount > 0) {
			float percent = Math.max(100.0f / fakeTank.getCapacity() * amount, 1);
			result.add(Component.literal(String.format("%d mB (%.0f%%)", amount, percent)));

			if (flag.isAdvanced()) {
				final Fluid fluid = fluidStack.getFluid();
				result.add(fluidStack.getHoverName().copy().withStyle(ChatFormatting.DARK_GRAY));
			}
		}
	}

	@Override
	public Component getName(ItemStack stack) {
		final FluidTank fakeTank = readTank(stack);
		final FluidStack fluidStack = fakeTank.getFluid();

		if (!fluidStack.isEmpty()) {
			final Component fluidName = fluidStack.getHoverName();
			return Component.translatable("block.openblocks.tank.filled", fluidName);
		}

		return super.getName(stack);
	}

	public static boolean fillTankItem(ItemStack result, Fluid fluid) {
		if (result.isEmpty() || !(result.getItem() instanceof TankItem)) {
			return false;
		}
		final int tankCapacity = TankBlockEntity.getTankCapacity();
		FluidTank tank = new FluidTank(tankCapacity);
		tank.setFluid(new FluidStack(fluid, tankCapacity));
		saveTank(result, tank);
		return true;
	}

	public static FluidTank readTank(ItemStack stack) {
		FluidTank tank = new FluidTank(TankBlockEntity.getTankCapacity());
        tank.setFluid(stack.getOrDefault(OpenBlocks.FLUID_COMPONENT, FluidStack.EMPTY));
        return tank;
	}

	private static void saveTank(@Nonnull ItemStack container, FluidTank tank) {
		container.set(OpenBlocks.FLUID_COMPONENT, tank.getFluid());
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			TankBEWLR renderer = new TankBEWLR();

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		});
	}

	public void fillItemGroup(CreativeModeTab.Output output) {
		output.accept(new ItemStack(this));

		if (Config.displayAllFilledTanks) {
			final ItemStack emptyTank = new ItemStack(this);
			for (Fluid fluid : BuiltInRegistries.FLUID) {
				FluidState state = fluid.defaultFluidState();
				if (state.isSource()) {
					try {
						final ItemStack tankStack = emptyTank.copy();
						if (TankItem.fillTankItem(tankStack, fluid)) {
							output.accept(tankStack, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
						} else {
							//Log.debug("Failed to create filled tank stack for fluid '%s'. Not registered?", fluid.getRegistryName());
						}
					} catch (Throwable t) {
						throw new RuntimeException(String.format("Failed to create item for fluid '%s'. Until this is fixed, you can bypass this code with config option 'tanks.displayAllFluids'",
								BuiltInRegistries.FLUID.getKey(fluid)), t);
					}
				}
			}
		}
	}

}
