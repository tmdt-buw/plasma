package de.buw.tmdt.plasma.utilities.buffer;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A FIFO buffer which uses a list as backing data structure.
 *
 * @param <T>
 */
public class ListSlidingWindowBuffer<T> implements SlidingWindowBuffer<T> {

	private final ArrayList<T> list;
	private final int capacity;

	public ListSlidingWindowBuffer(int capacity) {
		if (capacity < 1) {
			throw new IllegalArgumentException("capacity must be a positive integer");
		}
		this.capacity = capacity;
		list = new ArrayList<>(capacity);
	}

	@Override
	public Optional<T> push(@NotNull T value) {
		Optional<T> result = Optional.empty();
		if (list.size() == capacity) {
			result = Optional.of(list.remove(0));
		}
		list.add(value);
		return result;
	}

	@Override
	public Optional<T> peek() {
		return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
	}

	@Override
	public Optional<T> pop() {
		return list.isEmpty() ? Optional.empty() : Optional.of(list.remove(0));
	}

	@Override
	public void forEach(Consumer<? super T> consumer) {
		list.forEach(consumer);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public int capacity() {
		return capacity;
	}

	@Override
	public List<T> toList() {
		return new ArrayList<>(list);
	}
}
