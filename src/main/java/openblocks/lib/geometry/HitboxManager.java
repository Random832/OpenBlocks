package openblocks.lib.geometry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import openblocks.lib.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HitboxManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public HitboxManager() {
        super(GSON, "hitboxes");
    }

    Map<ResourceLocation, HitboxSupplier> map = new HashMap<>();

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        map.clear();
        DynamicOps<JsonElement> ops = JsonOps.INSTANCE;
        for (Map.Entry<ResourceLocation, JsonElement> entry : resourceList.entrySet()) {
            ResourceLocation location = entry.getKey();
            JsonElement json = entry.getValue();
            final DataResult<HitboxSupplier> result1 = HitboxSupplier.CODEC.parse(ops, json);
            final Optional<HitboxSupplier> result2 = result1.resultOrPartial(errorMsg -> Log.warn("Could not decode Hitboxes with json id {} - error: {}", location, errorMsg));
            result2.ifPresent(s -> map.put(location, s));
        }
    }

    public HitboxSupplier get(ResourceLocation location) {
        return map.get(location);
    }
}