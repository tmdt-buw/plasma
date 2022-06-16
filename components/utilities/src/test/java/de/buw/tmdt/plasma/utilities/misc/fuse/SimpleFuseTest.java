package de.buw.tmdt.plasma.utilities.misc.fuse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleFuseTest {

    private SimpleFuse testee;
    private String testeeName;

    @BeforeEach
    public void setUp() {
        testee = new SimpleFuse();
        testeeName = testee.getClass().getSimpleName();
    }

    @Test
    public void destroy() {
        //when
        testee.destroy();

        //then
        assertFalse(testee.isSane());
        assertTrue(testee.isDestroyed());
    }

    @Test
    public void multiDestroy() {
        //when
        int max = new Random().nextInt(18) + 2;
        for (int i = 0; i < max; i++) {
            testee.destroy();
            //then
            assertFalse(testee.isSane(), "Wrong state after invoking " + testeeName + ".destroy() " + max + " time" + (max == 1 ? "." : "s."));
            assertTrue(testee.isDestroyed(), "Wrong state after invoking " + testeeName + ".destroy() " + max + " time" + (max == 1 ? "." : "s."));
        }

    }

    @Test
    public void pristineFuse() {
        assertTrue(testee.isSane());
        assertFalse(testee.isDestroyed());
    }

    @Test
    public void destroyIfSane() {
        assertTrue(testee.isSane(), "Pristine " + testeeName + " was not sane.");
        assertTrue(testee.destroyIfSane(), testeeName + ".destroyIfSane() returned wrong state.");
        assertTrue(testee.isDestroyed(), "Destroyed " + testeeName + " is in wrong state.");
        assertFalse(testee.destroyIfSane(), testeeName + ".destroyIfSane() returned wrong state.");
    }
}