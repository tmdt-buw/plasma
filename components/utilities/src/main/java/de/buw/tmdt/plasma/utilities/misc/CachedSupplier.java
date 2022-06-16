package de.buw.tmdt.plasma.utilities.misc;

import java.util.function.Supplier;

public class CachedSupplier<T> implements Supplier<T> {

    private final Supplier<T> supplier;
    private T cachedSupply;

    public CachedSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
        this.cachedSupply = null;
    }

    @Override
    public synchronized T get() {
        if (cachedSupply == null) {
            cachedSupply = supplier.get();
        }
        return cachedSupply;
    }

    public synchronized boolean clear() {
        boolean result = this.cachedSupply != null;
        this.cachedSupply = null;
        return result;
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public synchronized String toString() {
        return "{\"@class\":\"CachedSupplier\""
               + ", \"supplier\":" + supplier
               + ", \"cachedSupply\":" + cachedSupply
               + '}';
    }
}
