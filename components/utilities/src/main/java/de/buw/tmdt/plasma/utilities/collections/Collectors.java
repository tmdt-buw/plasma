package de.buw.tmdt.plasma.utilities.collections;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collector.Characteristics.UNORDERED;

public final class Collectors {

    private Collectors() {
    }

    /**
     * Returns a Collector which maps elements to serializable keys and values which are aggregated in a serializable map.
     *
     * @param keyMapper   maps an object to a key
     * @param valueMapper maps an object to a value
     * @param <K>         the key type
     * @param <V>         the value type
     * @param <T>         the element type
     *
     * @return a collector for the given types
     */
    public static <T, K extends Serializable, V extends Serializable>
    Collector<T, HashMap<K, V>, HashMap<K, V>> getSerializableMapCollector(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper
    ) {
        return Collector.of(
                HashMap::new,
                (map, element) -> map.put(
                        keyMapper.apply(element),
                        valueMapper.apply(element)
                ),
                (first, second) -> {
                    first.putAll(second);
                    return first;
                },
                UNORDERED,
                IDENTITY_FINISH
        );
    }

    /**
     * Returns a Collector which maps elements to a pair of keys and values where multiple values mapped to the same key are collected in a HashSet.
     * Formally this Collector generates a function: {@code f: K -> powerset(V)}
     *
     * @param keyMapper   the function from the elements to the keys
     * @param valueMapper the function from the elements to the values
     * @param <T>         the type of the elements of the stream to collect
     * @param <K>         the type of the keys of the target collection
     * @param <V>         the type of the elements of the collection which is the value of the target collection
     *
     * @return a collector from T -&gt; {@code HashMap<K, Set<V>>}
     */
    public static <T, K, V> Collector<T, HashMap<K, Set<V>>, HashMap<K, Set<V>>> getAggregatingCollector(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper
    ) {
        return Collector.of(
                HashMap::new,
                (HashMap<K, Set<V>> map, T element) -> map.computeIfAbsent(
                        keyMapper.apply(element),
                        (K k) -> new HashSet<>()
                ).add(valueMapper.apply(element)),
                (HashMap<K, Set<V>> firstMap, HashMap<K, Set<V>> secondMap) -> {
                    for (Map.Entry<K, Set<V>> entry : secondMap.entrySet()) {
                        firstMap.computeIfAbsent(
                                entry.getKey(),
                                key -> new HashSet<>()
                        ).addAll(entry.getValue());
                    }
                    return firstMap;
                },
                UNORDERED,
                IDENTITY_FINISH
        );
    }

    /**
     * Returns a collector which collects Serializable elements in a serializable list.
     *
     * @param <T> the type of the element
     *
     * @return the collector for serializable elements
     */
    public static <T extends Serializable>
    Collector<T, ArrayList<T>, ArrayList<T>> getSerializableListCollector() {
        return Collector.of(
                ArrayList::new,
                ArrayList::add,
                (first, second) -> {
                    first.addAll(second);
                    return first;
                },
                UNORDERED,
                IDENTITY_FINISH
        );
    }

    /**
     * Returns a collector which collects Serializable elements in a serializable set.
     *
     * @param <T> the type of the element
     *
     * @return the collector for serializable elements
     */
    public static <T extends Serializable>
    Collector<T, HashSet<T>, HashSet<T>> getSerializableSetCollector() {
        return Collector.of(
                HashSet::new,
                Set::add,
                (first, second) -> {
                    first.addAll(second);
                    return first;
                },
                UNORDERED,
                IDENTITY_FINISH
        );
    }

    /**
     * Returns a collector which collects Serializable elements in a serializable ArrayDeque used as stack.
     *
     * @param <T> the type of the element
     *
     * @return the collector for serializable elements
     */
    public static <T extends Serializable>
    Collector<T, ArrayDeque<T>, ArrayDeque<T>> getSerializableStackCollector() {
        return Collector.of(
                ArrayDeque::new,
                Deque::addFirst,
                (first, second) -> {
                    first.addAll(second);
                    return first;
                },
                IDENTITY_FINISH
        );
    }
}
