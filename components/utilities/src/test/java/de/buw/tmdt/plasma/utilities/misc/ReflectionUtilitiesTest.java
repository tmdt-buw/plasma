package de.buw.tmdt.plasma.utilities.misc;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReflectionUtilitiesTest {
	private static final Logger logger = LoggerFactory.getLogger(ReflectionUtilitiesTest.class);

	@Test
	public void getArrayTypeOf() {
		Map<Class<?>, List<?>> typeToElementsMap = new HashMap<>();
		typeToElementsMap.put(String.class, Arrays.asList("first", "second", "third"));
		typeToElementsMap.put(Integer.class, Arrays.asList(1, 2, 3));
		typeToElementsMap.put(List.class, Arrays.asList(Arrays.asList(1, 2, 3), Arrays.asList("a", "b", "c")));
		typeToElementsMap.put(String[].class, Arrays.asList(new String[]{"a", "b", "c"}, new String[]{"x", "y", "z"}, new String[]{"5", "6", "7"}));


		for (Map.Entry<Class<?>, List<?>> classListEntry : typeToElementsMap.entrySet()) {
			Class<?> elementType = classListEntry.getKey();
			logger.debug("Element type: {}", elementType);
			Class<?> arrayType = ReflectionUtilities.getArrayTypeOf(elementType);
			logger.debug("Array type: {}", arrayType);
			int arraySize = classListEntry.getValue().size();

			assertTrue("Generated type is no array.", arrayType.isArray());

			Object[] array = (Object[]) Array.newInstance(arrayType.getComponentType(), arraySize);
			for (int i = 0; i < arraySize; i++) {
				Object element = classListEntry.getValue().get(i);
				logger.debug("List Element : {} {}", element, element.getClass());
				array[i] = element;
				logger.debug("Array Element: {} {}", array[i], array[i].getClass());
				assertEquals("Elements were changed when assigning to array.", element, array[i]);
			}
		}
	}
}