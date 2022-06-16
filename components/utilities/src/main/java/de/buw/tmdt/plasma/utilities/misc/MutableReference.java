package de.buw.tmdt.plasma.utilities.misc;

/**
 * Wrapper class for a single element. Useful when needing an effective final element for a lambda which should be mutated. The main purpose of this class is
 * to provide a self explaining type. If performance if a concern use a T[] of size 1 instead.
 *
 * @param <T> type of the wrapped element
 */
public class MutableReference<T> {

    private T element;
    private boolean defined;

    public MutableReference() {
        element = null;
        defined = false;
    }

    public MutableReference(T element) {
        this.element = element;
        this.defined = true;
    }

    /**
     * Retrieve the referenced element.
     *
     * @return the referenced element.
     *
     * @throws IllegalStateException iff {@link MutableReference#set(Object)} was not invoked before.
     */
    public T get() throws IllegalStateException {
        if (!defined) {
            throw new IllegalStateException("Reference is not defined.");
        }
        return element;
    }

    public void set(T element) {
        this.element = element;
        this.defined = true;
    }

    public boolean isDefined() {
        return this.defined;
    }

    public boolean isNull() {
        return this.defined && this.element == null;
    }

    /**
     * Clears the reference and returns the currently referenced element if any exists.
     *
     * @return the currently referenced element
     */
    public T reset() {
        T old = this.element;
        this.element = null;
        this.defined = false;
        return old;
    }
}
