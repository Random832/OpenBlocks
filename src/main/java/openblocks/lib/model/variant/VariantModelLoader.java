package openblocks.lib.model.variant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import openblocks.lib.utils.JsonCompatUtils;

public class VariantModelLoader implements IGeometryLoader<VariantModelGeometry> {
    private static final String KEY_VARIANTS = "variants";
    //private static final String KEY_EXPANSIONS = "expansions";
    private static final String KEY_BASE = "base";


    @Override
    public VariantModelGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        final UnbakedModel base = deserializationContext.deserialize(JsonCompatUtils.getJsonObject(modelContents, KEY_BASE), BlockModel.class);

        //final Evaluator evaluator = new Evaluator();
        //if (modelContents.has(KEY_EXPANSIONS)) {
        //    JsonArray expansions = JsonCompatUtils.getJsonArray(modelContents, KEY_EXPANSIONS);
        //    for (JsonElement statement : expansions) {
        //        evaluator.addStatement(statement.getAsString());
        //    }
        //}

        final List<Pair<Predicate<VariantModelState>, UnbakedModel>> parts = Lists.newArrayList();

        if (modelContents.has(KEY_VARIANTS)) {
            JsonObject partData = JsonCompatUtils.getJsonObject(modelContents, KEY_VARIANTS);
            for (Map.Entry<String, JsonElement> p : partData.entrySet()) {
                final Predicate<VariantModelState> predicate = parsePredicate(p.getKey());
                final UnbakedModel partModel = deserializationContext.deserialize(p.getValue(), BlockModel.class);
                parts.add(Pair.of(predicate, partModel));
            }
        }

        return new VariantModelGeometry(base, ImmutableList.copyOf(parts));
    }

    private static Predicate<VariantModelState> parsePredicate(String key) {
        int separator = key.indexOf('.');
        if (separator != -1) {
            final String k = key.substring(0, separator);
            final String v = key.substring(separator + 1);
            return state -> state.testKeyValue(k, v);
        } else {
            return state -> state.testKey(key);
        }
    }
}