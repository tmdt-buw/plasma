package de.buw.tmdt.plasma.utilities.collections.collectionutilities;

import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapTest<T, U> {

    public static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of(
                        "String length from list to set",
                        new Parameter<>(
                                asList("foo", "bar"),
                                HashSet::new,
                                String::length,
                                new HashSet<>(asList(3, 3))
                        )
                ), Arguments.of(
                        "Reverse Strings from list to list",
                        new Parameter<>(
                                asList("Some", "different", "String"),
                                ArrayList::new,
                                s -> new StringBuilder(s).reverse().toString(),
                                new ArrayList<>(asList("emoS", "tnereffid", "gnirtS"))
                        )
                ), Arguments.of(
                        "Square integers from iterable to linked list",
                        new Parameter<>(
                                () -> asList(1, 2, 3, 4, 5).iterator(),
                                LinkedList::new,
                                i -> i * i,
                                new LinkedList<>(asList(1, 4, 9, 16, 25))
                        )
                ), Arguments.of(
                        "Empty list input",
                        new Parameter<>(
                                Collections.emptyList(),
                                ArrayList::new,
                                s -> {
                                    throw new RuntimeException("This method should've not been called for an empty list. But was with: `" + s + '`');
                                }, new ArrayList<>()
                        )
                ), Arguments.of(
                        "null input",
                        new Parameter<>(
                                null,
                                LinkedHashSet::new,
                                s -> {
                                    throw new RuntimeException("This method should've not been called for an empty list. But was with: `" + s + '`');
                                }, new LinkedHashSet<>()
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    @SuppressWarnings("unchecked")
    public void testNullSafeCopy(String name, Parameter parameter) {
        final Collection<? super U> result = CollectionUtilities.map(parameter.source, parameter.mappingFunction, parameter.targetSupplier);
        assertEquals(result.getClass(), parameter.targetSupplier.get().getClass());
        if (parameter.source == null) {
            assertEquals(result, parameter.targetSupplier.get());
        } else {
            //equals with ignore order
            assertTrue(result.containsAll(parameter.expectedResult));
            assertTrue(parameter.expectedResult.containsAll(result));
        }
    }

    private static class Parameter<T, U> {
        private final @NotNull Supplier<Collection<? super U>> targetSupplier;
        private final @Nullable Iterable<? extends T> source;
        private final @NotNull Function<? super T, ? extends U> mappingFunction;
        private final @NotNull Collection<? extends U> expectedResult;

        public Parameter(
                @Nullable Iterable<? extends T> source,
                @NotNull Supplier<Collection<? super U>> targetSupplier,
                @NotNull Function<? super T, ? extends U> mappingFunction,
                @NotNull Collection<? extends U> expectedResult) {
            this.targetSupplier = targetSupplier;
            this.source = source;
            this.mappingFunction = mappingFunction;
            this.expectedResult = expectedResult;
        }
    }
}
