package openblocks.lib.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonCompatUtils {
    public static JsonObject getJsonObject(JsonObject object, String memberName) {
        return object.getAsJsonObject(memberName);
    }

    public static JsonArray getJsonArray(JsonObject object, String memberName) {
        return object.getAsJsonArray(memberName);
    }

    public static String getString(JsonObject object, String memberName) {
        return object.getAsJsonPrimitive(memberName).getAsString();
    }

    public static float getFloatFromArray(JsonArray jsonarray, int i) {
        return jsonarray.get(i).getAsFloat();
    }
}
