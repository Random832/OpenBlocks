package openblocks.lib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import openblocks.client.ClientProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static openblocks.common.item.TankItem.TANK_TAG;

public class CompatibilityUtils {
    public static boolean isFluidHandler(Level level, BlockPos pos, Direction direction) {
        return getFluidHandler(level, pos, direction) != null;
    }

    @Nullable
    public static IFluidHandler getFluidHandler(Level level, BlockPos pos, Direction direction) {
        return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, direction);
    }

    public static HolderLookup.Provider getRegistryAccessStatic() {
        // TODO ugh only keep this until the component rework
        if (EffectiveSide.get().isClient())
            return ClientProxy.registryAccess();
        else
            return ServerLifecycleHooks.getCurrentServer().registryAccess();
    }

    public static @NotNull CustomData setItemData(ItemStack result, Consumer<CompoundTag> updater) {
        return result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).update(updater);
    }

    public static void updateTankFromStack(ItemStack stack, FluidTank tank) {
        CompoundTag itemTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();

        if (itemTag.contains(TANK_TAG)) {
            tank.readFromNBT(getRegistryAccessStatic(), itemTag.getCompound(TANK_TAG));
        } else {
            tank.setFluid(FluidStack.EMPTY);
        }
    }

    public static void saveTankToStack(FluidTank tank, ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        stack.set(DataComponents.CUSTOM_DATA, data.update(itemTag -> {
            itemTag.put(TANK_TAG, tank.writeToNBT(getRegistryAccessStatic(), new CompoundTag()));
        }));
    }

    @Nullable
    public static DyeColor colorFromStack(ItemStack heldStack) {
        if(heldStack.getItem() instanceof DyeItem di) {
            return di.getDyeColor();
        } else {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    public static Holder<Block> asHolder(Block block) {
        return block.builtInRegistryHolder();
    }
}
