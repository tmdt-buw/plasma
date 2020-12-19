package de.buw.tmdt.plasma.utilities.misc;

import org.junit.Test;

import static org.junit.Assert.*;

public class MutableReferenceTest {

    @Test
    public void get() {
        final MutableReference<String> ref = new MutableReference<>("hello");
        final String result = ref.get();

        assertEquals("hello", result);
    }

    @Test
    public void getNull() {
        final MutableReference<String> ref = new MutableReference<>(null);
        final String result = ref.get();

        assertNull(result);
    }

    @Test(expected = IllegalStateException.class)
    public void getOnUninitializedReference() {
        final MutableReference<Boolean> ref = new MutableReference<>();
        final boolean result = ref.get();
    }

    @Test
    public void setThenGet() {
        final MutableReference<Integer> ref = new MutableReference<>(12);
        ref.set(42);
        final int result = ref.get();

        assertEquals(42, result);
    }

    @Test
    public void isNullAndDefined() {
        final MutableReference<?> ref = new MutableReference<>(null);

        assertTrue(ref.isNull());
        assertTrue(ref.isDefined());
    }

    @Test
    public void isNotNullAndDefined() {
        final MutableReference<Integer> ref = new MutableReference<>(1);

        assertFalse(ref.isNull());
        assertTrue(ref.isDefined());
    }

    @Test
    public void isNotNullWithUnsetReference() {
        final MutableReference<Integer> ref = new MutableReference<>();

        assertFalse(ref.isNull());
        assertFalse(ref.isDefined());
    }

    @Test
    public void setNullThenIsNullAndDefined() {
        final MutableReference<Integer> ref = new MutableReference<>(3);
        ref.set(null);

        assertTrue(ref.isNull());
        assertTrue(ref.isDefined());
    }

    @Test
    public void reset() {
        final MutableReference<String> ref = new MutableReference<>("test");
        final String result = ref.reset();

        assertEquals("test", result);
        assertFalse(ref.isDefined());
        assertFalse(ref.isNull());
    }

    @Test
    public void resetWithUnsetReference() {
        final MutableReference<String> ref = new MutableReference<>();
        final String result = ref.reset();

        assertNull(result);
        assertFalse(ref.isDefined());
        assertFalse(ref.isNull());
    }
}
