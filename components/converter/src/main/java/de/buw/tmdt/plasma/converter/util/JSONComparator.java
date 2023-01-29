package de.buw.tmdt.plasma.converter.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

public class JSONComparator implements Comparator<JsonNode>, Serializable {

    private JSONComparator() {
    }

    /**
     * Recursive function that checks whether 2 JsonNode contain exact same information regardless of the order in ArrayNode/ObjectNode.
     *
     * @param element1 first JsonNode used in comparison.
     * @param element2 second JsonNode used in comparison.
     * @return boolean indicating whether JsonNode contain same information.
     */
    public static boolean areEqualElements(JsonNode element1, JsonNode element2) {
        if (element1.isValueNode() && element2.isValueNode()) {
            return areEqualPrimitives((ValueNode) element1, (ValueNode) element2);
        } else if (element1.isArray() && element2.isArray()) {
            return areEqualArraysIgnoreOrder((ArrayNode) element1, (ArrayNode) element2);
            //comparing JsonObjects
        } else if (element1.isObject() && element2.isObject()) {
            return areEqualObjects((ObjectNode) element1, (ObjectNode) element2);
        }
        return false;
    }

    private static boolean areEqualPrimitives(ValueNode element1, ValueNode element2) {
        return element1.asText().equals(element2.asText());
    }

    /**
     * Compares two Json Arrays while ignoring the order of elements.
     */
    private static boolean areEqualArraysIgnoreOrder(ArrayNode element1, ArrayNode element2) {
        if (element1.size() != element2.size()) {
            return false;
        }
        for (int i = 0; i < element1.size(); i++) {
            int countSame = 0;

            for (int j = 0; j < element2.size(); j++) {
                if (areEqualElements(element1.get(i), element2.get(j))) {
                    countSame++;
                }
            }

            for (int j = 0; j < element1.size(); j++) {
                if (areEqualElements(element1.get(i), element1.get(j))) {
                    countSame--;
                }
            }

            if (countSame != 0) {
                return false;
            }
        }
        return true;
    }

    private static boolean areEqualObjects(ObjectNode object1, ObjectNode object2) {
        if (object1.size() != object2.size()) {
            return false;
        }

        @SuppressWarnings("NullableProblems") Iterable<Map.Entry<String, JsonNode>> object1Iterable = object1::fields;
        for (Map.Entry<String, JsonNode> object1Entry : object1Iterable) {
            JsonNode e1 = object1Entry.getValue();
            JsonNode e2 = object2.get(object1Entry.getKey());
            if (!areEqualElements(e1, e2)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int compare(JsonNode o1, JsonNode o2) {
        return 0;
    }
}