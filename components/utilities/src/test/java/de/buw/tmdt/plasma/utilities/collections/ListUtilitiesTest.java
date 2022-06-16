package de.buw.tmdt.plasma.utilities.collections;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ListUtilitiesTest {

    private final List<String> example = Arrays.asList("first", "second", "third", "fourth");

    @Test
    public void test1() {
        assertEquals(ListUtilities.getLastElement(example), "fourth");
    }

    @Test
    public void test2() {
        assertNull(ListUtilities.getLastElement(emptyList()));
    }

    @Test
    public void test3() {
        assertEquals(ListUtilities.truncateFirstElement(example), example.subList(1, 4));
    }

    @Test
    public void test4() {
        assertEquals(ListUtilities.truncateFirstElement(emptyList()), emptyList());
    }

    @Test
    public void test5() {
        assertEquals(ListUtilities.truncateLastElement(example), example.subList(0, 3));
    }

    @Test
    public void test6() {
        assertEquals(ListUtilities.truncateLastElement(emptyList()), emptyList());
    }

    @Test
    public void test7() {
        assertEquals(ListUtilities.nullSafeCopy(example), example);
    }

    @Test
    public void test8() {
        assertEquals(ListUtilities.nullSafeCopy(emptyList()), emptyList());
    }
}