package de.buw.tmdt.plasma.utilities.collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class MapUtilities {

    private MapUtilities() {
    }

    /**
     * Merges the second map into the first one. This is done by adding each entry of the second map into the first map. If a conflict occurs, which is when
     * for a {@code K \in second.keySet()} the expression {@code first.contains(K)} is {@code true}, the resolveConflict functional is invoked. with the
     * key K  for  which the conflict occurred, the value V_1 for K from the first map and the value V_2 for K from the second map (f(K,V_1,V_2)).
     * ø
     *
     * @param first    the map where the elements are added to
     * @param second   the map from which the elements are added
     * @param resolver the method which is invoked if a key exists in both maps
     * @param <K>      the key type of the maps
     * @param <V>      the value type of the maps
     */
    public static <K, V> void innerMerge(Map<K, V> first, Map<K, V> second, Resolver<K, V> resolver) {
        for (Map.Entry<K, V> secondEntry : second.entrySet()) {
            if (first.containsKey(secondEntry.getKey())) {
                resolver.resolve(secondEntry.getKey(), first.get(secondEntry.getKey()), secondEntry.getValue());
            } else {
                first.put(secondEntry.getKey(), secondEntry.getValue());
            }
        }
    }

    /**
     * Checks if the given element is either a key or a value in the given map. In opposite to invoking
     * {@link java.util.Set#contains(Object)} on the set returned by {@link Map#values()} and {@link Map#keySet()}
     * this method does not throw {@link NullPointerException}s.
     *
     * @param map     the map which is searched
     * @param element the element which is looked for
     *
     * @return true iff there is a key or value x in the map for which {@code Objects.equals(x, element} is true
     * - false otherwise
     */
    public static boolean mapContains(@Nullable Map<?, ?> map, @Nullable Object element) {
        if (map == null) {
            return false;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (Objects.equals(entry.getKey(), element) || Objects.equals(entry.getValue(), element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsNull(@Nullable Map<?, ?> map) {
        return mapContains(map, null);
    }

    /**
     * Searches the map for a value and returns the key it is stored for. The method iterates over the entry set and
     * returns the key for the first entry where the value matches. If the value is not present in the map an
     * {@link IllegalArgumentException} is thrown.
     *
     * @param map   the map to search in
     * @param value the value to search for
     * @param <K>   the key type
     * @param <V>   the value type
     *
     * @return the key {@code k} for which {@code map.get(k) == value} is {@code true}
     */
    public static <K, V> K keyOfValue(Map<? extends K, ? super V> map, V value) {
        for (Map.Entry<? extends K, ? super V> entry : map.entrySet()) {
            if (Objects.equals(entry.getValue(), value)) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Value " + value + " is not a value of the provided map.");
    }

    /**
     * Creates a new map with the identical values but new keys.
     *
     * @param map             the old map
     * @param newKeyGenerator the generator function for the new key
     * @param <K1>            old key type
     * @param <K2>            new key type
     * @param <V>             value type
     *
     * @return a new map with the replaced keys
     *
     * @throws IllegalArgumentException if a new key is generated twice
     */
    public static <K1, K2, V> Map<K2, V> remap(@NotNull Map<K1, V> map, @NotNull Function<? super Map.Entry<K1, V>, ? extends K2> newKeyGenerator) {
        HashMap<K2, V> result = new HashMap<>(map.size());

        for (Map.Entry<K1, V> k1VEntry : map.entrySet()) {
            final K2 newKey = newKeyGenerator.apply(k1VEntry);
            if (result.put(newKey, k1VEntry.getValue()) != null) {
                throw new IllegalArgumentException("The newKeyGenerator produced a duplicate key: `" + newKey + "`.");
            }
        }

        return result;
    }

    /**
     * Creates a new map with identical keys but new values which are computed by applying the passed function.
     *
     * @param input    the old map
     * @param function the value projection function
     * @param <K>      the key type
     * @param <V1>     old value type
     * @param <V2>     new value type
     *
     * @return a new map with the replaced values
     */
    public static <K, V1, V2> Map<K, V2> mapValues(Map<K, V1> input, Function<? super V1, ? extends V2> function) {
        Map<K, V2> result = new HashMap<>(input.size());
        for (Map.Entry<K, V1> entry : input.entrySet()) {
            result.put(entry.getKey(), function.apply(entry.getValue()));
        }
        return result;
    }

    /**
     * Creates a map of the given collection of elements by mapping it to the key provided by the given generator. If the key generator produces equivalent keys
     * for different elements the resulting map will contain only the latest element with the ambigous key.
     *
     * @param elements     the elements which should be put into the map
     * @param keyGenerator the operation to calculate the key for a given element
     * @param <K>          the key type
     * @param <V>          the element type
     *
     * @return the map of elements with the provided keys
     */
    public static <K, V> Map<K, V> map(Collection<V> elements, Function<? super V, ? extends K> keyGenerator) {
        return elements.stream().collect(Collectors.toMap(
                keyGenerator,
                Function.identity()
        ));
    }

    /**
     * Returns an inverted map where every entry (K, V) from the source map is stored as (V, K) in the result map.
     *
     * @param map the map to invert
     * @param <K> the key type of the result
     * @param <V> the value type of the result
     *
     * @return the inverted map
     *
     * @throws IllegalArgumentException iff any two keys of the input map relate to the same value
     */
    public static <K, V> Map<K, V> flip(Map<? extends V, ? extends K> map) {
        Map<K, V> result = new HashMap<>(map.size());
        for (Map.Entry<? extends V, ? extends K> entry : map.entrySet()) {
            final V replacedValue = result.put(entry.getValue(), entry.getKey());
            if (replacedValue != null) {
                throw new IllegalArgumentException(String.format(
                        "Value `%s` was mapped by `%s` and `%s` creating an ambiguity in the flipped map.",
                        entry.getValue(),
                        replacedValue,
                        entry.getKey()
                ));
            }
        }
        return result;
    }

    /*
		&#x1D6C3; = \beta
		&#x1D6FD; = \beta
		&#x2286; = \subseteq
		&#x2A2F; = \times
		&#x220A; = \in
		&#x222A; = \cup
		&#x2203; = \exists
		&#x2227; = \land
		&#x2228; = \lor
    */

    /**
     * Composes a map from the keys of the first map to the values of the second map. The created map is described as R<sub>3</sub> in the following equations:
     * <p>
     * Y &#x2286; A
     * <br>
     * R<sub>1</sub> &#x2286; X &#x2A2F; Y
     * <br>
     * R<sub>2</sub> &#x2286; A &#x2A2F; B
     * <br>
     * R<sub>3</sub> := {
     * (x, b) | x &#x220A; X, b &#x220A; B,
     * &#x2203; y &#x220A; Y: (xR<sub>1</sub>y &#x2227; yR<sub>2</sub>b)
     * }
     *
     * @param first  the map providing the key set
     * @param second the map providing the value map
     * @param <K1>   key type of the first map
     * @param <V1>   value type of the first map
     * @param <K2>   key type of the second map
     * @param <V2>   value type of the second map
     *
     * @return the
     */
    public static <K1, V1 extends K2, K2, V2> Map<K1, V2> compose(Map<K1, V1> first, Map<K2, V2> second) {
        Map<K1, V2> result = new HashMap<>();
        for (Map.Entry<K1, V1> firstEntry : first.entrySet()) {
            K2 potentialKey2 = firstEntry.getValue();
            if (second.containsKey(potentialKey2)) {
                result.put(firstEntry.getKey(), second.get(potentialKey2));
            }
        }
        return result;
    }

    /**
     * Composes a map from the keys of the first map to the values of the second map. The created map is described as R<sub>3</sub> in the following equations:
     * <p>
     * f: Y &#x2B62 A
     * <br>
     * R<sub>1</sub> &#x2286; X &#x2A2F; Y
     * <br>
     * R<sub>2</sub> &#x2286; A &#x2A2F; B
     * <br>
     * R<sub>3</sub> := {
     * (x, b) | x &#x220A; X, b &#x220A; B,
     * &#x2203; y &#x220A; Y: (xR<sub>1</sub>y &#x2227; f(y)R<sub>2</sub>b)
     * }
     *
     * @param first   the map providing the key set
     * @param second  the map providing the value map
     * @param mapping the mapping from V1 to K2
     * @param <K1>    key type of the first map
     * @param <V1>    value type of the first map
     * @param <K2>    key type of the second map
     * @param <V2>    value type of the second map
     *
     * @return the
     */
    public static <K1, V1, K2, V2> Map<K1, V2> compose(Map<K1, V1> first, Map<K2, V2> second, Function<? super V1, ? extends K2> mapping) {
        Map<K1, V2> result = new HashMap<>();
        for (Map.Entry<K1, V1> firstEntry : first.entrySet()) {
            K2 potentialKey2 = mapping.apply(firstEntry.getValue());
            if (second.containsKey(potentialKey2)) {
                result.put(firstEntry.getKey(), second.get(potentialKey2));
            }
        }
        return result;
    }

    public interface Resolver<K, V> {
        void resolve(K key, V first, V second);
    }
}
