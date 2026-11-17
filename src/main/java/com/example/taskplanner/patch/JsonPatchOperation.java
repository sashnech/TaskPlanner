package com.example.taskplanner.patch;

import tools.jackson.databind.JsonNode;


public record JsonPatchOperation(
        String op,
        String path,
        String from,
        JsonNode value
) {
}
