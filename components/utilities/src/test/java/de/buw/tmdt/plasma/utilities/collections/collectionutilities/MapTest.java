package de.buw.tmdt.plasma.utilities.collections.collectionutilities;

import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class MapTest<T, U> {

    private final @NotNull Supplier<Collection<? super U>> targetSupplier;
    private final @Nullable Iterable<? extends T> source;
    private final @NotNull Function<? super T, ? extends U> mappingFunction;
    private final @NotNull Collection<? extends U> expectedResult;

    public MapTest(String ignoredName, Parameter<T, U> parameter) {
        this.targetSupplier = parameter.targetSupplier;
        this.source = parameter.source;
        this.mappingFunction = parameter.mappingFunction;
        this.expectedResult = parameter.expectedResult;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(
                new Object[]{
                        "String length from list to set",
                        new Parameter<>(
                                asList("foo", "bar"),
                                HashSet::new,
                                String::length,
                                new HashSet<>(asList(3, 3))
                        )
                }, new Object[]{
                        "Reverse Strings from list to list",
                        new Parameter<>(
                                asList("Some", "different", "String"),
                                ArrayList::new,
                                s -> new StringBuilder(s).reverse().toString(),
                                new ArrayList<>(asList("emoS", "tnereffid", "gnirtS"))
                        )
                }, new Object[]{
                        "Square integers from iterable to linked list",
                        new Parameter<>(
                                () -> asList(1, 2, 3, 4, 5).iterator(),
                                LinkedList::new,
                                i -> i * i,
                                new LinkedList<>(asList(1, 4, 9, 16, 25))
                        )
                }, new Object[]{
                        "Empty list input",
                        new Parameter<>(
                                Collections.emptyList(),
                                ArrayList::new,
                                s -> {
                                    throw new RuntimeException("This method should've not been called for an empty list. But was with: `" + s + '`');
                                }, new ArrayList<>()
                        )
                }, new Object[]{
                        "null input",
                        new Parameter<>(
                                null,
                                LinkedHashSet::new,
                                s -> {
                                    throw new RuntimeException("This method should've not been called for an empty list. But was with: `" + s + '`');
                                }, new LinkedHashSet<>()
                        )
                }
        );
    }

    @Test
    public void testNullSafeCopy() {
        final Collection<? super U> result = CollectionUtilities.map(source, mappingFunction, targetSupplier);
        assertEquals(result.getClass(), targetSupplier.get().getClass());
        if (source == null) {
            assertEquals(result, targetSupplier.get());
        } else {
            //equals with ignore order
            assertTrue(result.containsAll(expectedResult));
            assertTrue(expectedResult.containsAll(result));
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
