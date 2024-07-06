package openblocks.lib.utils;

import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class FluidXpUtils {
    public interface IFluidXpConverter {
        int fluidToXp(int fluid);
        int xpToFluid(int xp);
    }

    private static class Linear implements IFluidXpConverter {

        private final int xpToFluid;

        public Linear(int xpToFluid) {
            this.xpToFluid = xpToFluid;
        }

        @Override
        public int fluidToXp(int fluid) {
            return fluid / xpToFluid;
        }

        @Override
        public int xpToFluid(int xp) {
            return xp * xpToFluid;
        }
    }

    private record ConversionEntry(HolderSet<Fluid> fluids, int amount, @Nullable IFluidXpConverter converter) {
        public boolean matches(FluidStack input) {
            return input.is(fluids);
        }
    }

    private static final List<ConversionEntry> converters = Lists.newArrayList();

    public static void initializeFromConfig(List<? extends String> strings) {
        converters.clear();
        for (String string : strings) {
            String[] split = string.split(":", 3);
            if(split[0].startsWith("#")) {
                ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(split[0].substring(1), split[1]);
                int amount = Integer.parseInt(split[2]);
                final HolderSet.Named<Fluid> tag = BuiltInRegistries.FLUID.getOrCreateTag(TagKey.create(Registries.FLUID, loc));
                converters.add(new ConversionEntry(tag, amount, new Linear(amount)));
            } else {
                ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(split[0], split[1]);
                int amount = Integer.parseInt(split[2]);
                Optional<Fluid> optional = BuiltInRegistries.FLUID.getOptional(loc);
                optional.ifPresent(fluid -> {
                    HolderSet.Direct<Fluid> set = HolderSet.direct(fluid.builtInRegistryHolder());
                    converters.add(new ConversionEntry(set, amount, new Linear(amount)));
                });
            }
        }
    }

    public static Optional<IFluidXpConverter> getConverter(FluidStack stack) {
        for (ConversionEntry converter : converters) {
            if(converter.matches(stack))
                return Optional.ofNullable(converter.converter);
        }
        return Optional.empty();
    }


    public static final Function<FluidStack, Float> FLUID_TO_LEVELS = input -> {
        if (input == null) return null;
        final Optional<IFluidXpConverter> maybeConverter = getConverter(input);
        return maybeConverter.map(converter -> {
            final int xp = converter.fluidToXp(input.getAmount());
            int level = EnchantmentUtils.getLevelForExperience(xp);
            float partial = (xp - EnchantmentUtils.getExperienceForLevel(level)) / (float) EnchantmentUtils.getXpToNextLevel(level);
            return level + partial;
        }).orElse(null);
    };

    public static int insertAnyXpFluid(IFluidHandler handler, int amountXp, IFluidHandler.FluidAction execute) {
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack fluid = handler.getFluidInTank(i);
            Optional<IFluidXpConverter> maybeConverter = getConverter(fluid);
            if (maybeConverter.isPresent()) {
                int result = tryFill(handler, fluid.getFluid().builtInRegistryHolder(), amountXp, maybeConverter.get(), execute);
                if (result >= 0) return result;
            }
        }
        for (ConversionEntry converter : converters) {
            if (converter.converter == null)
                continue;
            for (Holder<Fluid> fluid : converter.fluids) {
                int result = tryFill(handler, fluid, amountXp, converter.converter, execute);
                if (result >= 0) return result;
            }
        }
        return 0;
    }

    private static int tryFill(IFluidHandler handler, Holder<Fluid> fluid, int amountXp, IFluidXpConverter converter, IFluidHandler.FluidAction execute) {
        int amount = converter.xpToFluid(amountXp);
        FluidStack toInsert = new FluidStack(fluid, amount);
        int result = handler.fill(toInsert, IFluidHandler.FluidAction.SIMULATE);
        if (result != amount) {
            // round down to avoid voiding
            toInsert.setAmount(converter.xpToFluid(converter.fluidToXp(result)));
        }
        return converter.fluidToXp(handler.fill(toInsert, execute));
    }
}