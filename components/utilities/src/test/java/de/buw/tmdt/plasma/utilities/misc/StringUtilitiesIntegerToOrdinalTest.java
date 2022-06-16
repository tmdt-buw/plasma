package de.buw.tmdt.plasma.utilities.misc;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class StringUtilitiesIntegerToOrdinalTest {

    private final Logger logger = LoggerFactory.getLogger(StringUtilitiesIntegerToOrdinalTest.class);

    public static Stream<Arguments> parameters() {
        return Stream.of(
                Arguments.of("Zero Counting 0", 0, true, "1st"),
                Arguments.of("Zero Counting 1", 1, true, "2nd"),
                Arguments.of("Zero Counting 2", 2, true, "3rd"),
                Arguments.of("Zero Counting 3", 3, true, "4th"),
                Arguments.of("Zero Counting 10", 10, true, "11th"),
                Arguments.of("Zero Counting 11", 11, true, "12th"),
                Arguments.of("Zero Counting 12", 12, true, "13th"),
                Arguments.of("Zero Counting 13", 13, true, "14th"),
                Arguments.of("Zero Counting 20", 20, true, "21st"),
                Arguments.of("Zero Counting 21", 21, true, "22nd"),
                Arguments.of("Zero Counting 22", 22, true, "23rd"),
                Arguments.of("Zero Counting 23", 23, true, "24th"),
                Arguments.of("Zero Counting 100", 100, true, "101st"),
                Arguments.of("Zero Counting 101", 101, true, "102nd"),
                Arguments.of("Zero Counting 102", 102, true, "103rd"),
                Arguments.of("Zero Counting 103", 103, true, "104th"),
                Arguments.of("Zero Counting 110", 110, true, "111th"),
                Arguments.of("Zero Counting 111", 111, true, "112th"),
                Arguments.of("Zero Counting 112", 112, true, "113th"),
                Arguments.of("Zero Counting 113", 113, true, "114th"),
                Arguments.of("Zero Counting 120", 120, true, "121st"),
                Arguments.of("Zero Counting 121", 121, true, "122nd"),
                Arguments.of("Zero Counting 122", 122, true, "123rd"),
                Arguments.of("Zero Counting 123", 123, true, "124th"),
                Arguments.of("Zero Counting 1000", 1000, true, "1001st"),
                Arguments.of("Zero Counting 1001", 1001, true, "1002nd"),
                Arguments.of("Zero Counting 1002", 1002, true, "1003rd"),
                Arguments.of("Zero Counting 1003", 1003, true, "1004th"),
                Arguments.of("Zero Counting 1010", 1010, true, "1011th"),
                Arguments.of("Zero Counting 1011", 1011, true, "1012th"),
                Arguments.of("Zero Counting 1012", 1012, true, "1013th"),
                Arguments.of("Zero Counting 1013", 1013, true, "1014th"),
                Arguments.of("Zero Counting 1020", 1020, true, "1021st"),
                Arguments.of("Zero Counting 1021", 1021, true, "1022nd"),
                Arguments.of("Zero Counting 1022", 1022, true, "1023rd"),
                Arguments.of("Zero Counting 1023", 1023, true, "1024th"),
                Arguments.of("Natural Counting 1", 1, false, "1st"),
                Arguments.of("Natural Counting 2", 2, false, "2nd"),
                Arguments.of("Natural Counting 3", 3, false, "3rd"),
                Arguments.of("Natural Counting 11", 11, false, "11th"),
                Arguments.of("Natural Counting 12", 12, false, "12th"),
                Arguments.of("Natural Counting 13", 13, false, "13th"),
                Arguments.of("Natural Counting 21", 21, false, "21st"),
                Arguments.of("Natural Counting 22", 22, false, "22nd"),
                Arguments.of("Natural Counting 23", 23, false, "23rd"),
                Arguments.of("Natural Counting 101", 101, false, "101st"),
                Arguments.of("Natural Counting 102", 102, false, "102nd"),
                Arguments.of("Natural Counting 103", 103, false, "103rd"),
                Arguments.of("Natural Counting 111", 111, false, "111th"),
                Arguments.of("Natural Counting 112", 112, false, "112th"),
                Arguments.of("Natural Counting 113", 113, false, "113th"),
                Arguments.of("Natural Counting 121", 121, false, "121st"),
                Arguments.of("Natural Counting 122", 122, false, "122nd"),
                Arguments.of("Natural Counting 123", 123, false, "123rd"),
                Arguments.of("Natural Counting 1001", 1001, false, "1001st"),
                Arguments.of("Natural Counting 1002", 1002, false, "1002nd"),
                Arguments.of("Natural Counting 1003", 1003, false, "1003rd"),
                Arguments.of("Natural Counting 1011", 1011, false, "1011th"),
                Arguments.of("Natural Counting 1012", 1012, false, "1012th"),
                Arguments.of("Natural Counting 1013", 1013, false, "1013th"),
                Arguments.of("Natural Counting 1021", 1021, false, "1021st"),
                Arguments.of("Natural Counting 1022", 1022, false, "1022nd"),
                Arguments.of("Natural Counting 1023", 1023, false, "1023rd"),
                Arguments.of("Zero Counting -1", -1, true, IllegalArgumentException.class),
                Arguments.of("Zero Counting -1234", -1234, true, IllegalArgumentException.class),
                Arguments.of("Natural Counting 0", 0, false, IllegalArgumentException.class),
                Arguments.of("Natural Counting -1", -1, false, IllegalArgumentException.class),
                Arguments.of("Natural Counting -1234", -1234, false, IllegalArgumentException.class)
        );
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void integerToOrdinal(String name, Integer numberInput, Boolean flagInput, Object expectation) {
        Class<? extends Exception> exceptionType;
        String output;
        if (expectation instanceof String) {
            output = (String) expectation;
            exceptionType = null;
        } else if (expectation instanceof Class<?>) {
            output = null;
            if (!ReflectionUtilities.getClassHierarchy((Class<?>) expectation).contains(Exception.class)) {
                throw new IllegalArgumentException("Class does not inherit Exception: " + expectation);
            }
            //noinspection unchecked
            exceptionType = (Class<? extends Exception>) expectation;
        } else {
            throw new IllegalArgumentException("Expectation was neither String nor Class<? extends Exception>");
        }

        if (output != null) {
            testSuccess(name, numberInput, flagInput, output);
        } else {
            testFailure(name, numberInput, flagInput, exceptionType);
        }
    }

    private void testSuccess(String name, Integer numberInput, Boolean flagInput, String output) {
        assertEquals(output, StringUtilities.integerToOrdinal(numberInput, flagInput), name + " failed.");
    }

    private void testFailure(String name, Integer numberInput, Boolean flagInput, Class<? extends Exception> exceptionType) {
        String result;
        try {
            result = StringUtilities.integerToOrdinal(numberInput, flagInput);
        } catch (Exception e) {
            logger.debug("Caught exception." + name, e);
            if (!exceptionType.isInstance(e)) {
                fail("Wrong exception type.\nExpected: " + exceptionType.getCanonicalName() + "\n But found: " + e.getClass().getCanonicalName());
            }
            return;
        }
        fail("Was expecting exception of type " + exceptionType.getCanonicalName() + " but method returned gracefully.\nInput: [" + numberInput + ", " + flagInput + "]\nOutput: " + result);
    }
}