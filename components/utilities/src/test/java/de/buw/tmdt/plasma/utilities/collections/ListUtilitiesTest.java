package de.buw.tmdt.plasma.utilities.collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ListUtilitiesTest{
    private final Function<List<?>, ?> method;
    private final List<?> input;
    private final Object expectation;

    public ListUtilitiesTest(Function<List<?>, ?> method, List<?> input, Object expectation) {
        this.method = method;
        this.input = input;
        this.expectation = expectation;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        List<String> example = Arrays.asList("first", "second", "third", "fourth");
        return Arrays.asList(
                wrap(ListUtilities::getLastElement, example, "fourth"),
                wrap(ListUtilities::getLastElement, emptyList(), null),
                wrap(ListUtilities::truncateFirstElement, example, example.subList(1, 4)),
                wrap(ListUtilities::truncateFirstElement, emptyList(), emptyList()),
                wrap(ListUtilities::truncateLastElement, example, example.subList(0, 3)),
                wrap(ListUtilities::truncateLastElement, emptyList(), emptyList()),
                wrap(ListUtilities::nullSafeCopy, example, example),
                wrap(ListUtilities::nullSafeCopy, null, emptyList())
        );
    }

    private static <E, R> Object[] wrap(Function<List<E>, R> method, List<E> input, R expectation) {
        return new Object[]{method, input, expectation};
    }

    @Test
    public void test() {
        Object result = method.apply(input);
        assertEquals(expectation, result);
    }

}
