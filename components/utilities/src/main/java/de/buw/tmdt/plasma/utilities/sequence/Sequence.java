package de.buw.tmdt.plasma.utilities.sequence;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

public class Sequence<T> implements Iterator<T>, Supplier<T>, Iterable<T> {

    private final @NotNull Incrementor<@NotNull T> successorGenerator;
    private @NotNull T previous;
    private T cached = null;

    public Sequence(@NotNull T seed, @NotNull Incrementor<@NotNull T> successorGenerator) {
        this.previous = seed;
        this.successorGenerator = successorGenerator;
    }

    @Override
    public boolean hasNext() {
        if (cached == null) {
            try {
                cached = successorGenerator.of(previous);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull T next() throws NoSuchElementException {
        if (cached == null) {
            try {
                cached = successorGenerator.of(previous);
            } catch (Exception e) {
                throw new NoSuchElementException("There is no successor to " + previous + '.');
            }
        }
        try {
            return cached;
        } finally {
            previous = cached;
            cached = null;
        }
    }

    @Override
    public T get() {
        return next();
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @FunctionalInterface
    public interface Incrementor<T> {
        T of(T previous) throws Exception;
    }
}
