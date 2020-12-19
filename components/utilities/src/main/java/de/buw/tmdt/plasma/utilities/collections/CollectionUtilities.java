package de.buw.tmdt.plasma.utilities.collections;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Contains utility methods.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class CollectionUtilities {
    private CollectionUtilities() {
    }

    /**
     * Returns true iff the given collection contains the given element. In opposite to {@link Collection#contains(Object)} this method does not throw {@link NullPointerException}s.
     *
     * @param collection the collection which is searched
     * @param element    the element which is looked for
     * @param <T>        the element Type
     * @return true iff there is an element e in collection for which {@code Objects.equals(e, element} is true - false otherwise
     */
    public static <T> boolean collectionContains(@Nullable Collection<? super T> collection, @Nullable T element) {
        if (collection == null) {
            return false;
        }
        for (java.lang.Object o : collection) {
            if (Objects.equals(o, element)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean containsNull(@Nullable Collection<? super T> collection) {
        return collectionContains(collection, null);
    }

    /**
     * Returns true if at least one of the elements of the second collection ({@code testes}) is contained in the first collection ({@code collection}). If either collection is empty or null the method returns false.
     *
     * @param collection the collection which is searched
     * @param testes     the collection with the testees
     * @param <T>        the element type
     * @return true iff at least one element of the second collection is in the first collection - false otherwise
     */
    public static <T> boolean containsAny(@Nullable Collection<? super T> collection, @Nullable Collection<T> testes) {
        if (collection == null || testes == null) {
            return false;
        }
        if (collection.isEmpty() || testes.isEmpty()) {
            return false;
        }
        for (T element : testes) {
            if (collectionContains(collection, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Searches for the first element in iterator order which is accepted by the provided predicate.
     *
     * @param collection the collection to search in
     * @param condition  the condition the element must fulfill
     * @param <T>        the type of the searched element
     * @return the first element in {@code collection} which is accepted by {@code condition}
     */
    public static <T> Optional<T> findFirst(@Nullable Collection<? extends T> collection, @NotNull Predicate<T> condition) {
        if (collection != null) {
            for (T t : collection) {
                if (condition.test(t)) {
                    return Optional.ofNullable(t);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Copies all elements from {@code source} to the via {@code supplier} provided collection and returns the latter for ease of use.
     *
     * @param targetSupplier supplier for the
     * @param source         elements which are copied to {@code supplier}
     * @param <T>            element type
     * @param <C>            collection type
     * @return The supplied collection where all elements from source are added
     */
    @Contract("null, _ -> fail; _, !null -> !null")
    public static <T, C extends Collection<? super T>> C nullSafeCopy(@NotNull Supplier<? extends C> targetSupplier, @Nullable Collection<? extends T> source) {
        C collection = targetSupplier.get();
        if (source != null) {
            collection.addAll(source);
        }
        return collection;
    }

    /**
     * Maps all elements from an input iterable to a collection of the given type.
     *
     * @param <T>                   Input element type
     * @param <U>                   Output element type
     * @param <COut>                Output collection type
     * @param collection            Collection of input elements
     * @param mapping               The mapping function which projects the input elements to the output collection
     * @param collectionConstructor Provider for the resulting collection.
     * @return a collection containing all
     */
    @Contract("_, !null, !null -> !null; _, _, null -> fail; _, null, _ -> fail")
    public static <T, U, COut extends Collection<? super U>> @NotNull COut map(
            @Nullable Iterable<? extends T> collection,
            @NotNull Function<? super T, ? extends U> mapping,
            @NotNull Supplier<COut> collectionConstructor
    ) {
        COut result = collectionConstructor.get();
        if (collection != null) {
            for (T t : collection) {
                result.add(mapping.apply(t));
            }
        }
        return result;
    }
}
