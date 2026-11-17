package com.example.taskplanner.patch;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;


public final class JsonPatch {

    private JsonPatch() {
    }

    public static JsonNode apply(List<JsonPatchOperation> operations, JsonNode target) {
        if (operations == null) {
            throw PatchException.badRequest("Документ JSON Patch має бути масивом операцій");
        }

        JsonNode document = target.deepCopy();

        for (int i = 0; i < operations.size(); i++) {
            JsonPatchOperation operation = operations.get(i);

            if (operation == null) {
                throw PatchException.badRequest("Операція #" + i + " порожня");
            }

            document = applyOne(operation, document, i);
        }

        return document;
    }

    private static JsonNode applyOne(JsonPatchOperation operation, JsonNode document, int index) {
        String op = operation.op();
        String prefix = "Операція #" + index + " (" + op + "): ";

        if (op == null) {
            throw PatchException.badRequest("Операція #" + index + ": відсутнє поле \"op\"");
        }

        List<String> path = parsePointer(operation.path(), prefix + "поле \"path\"");

        return switch (op) {
            case "add" -> add(document, path, requireValue(operation, prefix), prefix);
            case "remove" -> {
                remove(document, path, prefix);
                yield document;
            }
            case "replace" -> replace(document, path, requireValue(operation, prefix), prefix);
            case "move" -> {
                List<String> from = parsePointer(operation.from(), prefix + "поле \"from\"");

                if (isProperPrefix(from, path)) {
                    throw PatchException.badRequest(prefix + "\"from\" не може бути префіксом \"path\"");
                }

                JsonNode value = get(document, from, prefix);
                remove(document, from, prefix);
                yield add(document, path, value, prefix);
            }
            case "copy" -> {
                List<String> from = parsePointer(operation.from(), prefix + "поле \"from\"");
                JsonNode value = get(document, from, prefix).deepCopy();
                yield add(document, path, value, prefix);
            }
            case "test" -> {
                JsonNode expected = requireValue(operation, prefix);
                JsonNode actual = get(document, path, prefix);

                if (!actual.equals(expected)) {
                    throw PatchException.conflict(prefix + "значення за шляхом \""
                            + operation.path() + "\" не дорівнює очікуваному");
                }

                yield document;
            }
            default -> throw PatchException.badRequest(
                    "Операція #" + index + ": невідомий тип операції \"" + op + "\"");
        };
    }

    private static JsonNode requireValue(JsonPatchOperation operation, String prefix) {
        if (operation.value() == null) {
            throw PatchException.badRequest(prefix + "відсутнє поле \"value\"");
        }

        return operation.value();
    }


    private static JsonNode add(JsonNode document, List<String> path, JsonNode value, String prefix) {
        if (path.isEmpty()) {
            return value;
        }

        JsonNode parent = get(document, path.subList(0, path.size() - 1), prefix);
        String last = path.getLast();

        if (parent instanceof ObjectNode object) {
            object.set(last, value);
        } else if (parent instanceof ArrayNode array) {
            if ("-".equals(last)) {
                array.add(value);
            } else {
                int i = arrayIndex(last, array.size(), prefix);
                array.insert(i, value);
            }
        } else {
            throw PatchException.unprocessable(prefix + "батьківський елемент не є об'єктом чи масивом");
        }

        return document;
    }

    private static void remove(JsonNode document, List<String> path, String prefix) {
        if (path.isEmpty()) {
            throw PatchException.unprocessable(prefix + "неможливо видалити корінь документа");
        }

        JsonNode parent = get(document, path.subList(0, path.size() - 1), prefix);
        String last = path.getLast();

        if (parent instanceof ObjectNode object) {
            if (!object.has(last)) {
                throw PatchException.unprocessable(prefix + "поле \"" + last + "\" не існує");
            }
            object.remove(last);
        } else if (parent instanceof ArrayNode array) {
            array.remove(arrayIndex(last, array.size() - 1, prefix));
        } else {
            throw PatchException.unprocessable(prefix + "батьківський елемент не є об'єктом чи масивом");
        }
    }

    private static JsonNode replace(JsonNode document, List<String> path, JsonNode value, String prefix) {
        if (path.isEmpty()) {
            return value;
        }

        get(document, path, prefix);
        remove(document, path, prefix);
        return add(document, path, value, prefix);
    }

    private static JsonNode get(JsonNode document, List<String> path, String prefix) {
        JsonNode current = document;

        for (String token : path) {
            if (current instanceof ObjectNode object) {
                current = object.get(token);
            } else if (current instanceof ArrayNode array) {
                current = array.get(arrayIndex(token, array.size() - 1, prefix));
            } else {
                current = null;
            }

            if (current == null) {
                throw PatchException.unprocessable(prefix + "шлях \"/"
                        + String.join("/", path) + "\" не існує");
            }
        }

        return current;
    }


    private static List<String> parsePointer(String pointer, String what) {
        if (pointer == null) {
            throw PatchException.badRequest(what + " є обов'язковим");
        }

        if (pointer.isEmpty()) {
            return List.of();
        }

        if (!pointer.startsWith("/")) {
            throw PatchException.badRequest(what + " має починатися з \"/\"");
        }

        List<String> tokens = new ArrayList<>();

        for (String raw : pointer.substring(1).split("/", -1)) {
            tokens.add(raw.replace("~1", "/").replace("~0", "~"));
        }

        return tokens;
    }

    private static int arrayIndex(String token, int max, String prefix) {
        if (!token.matches("0|[1-9][0-9]*")) {
            throw PatchException.unprocessable(prefix + "некоректний індекс масиву \"" + token + "\"");
        }

        int index = Integer.parseInt(token);

        if (index > max) {
            throw PatchException.unprocessable(prefix + "індекс масиву " + index + " поза межами");
        }

        return index;
    }

    private static boolean isProperPrefix(List<String> prefix, List<String> path) {
        return prefix.size() < path.size() && path.subList(0, prefix.size()).equals(prefix);
    }
}
