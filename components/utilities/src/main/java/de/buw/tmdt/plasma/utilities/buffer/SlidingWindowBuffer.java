package de.buw.tmdt.plasma.utilities.buffer;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A FIFO buffer which drops elements if more elements are pushed than it's
 * capacity.
 * The oldest element will be dropped on each exceeding push operation.
 * Uses Optionals to prevent exceptions in cases where
 * {@link SlidingWindowBuffer#pop()} or {@link SlidingWindowBuffer#peek()}
 * are called on an empty buffer.
 *
 * @param <T> the type of elements
 */
public interface SlidingWindowBuffer<T> {
	/**
	 * Adds an element at the end of the buffer. Returns the dropped element
	 * if the push exceeds the buffers capacity.
	 *
	 * @param value the value to store
	 * @return the dropped element
	 */
	Optional<T> push(@NotNull T value);

	/**
	 * Returns the oldest element of the buffer.
	 *
	 * @return the oldest element
	 */
	Optional<T> peek();

	/**
	 * Removes the oldest element of the buffer and returns it.
	 *
	 * @return the oldest element
	 */
	Optional<T> pop();

	/**
	 * Invokes the consumer for each element of the buffer in the order they
	 * were pushed.
	 *
	 * @param consumer the consumer which receives the elements
	 */
	void forEach(Consumer<? super T> consumer);

	/**
	 * Returns the number of elements in this buffer. If this buffer
	 * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
	 * <tt>Integer.MAX_VALUE</tt>.
	 *
	 * @return the number of elements in this buffer
	 */
	int size();

	/**
	 * Returns <tt>true</tt> if this buffer contains no elements.
	 *
	 * @return <tt>true</tt> if this buffer contains no elements
	 */
	boolean isEmpty();

	/**
	 * Returns the maximum capacity of this buffer.
	 *
	 * @return the maximum capacity
	 */
	int capacity();

	/**
	 * Returns the current elements of the buffer as a list in the order they
	 * were added.
	 *
	 * @return a list of the elements in this buffer
	 */
	List<T> toList();
}
