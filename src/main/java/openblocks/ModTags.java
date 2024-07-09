package openblocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

public class ModTags {
    public static final TagKey<Item> WRENCHES = ci("tools/wrench");
    public static final TagKey<Block> CURSOR_ENABLE_MENU = mb("cursor_allow_menu");
    public static final TagKey<Block> CURSOR_DISABLE_MENU = mb("cursor_deny_menu");
    public static final TagKey<Fluid> SPONGE_EFFECTIVE = mf("sponge_effective");
    public static final TagKey<Fluid> SPONGE_BURNS = mf("sponge_burns");
    public static final TagKey<Fluid> LAVA_SPONGE_EFFECTIVE = mf("lava_sponge_effective");

    private static @NotNull TagKey<Block> mb(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("openblocks", name));
    }
    private static @NotNull TagKey<Item> mi(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("openblocks", name));
    }
    private static @NotNull TagKey<Fluid> mf(String name) {
        return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("openblocks", name));
    }
    private static @NotNull TagKey<Item> ci(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name));
    }

}
