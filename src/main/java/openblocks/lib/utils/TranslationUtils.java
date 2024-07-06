package openblocks.lib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Locale;

public class TranslationUtils {
    public static String formatBlockPos(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    public static Component getName(ResourceKey<Level> dimension) {
        if(dimension.location().getNamespace().equals("minecraft")) {
            return Component.translatable("openblocks.misc.dimension." + dimension.location().getPath());
        }
        return Component.translatable(dimension.location().toLanguageKey("dimension"));
    }

    public static Component getName(Direction face) {
        return Component.translatable("openblocks.misc.side." + face.name().toLowerCase(Locale.ROOT));
    }
}
