package de.buw.tmdt.plasma.utilities.misc;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SequenceCache<T> {
    private static final Logger logger = LoggerFactory.getLogger(SequenceCache.class);

    private final ArrayList<T> cache = new ArrayList<>();
    private final Function<T, String> printer;
    private final BiFunction<T, T, Boolean> equals;

    public SequenceCache(Function<T, String> printer, BiFunction<T, T, Boolean> equals) {
        this.printer = printer;
        this.equals = equals;
    }

    public void store(@NotNull T data, int sequenceId) throws IllegalStateException {
        if (sequenceId >= cache.size()) {
            //insert null elements for missing packets in sequence
            for (int i = cache.size(); i < sequenceId; i++) {
                cache.add(null);
            }
            cache.add(data);
        } else {
            T cachedElement = cache.get(sequenceId);
            if (cachedElement == null) {
                cache.set(sequenceId, data);
            } else if (!equals.apply(cachedElement, data)) {
                logger.warn(
                        "Cached element for sequence id {} is different from provided element for the same id. Cached: {}\nReplayed: {}",
                        sequenceId,
                        printer.apply(cachedElement),
                        printer.apply(data)
                );
                throw new IllegalStateException("Cached element for sequence id {} is different from provided element with the same id.");
            }
        }
    }

    public List<T> getContent() {
        return new ArrayList<>(cache);
    }

    public int size() {
        return cache.size();
    }

    public boolean hasGaps() {
        return cache.contains(null);
    }

    public List<Integer> getGaps() {
        ArrayList<Integer> keys = new ArrayList<>();
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i) == null) {
                keys.add(i);
            }
        }
        return keys;
    }

    public void clear() {
        cache.clear();
    }
}