package de.buw.tmdt.plasma.utilities.buffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractSlidingWindowBufferTest {
	private static final int BUFFER_SIZE = 5;
	private static final int EXPECTED_VALUE = 42;
	private static final int SOME_VALUE = 139;
	private static final int BAD_VALUE = 1337;

	private Function<Integer, ? extends SlidingWindowBuffer<Integer>> testeeConstructor;

	private SlidingWindowBuffer<Integer> testee;


	protected AbstractSlidingWindowBufferTest(Function<Integer, ? extends SlidingWindowBuffer<Integer>> testeeConstructor) {
		this.testeeConstructor = testeeConstructor;
	}

	@BeforeEach
	public void setUp() {
		testee = testeeConstructor.apply(BUFFER_SIZE);
	}

	@Test
	public void push() {
		for (int i = 0; i < 10; i++) {
			testee.push(i);
		}
		for (int i = 0; i < 10; i++) {
			testee.pop();
		}
	}

	@Test
	public void peekAfterEmpty() {
		testee.push(EXPECTED_VALUE);

		final Optional<Integer> result = testee.peek();
		assertTrue(result.isPresent());
		assertEquals(EXPECTED_VALUE, result.get().intValue());
	}

	@Test
	public void peekAfterPartial() {
		testee.push(EXPECTED_VALUE);

		for (int i = 0; i < BUFFER_SIZE / 2; i++) {
			testee.push(BAD_VALUE);
		}

		final Optional<Integer> result = testee.peek();
		assertTrue(result.isPresent());
		assertEquals(EXPECTED_VALUE, result.get().intValue());
	}

	@Test
	public void peekAfterFull() {
		testee.push(EXPECTED_VALUE);

		for (int i = 0; i < BUFFER_SIZE - 1; i++) {
			testee.push(BAD_VALUE);
		}

		final Optional<Integer> result = testee.peek();
		assertTrue(result.isPresent());
		assertEquals(EXPECTED_VALUE, result.get().intValue());
	}

	@Test
	public void peekAfterWrapAround() {
		testee.push(BAD_VALUE);
		testee.push(BAD_VALUE);
		testee.push(EXPECTED_VALUE);

		for (int i = 0; i < BUFFER_SIZE - 1; i++) {
			testee.push(BAD_VALUE);
		}

		final Optional<Integer> result = testee.peek();
		assertTrue(result.isPresent());
		assertEquals(EXPECTED_VALUE, result.get().intValue());
	}

	@Test
	public void popEmpty() {
		assertFalse(testee.pop().isPresent());
	}

	@Test
	public void popDrain() {
		for (int i = 0; i < BUFFER_SIZE; i++) {
			testee.push(i);
		}

		for (int i = 0; i < BUFFER_SIZE; i++) {
			final Optional<Integer> value = testee.pop();
			assertTrue(value.isPresent());
			assertEquals(i, value.get().intValue());
		}

		assertFalse(testee.pop().isPresent());
	}

	@Test
	public void popDrainAfterWrapAround() {
		for (int i = 0; i < BUFFER_SIZE * 2; i++) {
			testee.push(i);
		}

		for (int i = 0; i < BUFFER_SIZE; i++) {
			final Optional<Integer> value = testee.pop();
			assertTrue(value.isPresent());
			assertEquals(i + BUFFER_SIZE, value.get().intValue());
		}

		assertFalse(testee.pop().isPresent());
	}

	@Test
	public void forEachEmpty() {
		testee.forEach(e -> fail("Consumer should not be executed for an empty buffer."));
	}

	@Test
	public void forEachFull() {
		for (int i = 0; i < BUFFER_SIZE; i++) {
			testee.push(i);
		}

		List<Integer> list = new ArrayList<>();

		testee.forEach(list::add);

		assertEquals(5, list.size());
		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals(i, list.get(i).intValue());
		}
	}

	@Test
	public void forEachPartial() {
		final int elementCount = BUFFER_SIZE / 2;

		for (int i = 0; i < elementCount; i++) {
			testee.push(2 * i);
		}

		List<Integer> list = new ArrayList<>();

		testee.forEach(list::add);

		assertEquals(elementCount, list.size());
		for (int i = 0; i < elementCount; i++) {
			assertEquals(i * 2, list.get(i).intValue());
		}
	}

	@Test
	public void testEmpty() {
		assertTrue(testee.isEmpty());
		testee.push(SOME_VALUE);
		assertFalse(testee.isEmpty());
		testee.pop();
		assertTrue(testee.isEmpty());
	}

	@Test
	public void testSize() {
		assertEquals(0, testee.size());
		//[0, BUFFER_SIZE]
		for (int i = 0; i < BUFFER_SIZE; i++) {
			testee.push(SOME_VALUE);
			assertEquals(i + 1, testee.size());
		}

		//{BUFFER_SIZE}
		for (int i = 0; i < BUFFER_SIZE; i++) {
			testee.push(SOME_VALUE);
			assertEquals(BUFFER_SIZE, testee.size());
		}
		//[BUFFER_SIZE-1, 0]
		for (int i = 0; i < BUFFER_SIZE; i++) {
			testee.pop();
			assertEquals(BUFFER_SIZE - (i + 1), testee.size());
		}

		//{0}
		for (int i = 0; i < BUFFER_SIZE; i++) {
			testee.pop();
			assertEquals(0, testee.size());
		}
	}

	@Test
	public void testCapacity() {
		assertEquals(BUFFER_SIZE, testee.capacity());
	}

	@Test
	public void testToListEmpty() {
		assertTrue(testee.toList().isEmpty());
	}

	@Test
	public void testToListPartial() {
		final int elementCount = BUFFER_SIZE / 2;
		for (int i = 0; i < elementCount; i++) {
			testee.push(i);
		}

		List<Integer> result = testee.toList();

		assertEquals(elementCount, result.size());

		for (int i = 0; i < elementCount; i++) {
			assertEquals(i, result.get(i).intValue());
		}
	}

	@Test
	public void testToListFull() {
		final int elementCount = BUFFER_SIZE;
		for (int i = 0; i < elementCount; i++) {
			testee.push(i);
		}

		List<Integer> result = testee.toList();

		assertEquals(elementCount, result.size());

		for (int i = 0; i < elementCount; i++) {
			assertEquals(i, result.get(i).intValue());
		}
	}

	@Test
	public void testToListWrapAround() {
		final int overlap = BUFFER_SIZE / 2;
		for (int i = 0; i < BUFFER_SIZE + overlap; i++) {
			testee.push(i);
		}

		List<Integer> result = testee.toList();

		assertEquals(BUFFER_SIZE, result.size());

		for (int i = 0; i < BUFFER_SIZE; i++) {
			assertEquals(i + overlap, result.get(i).intValue());
		}
	}
}