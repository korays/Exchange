package com.example.exchange;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
	public MainActivityTest() {
		super(MainActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@SmallTest
	public void testFromDateButton() {
		Button bt = (Button) getActivity().findViewById(R.id.fromDate);
		assertNotNull(bt);
	}
	@SmallTest
	public void testToDateButton() {
		Button bt = (Button) getActivity().findViewById(R.id.toDate);
		assertNotNull(bt);
	}
	@SmallTest
	public void testFromCurSpinner() {
		Spinner bt = (Spinner) getActivity().findViewById(R.id.fromCurrency);
		assertNotNull(bt);
	}
	@SmallTest
	public void testToCurSpinner() {
		Spinner bt = (Spinner) getActivity().findViewById(R.id.toCurrency);
		assertNotNull(bt);
	}
	@SmallTest
	public void testGetRatesButton() {
		Button bt = (Button) getActivity().findViewById(R.id.getRates);
		assertNotNull(bt);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
