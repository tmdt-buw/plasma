package de.buw.tmdt.plasma.utilities.misc;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PairTest {

    @Test
    public void map() {
        final Pair<String, String> input = new Pair<>("firstString", "secondString");
        final Pair<String, String> result = input.map(String::toLowerCase, String::toUpperCase);

        final Pair<String, String> expectation = new Pair<>("firststring", "SECONDSTRING");

        assertEquals(expectation, result);
    }

    @Test
    public void mapLeft() {
        final Pair<String, String> input = new Pair<>("firstString", "secondString");
        final Pair<Integer, String> result = input.mapLeft(String::length);

        final Pair<Integer, String> expectation = new Pair<>(11, "secondString");

        assertEquals(expectation, result);
    }

    @Test
    public void mapRight() {
        final Pair<String, String> input = new Pair<>("firstString", "secondString");
        final Pair<String, Integer> result = input.mapRight(String::length);

        final Pair<String, Integer> expectation = new Pair<>("firstString", 12);

        assertEquals(expectation, result);
    }
}