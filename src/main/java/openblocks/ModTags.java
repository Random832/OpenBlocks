package openblocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class ModTags {
    public static final TagKey<Item> WRENCHES = ci("tools/wrench");
    public static final TagKey<Block> CURSOR_ENABLE_MENU = mb("cursor_allow_menu");
    public static final TagKey<Block> CURSOR_DISABLE_MENU = mb("cursor_deny_menu");

    private static @NotNull TagKey<Block> mb(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("openblocks", name));
    }
    private static @NotNull TagKey<Item> mi(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("openblocks", name));
    }
    private static @NotNull TagKey<Item> ci(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name));
    }

}
