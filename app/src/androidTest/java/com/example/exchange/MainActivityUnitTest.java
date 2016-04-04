package com.example.exchange;

import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.TestCase;

public class MainActivityUnitTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@SmallTest
	public void testchecker() {
		MainActivity ma = new MainActivity();
		boolean result = ma.checker("test");
		assertEquals(true, result);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
