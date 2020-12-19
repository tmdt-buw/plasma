package de.buw.tmdt.plasma.utilities.collections;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MapUtilitiesTest {

    @Test
    public void flip() {
        final Map<String, Integer> stringToNumber = new HashMap<>();
        stringToNumber.put("One", 1);
        stringToNumber.put("Two", 2);
        stringToNumber.put("Thousand", 1000);

        final Map<Integer, String> flip = MapUtilities.flip(stringToNumber);
        assertEquals(3, flip.size());
        assertEquals("One", flip.get(1));
        assertEquals("Two", flip.get(2));
        assertEquals("Thousand", flip.get(1000));
    }

    @Test(expected = IllegalArgumentException.class)
    public void flipAmbiguous() {
        final Map<String, Integer> stringToLengthMap = Stream.of("Some", "Piece", "Of", "Text").collect(Collectors.toMap(
                Function.identity(),
                String::length
        ));
        MapUtilities.flip(stringToLengthMap);
    }

    @Test
    public void compose() {
        Map<String, Integer> stringToNumber = new HashMap<>();
        stringToNumber.put("One", 1);
        stringToNumber.put("Four", 4);
        stringToNumber.put("Eighty One", 81);
        stringToNumber.put("Ten Thousand", 10000);

        Map<Integer, Byte> numberToOrderOfMagnitude = new HashMap<>();
        numberToOrderOfMagnitude.put(1, (byte) 1);
        numberToOrderOfMagnitude.put(4, (byte) 1);
        numberToOrderOfMagnitude.put(81, (byte) 2);
        numberToOrderOfMagnitude.put(10000, (byte) 5);

        Map<String, Byte> stringToOrderOfMagnitude = MapUtilities.compose(stringToNumber, numberToOrderOfMagnitude);

        assertEquals(4, stringToOrderOfMagnitude.size());
        assertEquals(1, (int) stringToOrderOfMagnitude.get("One"));
        assertEquals(1, (int) stringToOrderOfMagnitude.get("Four"));
        assertEquals(2, (int) stringToOrderOfMagnitude.get("Eighty One"));
        assertEquals(5, (int) stringToOrderOfMagnitude.get("Ten Thousand"));
    }

    @Test
    public void compose2() {
        Map<String, Integer> stringToNumber = new HashMap<>();
        stringToNumber.put("One", 1);
        stringToNumber.put("Four", 4);
        stringToNumber.put("Eighty One", 81);
        stringToNumber.put("Ten Thousand", 10000);

        Map<Integer, Byte> numberToOrderOfMagnitude = new HashMap<>();
        numberToOrderOfMagnitude.put(1, (byte) 1);
        numberToOrderOfMagnitude.put(4, (byte) 1);
        numberToOrderOfMagnitude.put(10000, (byte) 5);

        Map<String, Byte> stringToOrderOfMagnitude = MapUtilities.compose(stringToNumber, numberToOrderOfMagnitude);

        assertEquals(3, stringToOrderOfMagnitude.size());
        assertEquals((Byte) (byte) 1, stringToOrderOfMagnitude.get("One"));
        assertEquals((Byte) (byte) 1, stringToOrderOfMagnitude.get("Four"));
        assertEquals((Byte) (byte) 5, stringToOrderOfMagnitude.get("Ten Thousand"));
        assertNull(stringToOrderOfMagnitude.get("Eighty One"));
    }

    @Test
    public void compose3() {
        Map<String, Integer> stringToNumber = new HashMap<>();
        stringToNumber.put("One", 1);
        stringToNumber.put("Four", 4);
        stringToNumber.put("Ten Thousand", 10000);

        Map<Integer, Byte> numberToOrderOfMagnitude = new HashMap<>();
        numberToOrderOfMagnitude.put(1, (byte) 1);
        numberToOrderOfMagnitude.put(4, (byte) 1);
        numberToOrderOfMagnitude.put(81, (byte) 2);
        numberToOrderOfMagnitude.put(10000, (byte) 5);

        Map<String, Byte> stringToOrderOfMagnitude = MapUtilities.compose(stringToNumber, numberToOrderOfMagnitude);

        assertEquals(3, stringToOrderOfMagnitude.size());
        assertEquals((Byte) (byte) 1, stringToOrderOfMagnitude.get("One"));
        assertEquals((Byte) (byte) 1, stringToOrderOfMagnitude.get("Four"));
        assertEquals((Byte) (byte) 5, stringToOrderOfMagnitude.get("Ten Thousand"));
    }

    @Test
    public void remapSuccess() {
        Map<String, String> input = new HashMap<>(3);
        input.put("key1", "value1");
        input.put("key2", "value2");
        input.put("key3", "value3");

        Map<Integer, String> expectation = new HashMap<>(3);
        expectation.put(1, "value1");
        expectation.put(2, "value2");
        expectation.put(3, "value3");

        Function<Map.Entry<String, String>, Integer> keyGenerator = entry -> Integer.valueOf(entry.getKey().substring(3));

        final Map<Integer, String> result = MapUtilities.remap(input, keyGenerator);

        assertEquals(expectation, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void remapFailDuplicateKey() {
        Map<String, String> input = new HashMap<>(3);
        input.put("key1", "value1");
        input.put("key2", "value2");
        input.put("key3", "value3");

        Function<Map.Entry<String, String>, Integer> keyGenerator = entry -> entry.getKey().length();

        final Map<Integer, String> result = MapUtilities.remap(input, keyGenerator);
    }

    @Test
    public void mapValues() {
        Map<Integer, String> input = new HashMap<>(3);
        input.put(0, "test value");
        input.put(1, "longer test value");
        input.put(2, "longest test value ever");

        Function<String, Integer> projection = String::length;

        final Map<Integer, Integer> result = MapUtilities.mapValues(input, projection);

        assertEquals(10, result.get(0).intValue());
        assertEquals(17, result.get(1).intValue());
        assertEquals(23, result.get(2).intValue());
    }
}