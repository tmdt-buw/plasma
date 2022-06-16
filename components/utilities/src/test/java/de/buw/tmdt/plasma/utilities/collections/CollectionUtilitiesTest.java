package de.buw.tmdt.plasma.utilities.collections;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class CollectionUtilitiesTest {

    @Test
    public void findFirst() {
        List<String> input = asList("First", "Second", "Third", "Fourth");

        final Optional<String> sixLong = CollectionUtilities.findFirst(input, s -> s.length() == 6);
        assertTrue(sixLong.isPresent());
        assertEquals("Second", sixLong.get());

        final Optional<String> startsWithFo = CollectionUtilities.findFirst(input, s -> s.startsWith("Fo"));
        assertTrue(startsWithFo.isPresent());
        assertEquals("Fourth", startsWithFo.get());

        final Optional<String> noMatch = CollectionUtilities.findFirst(input, "noMatch"::equals);
        assertFalse(noMatch.isPresent());
    }

    @Test
    public void findFirstInMissingList() {
        final Optional<?> elementInMissingList = CollectionUtilities.findFirst(null, ignored -> true);
        assertFalse(elementInMissingList.isPresent());
    }

    /**
     * Tests if the method correctly returns an empty optional if searching for {@code null}.
     */
    @Test
    public void findFirstNull() {
        //Optional of null is not present. Method must not throw an exception
        final Optional<String> nullElement = CollectionUtilities.findFirst(asList("First", null, "Third"), Objects::isNull);
        assertFalse(nullElement.isPresent());
    }
}