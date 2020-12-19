package de.buw.tmdt.plasma.utilities.misc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class StringUtilitiesIntegerToOrdinalTest {


	private final String name;
	private final Integer numberInput;
	private final Boolean flagInput;
	private final String output;
	private final Class<? extends Exception> exceptionType;
	private Logger logger = LoggerFactory.getLogger(StringUtilitiesIntegerToOrdinalTest.class);

	public StringUtilitiesIntegerToOrdinalTest(String name, Integer numberInput, Boolean flagInput, Object expectation) {
		this.name = name;
		this.numberInput = numberInput;
		this.flagInput = flagInput;
		if (expectation instanceof String) {
			this.output = (String) expectation;
			this.exceptionType = null;
		} else if (expectation instanceof Class<?>) {
			this.output = null;
			if (!ReflectionUtilities.getClassHierarchy((Class<?>) expectation).contains(Exception.class)) {
				throw new IllegalArgumentException("Class does not inherit Exception: " + expectation);
			}
			//noinspection unchecked
			this.exceptionType = (Class<? extends Exception>) expectation;
		} else {
			throw new IllegalArgumentException("Expectation was neither String nor Class<? extends Exception>");
		}
	}

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> parameters() {
		return Arrays.asList(
				wrap("Zero Counting 0", 0, true, "1st"),
				wrap("Zero Counting 1", 1, true, "2nd"),
				wrap("Zero Counting 2", 2, true, "3rd"),
				wrap("Zero Counting 3", 3, true, "4th"),
				wrap("Zero Counting 10", 10, true, "11th"),
				wrap("Zero Counting 11", 11, true, "12th"),
				wrap("Zero Counting 12", 12, true, "13th"),
				wrap("Zero Counting 13", 13, true, "14th"),
				wrap("Zero Counting 20", 20, true, "21st"),
				wrap("Zero Counting 21", 21, true, "22nd"),
				wrap("Zero Counting 22", 22, true, "23rd"),
				wrap("Zero Counting 23", 23, true, "24th"),
				wrap("Zero Counting 100", 100, true, "101st"),
				wrap("Zero Counting 101", 101, true, "102nd"),
				wrap("Zero Counting 102", 102, true, "103rd"),
				wrap("Zero Counting 103", 103, true, "104th"),
				wrap("Zero Counting 110", 110, true, "111th"),
				wrap("Zero Counting 111", 111, true, "112th"),
				wrap("Zero Counting 112", 112, true, "113th"),
				wrap("Zero Counting 113", 113, true, "114th"),
				wrap("Zero Counting 120", 120, true, "121st"),
				wrap("Zero Counting 121", 121, true, "122nd"),
				wrap("Zero Counting 122", 122, true, "123rd"),
				wrap("Zero Counting 123", 123, true, "124th"),
				wrap("Zero Counting 1000", 1000, true, "1001st"),
				wrap("Zero Counting 1001", 1001, true, "1002nd"),
				wrap("Zero Counting 1002", 1002, true, "1003rd"),
				wrap("Zero Counting 1003", 1003, true, "1004th"),
				wrap("Zero Counting 1010", 1010, true, "1011th"),
				wrap("Zero Counting 1011", 1011, true, "1012th"),
				wrap("Zero Counting 1012", 1012, true, "1013th"),
				wrap("Zero Counting 1013", 1013, true, "1014th"),
				wrap("Zero Counting 1020", 1020, true, "1021st"),
				wrap("Zero Counting 1021", 1021, true, "1022nd"),
				wrap("Zero Counting 1022", 1022, true, "1023rd"),
				wrap("Zero Counting 1023", 1023, true, "1024th"),
				wrap("Natural Counting 1", 1, false, "1st"),
				wrap("Natural Counting 2", 2, false, "2nd"),
				wrap("Natural Counting 3", 3, false, "3rd"),
				wrap("Natural Counting 11", 11, false, "11th"),
				wrap("Natural Counting 12", 12, false, "12th"),
				wrap("Natural Counting 13", 13, false, "13th"),
				wrap("Natural Counting 21", 21, false, "21st"),
				wrap("Natural Counting 22", 22, false, "22nd"),
				wrap("Natural Counting 23", 23, false, "23rd"),
				wrap("Natural Counting 101", 101, false, "101st"),
				wrap("Natural Counting 102", 102, false, "102nd"),
				wrap("Natural Counting 103", 103, false, "103rd"),
				wrap("Natural Counting 111", 111, false, "111th"),
				wrap("Natural Counting 112", 112, false, "112th"),
				wrap("Natural Counting 113", 113, false, "113th"),
				wrap("Natural Counting 121", 121, false, "121st"),
				wrap("Natural Counting 122", 122, false, "122nd"),
				wrap("Natural Counting 123", 123, false, "123rd"),
				wrap("Natural Counting 1001", 1001, false, "1001st"),
				wrap("Natural Counting 1002", 1002, false, "1002nd"),
				wrap("Natural Counting 1003", 1003, false, "1003rd"),
				wrap("Natural Counting 1011", 1011, false, "1011th"),
				wrap("Natural Counting 1012", 1012, false, "1012th"),
				wrap("Natural Counting 1013", 1013, false, "1013th"),
				wrap("Natural Counting 1021", 1021, false, "1021st"),
				wrap("Natural Counting 1022", 1022, false, "1022nd"),
				wrap("Natural Counting 1023", 1023, false, "1023rd"),
				wrap("Zero Counting -1", -1, true, IllegalArgumentException.class),
				wrap("Zero Counting -1234", -1234, true, IllegalArgumentException.class),
				wrap("Natural Counting 0", 0, false, IllegalArgumentException.class),
				wrap("Natural Counting -1", -1, false, IllegalArgumentException.class),
				wrap("Natural Counting -1234", -1234, false, IllegalArgumentException.class)
		);
	}

	private static Object[] wrap(String name, int number, boolean flag, String expectation) {
		return new Object[]{name, number, flag, expectation};
	}

	private static Object[] wrap(String name, int number, boolean flag, Class<? extends Exception> exceptionType) {
		return new Object[]{name, number, flag, exceptionType};
	}

	@Test
	public void integerToOrdinal() {
		if (this.output != null) {
			testSuccess();
		} else if (this.exceptionType != null) {
			testFailure();
		}
	}

	private void testSuccess() {
		assertEquals(name + " failed.", output, StringUtilities.integerToOrdinal(numberInput, flagInput));
	}

	private void testFailure() {
		String result;
		try {
			result = StringUtilities.integerToOrdinal(numberInput, flagInput);
		} catch (Exception e) {
			logger.debug("Caught exception.", e);
			if (!this.exceptionType.isInstance(e)) {
				fail("Wrong exception type.\nExpected: " + this.exceptionType.getCanonicalName() + "\n But found: " + e.getClass().getCanonicalName());
			}
			return;
		}
		fail("Was expecting exception of type " + this.exceptionType.getCanonicalName() + " but method returned gracefully.\nInput: [" + numberInput + ", " + flagInput + "]\nOutput: " + result);
	}
}