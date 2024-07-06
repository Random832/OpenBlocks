package openblocks.lib.model.textureditem;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import openblocks.lib.utils.JsonCompatUtils;

public class TexturedItemModelLoader implements IGeometryLoader<TexturedItemModelGeometry> {
    @Override
    public TexturedItemModelGeometry read(JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        ResourceLocation untexturedModel = ResourceLocation.parse(JsonCompatUtils.getString(modelContents, "untexturedModel"));
        ResourceLocation texturedModel = ResourceLocation.parse(JsonCompatUtils.getString(modelContents, "texturedModel"));
        ResourceLocation placeholder = ResourceLocation.parse(JsonCompatUtils.getString(modelContents, "placeholder"));
        return new TexturedItemModelGeometry(untexturedModel, texturedModel, ImmutableSet.of(placeholder));
    }
}