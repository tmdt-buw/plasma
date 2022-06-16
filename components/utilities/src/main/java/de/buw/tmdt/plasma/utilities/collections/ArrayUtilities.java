package de.buw.tmdt.plasma.utilities.collections;

import de.buw.tmdt.plasma.utilities.misc.Pair;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class ArrayUtilities {
    private ArrayUtilities() {
    }

    public static boolean contains(double[] array, double element) {
        if (array != null) {
            for (double d : array) {
                if (d == element) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean contains(long[] array, long element) {
        if (array != null) {
            for (long l : array) {
                if (l == element) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T> boolean contains(T[] array, T element) {
        return contains(array, element, Objects::equals);
    }

    public static <T> boolean contains(T[] array, T element, BiFunction<T, T, Boolean> comparator) {
        if (array != null) {
            for (T t : array) {
                if (comparator.apply(element, t)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T> Pair<Integer, Integer> findDuplicate(@NotNull T[] array) {
        return findDuplicate(array, Objects::equals);
    }

    /**
     * Searches through the array for an element which is equal to another according to the passed comparing {@code BiFunction<T, T, Boolean>}. If one is
     * found a {@code Pair<Integer, Integer>} containing the indices. If no duplicate was found {@code null} is returned.
     *
     * @param array      the array to check for duplicates
     * @param comparator the comparator function
     * @param <T>        the element type
     *
     * @return the duplicate that was found
     */
    @SuppressWarnings("ObjectEquality")
    public static <T> Pair<Integer, Integer> findDuplicate(
            @NotNull T[] array,
            @NotNull BiFunction<T, T, Boolean> comparator
    ) {
        for (int i = 0; i < array.length; i++) {
            T first = array[i];
            for (int j = i + 1; j < array.length; j++) {
                T second = array[j];
                if (first == second || comparator.apply(first, second)) {
                    return new Pair<>(i, j);
                }
            }
        }
        return null;
    }

    /**
     * Checks if two arrays contain the same objects but does ignore the order.
     *
     * @param first      first array to compare
     * @param second     second array to compare
     * @param comparator operation to determine equality
     * @param <T>        the type of elements
     *
     * @return true if the contents are the same
     */
    @SuppressWarnings("ArrayEquality")
    public static <T> boolean equalsIgnoreOrder(
            @NotNull T[] first,
            @NotNull T[] second,
            @NotNull BiFunction<T, T, Boolean> comparator
    ) {
        if (first == second) {
            return true;
        } else if (first.length != second.length) {
            return false;
        }

        List<T> firstAsList = new ArrayList<>(Arrays.asList(first));
        List<T> secondAsList = new ArrayList<>(Arrays.asList(second));

        for (T element1 : firstAsList) {
            Optional<T> found = secondAsList.stream()
                    .filter(element2 -> comparator.apply(element1, element2))
                    .findFirst();
            if (!found.isPresent()) {
                return false;
            } else {
                secondAsList.remove(found.get());
            }
        }
        return true;
    }

    /**
     * Creates a {@link HashSet} from an array {@code T[]}.
     *
     * @param elements the elements of the set
     * @param <T>      the element type
     *
     * @return a {@link HashSet} containing the passed elements.
     *
     * @deprecated This is deprecated in favour of {@link ArrayUtilities#toCollection(Supplier, Object[])} (since = "0.4.10", forRemoval = false)
     */
    @SafeVarargs
    @Deprecated//since = "0.4.11", forRemoval = true
    public static <T> HashSet<T> toHashSet(T... elements) {
        return toCollection(HashSet::new, elements);
    }

    /**
     * Creates a {@link Collection} created via the passed provider and adds all elements to it. If the supplied collection contained elements they are not
     * explicitly removed but might if {@link Collection#add(Object)} does.
     * <p>
     * Example: {@code
     * List<String> strings = ArrayUtilities.toCollection(ArrayList::new, "foo", "bar");
     * }
     *
     * @param supplier collection supplier (usually constructor reference)
     * @param elements elements to add to the collection
     * @param <T>      element type
     * @param <C>      collection type
     *
     * @return {@link Collection} which contains all given elements
     */
    @SafeVarargs
    public static <T, C extends Collection<? super T>> C toCollection(Supplier<? extends C> supplier, T... elements) {
        C collection = supplier.get();
        Collections.addAll(collection, elements);
        return collection;
    }

    /**
     * Similar to {@link Arrays#asList(Object[])} but returns a mutable data structure.
     *
     * @param elements elements to put in the List
     * @param <T>      type of the elements
     *
     * @return mutable List containing the passed elements
     *
     * @deprecated This is deprecated in favour of {@link ArrayUtilities#toCollection(Supplier, Object[])} (since = "0.4.11", forRemoval = false)
     */
    @SafeVarargs
    @Deprecated//since = "0.4.11", forRemoval = true
    public static <T> List<T> mutableList(T... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    /**
     * Wrapper for {@link Array#newInstance(Class, int...)} with a less fucked up signature. Type-inference and generics!
     * You speak that standard lib?
     *
     * @param type       class object to identify the element type of the array to create
     * @param dimensions the dimensions of the array
     * @param <T>        the element type
     *
     * @return the array
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] create(@NotNull Class<T> type, @NotNull int... dimensions) {
        return (T[]) Array.newInstance(type, dimensions);
    }
}
