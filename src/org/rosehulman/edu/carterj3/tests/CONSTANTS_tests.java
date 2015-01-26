package org.rosehulman.edu.carterj3.tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rosehulman.edu.carterj3.CONSTANTS.Value;

public class CONSTANTS_tests {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void test_value() {
		assertTrue(Value.Two.compareTo(Value.Three) < 0);
		assertTrue(Value.Two.compareTo(Value.Two) == 0);
		assertTrue(Value.Three.compareTo(Value.Two) > 0);
	}

}
