package de.buw.tmdt.plasma.utilities.collections.collectionutilities;

import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NullSafeCopyTest<T> {

    public static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of(
                        "HashSet to empty HashSet",
                        new Parameter<>(
                                HashSet::new,
                                new HashSet<>(Arrays.asList("foo", "bar")),
                                new HashSet<>(Arrays.asList("foo", "bar"))
                        )
                ),
                Arguments.of(
                        "HashSet to empty ArrayList",
                        new Parameter<>(
                                ArrayList::new,
                                new HashSet<>(Arrays.asList("foo", "bar")),
                                new ArrayList<>(Arrays.asList("foo", "bar"))
                        )
                ),
                Arguments.of(
                        "ArrayList to empty HashSet (duplicates)",
                        new Parameter<>(
                                HashSet::new,
                                new ArrayList<>(Arrays.asList("foo", "bar", "foo")),
                                new HashSet<>(Arrays.asList("foo", "bar"))
                        )
                ),
                Arguments.of(
                        "ArrayList to empty HashSet (no duplicates)",
                        new Parameter<>(
                                HashSet::new,
                                new ArrayList<>(Arrays.asList("foo", "bar")),
                                new HashSet<>(Arrays.asList("foo", "bar"))
                        )
                ),
                Arguments.of(
                        "ArrayList to empty ArrayList",
                        new Parameter<>(
                                ArrayList::new,
                                new ArrayList<>(Arrays.asList("foo", "bar")),
                                new ArrayList<>(Arrays.asList("foo", "bar"))
                        )
                ),
                Arguments.of(
                        "null to empty HashSet",
                        new Parameter<>(
                                HashSet::new,
                                null,
                                Collections.emptyList()
                        )
                ),
                Arguments.of(
                        "null to empty ArrayList",
                        new Parameter<>(
                                ArrayList::new,
                                null,
                                new ArrayList<>()
                        )
                ),
                Arguments.of(
                        "HashSet to pre-filled HashSet",
                        new Parameter<>(
                                () -> new HashSet<>(Arrays.asList("some", "pre set", "values")),
                                new HashSet<>(Arrays.asList("foo", "bar")),
                                new HashSet<>(Arrays.asList("some", "pre set", "values", "foo", "bar"))
                        )
                ),
                Arguments.of(
                        "HashSet to pre-filled ArrayList",
                        new Parameter<>(
                                () -> new ArrayList<>(Arrays.asList("some", "pre set", "values")),
                                new HashSet<>(Arrays.asList("foo", "bar")),
                                new ArrayList<>(Arrays.asList("some", "pre set", "values", "foo", "bar"))
                        )
                ),
                Arguments.of(
                        "ArrayList to pre-filled HashSet (duplicates)",
                        new Parameter<>(
                                () -> new HashSet<>(Arrays.asList("some", "pre set", "values")),
                                new ArrayList<>(Arrays.asList("foo", "bar", "foo")),
                                new HashSet<>(Arrays.asList("some", "pre set", "values", "foo", "bar"))
                        )
                ),
                Arguments.of(
                        "ArrayList to pre-filled HashSet (no duplicates)",
                        new Parameter<>(
                                () -> new HashSet<>(Arrays.asList("some", "pre set", "values")),
                                new ArrayList<>(Arrays.asList("foo", "bar")),
                                new HashSet<>(Arrays.asList("some", "pre set", "values", "foo", "bar"))
                        )
                ),
                Arguments.of(
                        "ArrayList to pre-filled ArrayList",
                        new Parameter<>(
                                () -> new ArrayList<>(Arrays.asList("some", "pre set", "values")),
                                new ArrayList<>(Arrays.asList("foo", "bar")),
                                new HashSet<>(Arrays.asList("some", "pre set", "values", "foo", "bar"))
                        )
                ),
                Arguments.of(
                        "null to pre-filled HashSet",
                        new Parameter<>(
                                () -> new HashSet<>(Arrays.asList("some", "pre set", "values")),
                                null,
                                new HashSet<>(Arrays.asList("some", "pre set", "values"))
                        )
                ),
                Arguments.of(
                        "null to pre-filled ArrayList",
                        new Parameter<>(
                                () -> new ArrayList<>(Arrays.asList("some", "pre set", "values")),
                                null,
                                new ArrayList<>(Arrays.asList("some", "pre set", "values"))
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    @SuppressWarnings("unchecked")
    public void testNullSafeCopy(String name, Parameter parameter) {
        final Collection<? super T> result = CollectionUtilities.nullSafeCopy(parameter.targetSupplier, parameter.source);
        assertEquals(result.getClass(), parameter.targetSupplier.get().getClass());
        if (parameter.source == null) {
            assertEquals(result, parameter.targetSupplier.get());
        } else {
            //equals with ignore order
            assertTrue(result.containsAll(parameter.expectedResult));
            assertTrue(parameter.expectedResult.containsAll(result));
        }
    }

    private static class Parameter<T> {
        public final @NotNull Supplier<Collection<? super T>> targetSupplier;
        public final @Nullable Collection<? extends T> source;
        public final @NotNull Collection<? extends T> expectedResult;

        public Parameter(@NotNull Supplier<Collection<? super T>> targetSupplier, @Nullable Collection<? extends T> source, @NotNull Collection<? extends T> expectedResult) {
            this.targetSupplier = targetSupplier;
            this.source = source;
            this.expectedResult = expectedResult;
        }
    }
}
