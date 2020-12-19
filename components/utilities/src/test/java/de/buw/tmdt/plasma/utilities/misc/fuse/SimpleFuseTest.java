package de.buw.tmdt.plasma.utilities.misc.fuse;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SimpleFuseTest {
	public static final Logger logger = LoggerFactory.getLogger(SimpleFuseTest.class);
	private SimpleFuse testee;
	private String testeeName;

	@Before
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
			assertFalse("Wrong state after invoking "+testeeName+".destroy() " + max + " time" + (max == 1 ? "." : "s."), testee.isSane());
			assertTrue("Wrong state after invoking "+testeeName+".destroy() " + max + " time" + (max == 1 ? "." : "s."), testee.isDestroyed());
		}

	}

	@Test
	public void pristineFuse() {
		assertTrue(testee.isSane());
		assertFalse(testee.isDestroyed());
	}

	@Test
	public void destroyIfSane() {
		assertTrue("Pristine "+testeeName+" was not sane.", testee.isSane());
		assertTrue(testeeName+".destroyIfSane() returned wrong state.", testee.destroyIfSane());
		assertTrue("Destroyed "+testeeName+" is in wrong state.", testee.isDestroyed());
		assertFalse(testeeName+".destroyIfSane() returned wrong state.", testee.destroyIfSane());
	}
}