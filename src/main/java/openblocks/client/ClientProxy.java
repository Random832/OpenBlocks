package openblocks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import openblocks.client.renderer.blockentity.GuideRenderer;
import openblocks.lib.geometry.HitboxManager;
import openblocks.lib.geometry.HitboxSupplier;

public class ClientProxy {
    public static HolderLookup.Provider registryAccess() {
        assert Minecraft.getInstance().level != null;
        return Minecraft.getInstance().level.registryAccess();
    }

    public static final HitboxManager hitboxManager = new HitboxManager();

    public static HitboxSupplier getHitboxes(ResourceLocation location) {
        return hitboxManager.get(location);
    }

    public static final GuideRenderer.ModelHolder guideModelHolder = new GuideRenderer.ModelHolder();

    public static Level getLevel() {
        return Minecraft.getInstance().level;
    }

    public static Player getPlayer() {
        return Minecraft.getInstance().player;
    }
}
