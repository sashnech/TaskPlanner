package com.example.taskplanner.patch;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.util.Map;


public final class JsonMergePatch {

    private JsonMergePatch() {
    }

    public static JsonNode apply(JsonNode patch, JsonNode target) {
        if (patch == null) {
            throw PatchException.badRequest("Документ JSON Merge Patch порожній");
        }

        return merge(target, patch);
    }

    private static JsonNode merge(JsonNode target, JsonNode patch) {
        if (!patch.isObject()) {
            return patch.deepCopy();
        }

        ObjectNode result = target != null && target.isObject()
                ? (ObjectNode) target.deepCopy()
                : JsonNodeFactory.instance.objectNode();

        for (Map.Entry<String, JsonNode> entry : patch.properties()) {
            if (entry.getValue().isNull()) {
                result.remove(entry.getKey());
            } else {
                result.set(entry.getKey(), merge(result.get(entry.getKey()), entry.getValue()));
            }
        }

        return result;
    }
}
