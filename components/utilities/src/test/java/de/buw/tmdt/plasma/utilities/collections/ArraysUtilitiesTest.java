package de.buw.tmdt.plasma.utilities.collections;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ArraysUtilitiesTest {

	@Test
	public void testEqualsIgnoreOrderComparables() {
		String[] firstArray = new String[]{"first", "second", "third"};
		String[] secondArray = new String[]{"second", "third", "first"};

		assertTrue(ArrayUtilities.equalsIgnoreOrder(firstArray, secondArray,
				(nonComparableElement, nonComparableElement2) -> nonComparableElement.compareTo(nonComparableElement2) == 0));
	}

	@Test
	public void testEqualsIgnoreOrderNonComparables() {
		NonComparableElement[] firstArray = new NonComparableElement[2];
		firstArray[0] = new NonComparableElement("first");
		firstArray[1] = new NonComparableElement("second");

		NonComparableElement[] secondArray = new NonComparableElement[2];
		secondArray[0] = new NonComparableElement("second");
		secondArray[1] = new NonComparableElement("first");

		assertTrue(ArrayUtilities.equalsIgnoreOrder(firstArray, secondArray,
				(nonComparableElement, nonComparableElement2) -> nonComparableElement.getField().compareTo(nonComparableElement2.getField()) == 0));
	}

	@Test
	public void testEqualsIgnoreOrderDuplicates() {
		String[] firstArray = new String[]{"first", "third", "third"};
		String[] secondArray = new String[]{"second", "third", "first"};

		assertFalse(ArrayUtilities.equalsIgnoreOrder(firstArray, secondArray,
				(nonComparableElement, nonComparableElement2) -> nonComparableElement.compareTo(nonComparableElement2) == 0));
	}

	@Test
	public void testToCollectionForHashSet() {
		Set<String> resultingCollection1 = ArrayUtilities.toCollection(HashSet::new, "first", "second", "third");
		Set<? extends String> resultingCollection2 = ArrayUtilities.toCollection(HashSet::new, "first", "second", "third");
		Set<? super String> resultingCollection3 = ArrayUtilities.toCollection(HashSet::new, "first", "second", "third");
		Set<CharSequence> resultingCollection4 = ArrayUtilities.toCollection(HashSet::new, "first", "second", "third");
		HashSet<String> resultingCollection5 = ArrayUtilities.toCollection(HashSet::new, "first", "second", "third");

		assertTypeAndSize(resultingCollection1, HashSet.class, 3);
		assertTypeAndSize(resultingCollection2, HashSet.class, 3);
		assertTypeAndSize(resultingCollection3, HashSet.class, 3);
		assertTypeAndSize(resultingCollection4, HashSet.class, 3);
		assertTypeAndSize(resultingCollection5, HashSet.class, 3);
	}

	@Test
	public void testToCollectionForArrayList() {
		List<String> resultingCollection1 = ArrayUtilities.toCollection(ArrayList::new, "first", "second", "third");
		List<? extends String> resultingCollection2 = ArrayUtilities.toCollection(ArrayList::new, "first", "second", "third");
		List<? super String> resultingCollection3 = ArrayUtilities.toCollection(ArrayList::new, "first", "second", "third");
		List<CharSequence> resultingCollection4 = ArrayUtilities.toCollection(ArrayList::new, "first", "second", "third");
		ArrayList<String> resultingCollection5 = ArrayUtilities.toCollection(ArrayList::new, "first", "second", "third");

		assertTypeAndSize(resultingCollection1, ArrayList.class, 3);
		assertTypeAndSize(resultingCollection2, ArrayList.class, 3);
		assertTypeAndSize(resultingCollection3, ArrayList.class, 3);
		assertTypeAndSize(resultingCollection4, ArrayList.class, 3);
		assertTypeAndSize(resultingCollection5, ArrayList.class, 3);
	}

	private void assertTypeAndSize(Collection<?> collection, Class<?> collectionType, int size) {
		assertEquals("Collection has wrong type.", collectionType, collection.getClass());
		assertEquals("Collection has wrong size.", size, collection.size());
	}

	private static class NonComparableElement {
		private String field;

		public NonComparableElement(String field) {
			this.field = field;
		}

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}
	}
}
