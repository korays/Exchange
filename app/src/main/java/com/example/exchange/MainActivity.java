package com.example.exchange;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "MainActivity";

	private DateFormat dateFormatter;
	private String fromDateStr = "";
	private String toDateStr = "";
	private Date fromDate;
	private Date toDate;

	private String currency1;
	private ArrayList<String> currencyList1 = new ArrayList<>();
	private String currency2;
	private ArrayList<String> currencyList2 = new ArrayList<>();

	private DatePickerDialog fromDatePickerDialog;
	private DatePickerDialog toDatePickerDialog;
	private int counter;
	private LineChart mLineChart;
	private Spinner fromCurrency;
	private Spinner toCurrency;
	private ProgressBar progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

		progress = (ProgressBar) findViewById(R.id.progressBar);

		final Button fromDateBtn = (Button) findViewById(R.id.fromDate);
		assert fromDateBtn != null;
		fromDateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fromDatePickerDialog.show();
			}
		});
		final Button toDateBtn = (Button) findViewById(R.id.toDate);
		assert toDateBtn != null;
		toDateBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toDatePickerDialog.show();
			}
		});


		fromCurrency = (Spinner) findViewById(R.id.fromCurrency);
		toCurrency = (Spinner) findViewById(R.id.toCurrency);


		Calendar newCalendar = Calendar.getInstance();
		fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				Calendar newDate = Calendar.getInstance();
				newDate.set(year, monthOfYear, dayOfMonth);
				fromDate = newDate.getTime();
				fromDateStr = dateFormatter.format(fromDate);
				fromDateBtn.setText(fromDateStr);
				progress.setVisibility(View.VISIBLE);
				getCurrenciesFrom();
			}

		},newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

		toDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				Calendar newDate = Calendar.getInstance();
				newDate.set(year, monthOfYear, dayOfMonth);
				toDate = newDate.getTime();
				toDateStr = dateFormatter.format(toDate);
				toDateBtn.setText(toDateStr);
				progress.setVisibility(View.VISIBLE);
				getCurrenciesTo();
			}

		},newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

		fromCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				currency1 = (String) parent.getItemAtPosition(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				currency1 = "";
			}
		});

		toCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				currency2 = (String) parent.getItemAtPosition(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				currency2 = "";
			}
		});

		final Button getRatesBtn = (Button) findViewById(R.id.getRates);
		assert getRatesBtn != null;
		getRatesBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (!fromDateStr.equals("") && !toDateStr.equals("")) {
					if (fromDate.after(toDate)) {
						Toast.makeText(MainActivity.this, "'From date' must be earlier than 'To date'", Toast.LENGTH_LONG).show();
					} else {
						if (currency1.equals("") || currency2.equals("")) {
							Toast.makeText(MainActivity.this, "Please select currency", Toast.LENGTH_LONG).show();
						} else {
							if (currency1.equals(currency2)) {
								Toast.makeText(MainActivity.this, "Please select different currencies", Toast.LENGTH_LONG).show();
							} else {
								getRates();
							}
						}
					}
				} else {
					Toast.makeText(MainActivity.this, "Please select dates", Toast.LENGTH_LONG).show();
				}

			}
		});

		mLineChart = (LineChart) findViewById(R.id.chart);
		assert mLineChart != null;
		mLineChart.getAxisRight().setEnabled(false);


		VolleyLog.DEBUG = false;
	}

	private void getRates() {

		progress.setVisibility(View.VISIBLE);

		try {
			fromDate = dateFormatter.parse(fromDateStr);
			toDate = dateFormatter.parse(toDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		RequestQueue mRequestQueue;

		// Instantiate the cache
		Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

		// Set up the network to use HttpURLConnection as the HTTP client.
		Network network = new BasicNetwork(new HurlStack());

		// Instantiate the RequestQueue with the cache and network.
		mRequestQueue = new RequestQueue(cache, network);

		counter = 0;


		final int daysBetweenDates = diffInDays(fromDate, toDate);

		final ArrayList<Entry> rateValues = new ArrayList<>();

		final ArrayList<String> xVals = new ArrayList<>();


		while (fromDate.before(toDate)) {

			String uri = String.format("http://api.fixer.io/%1$s?base=%2$s&symbols=%3$s",
					dateFormatter.format(fromDate),
					currency1,
					currency2);


			Log.i(TAG, "uri:\t" + uri);


			JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
					Request.Method.GET,
					uri,
					null,
					new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							try {
								JSONObject rates = response.getJSONObject("rates");
								Float rate = Float.parseFloat(rates.getString(currency2));

								String dateStr = response.getString("date");
								int diffInDays = diffInDays(dateFormatter.parse(dateStr), toDate);

								rateValues.add(new Entry(rate, daysBetweenDates - diffInDays));
								xVals.add(dateStr);

								counter++;

								Log.i(TAG, "rate: " + rate + "\tcount:\t" + (daysBetweenDates - diffInDays) + "\tcounter:\t" + counter + "\tdate:\t" + dateStr + "\ttodate\t" + dateFormatter.format(toDate));

								if (counter == daysBetweenDates) { //set chart data if all responses received
									setChartData(currency1, currency2, rateValues, xVals);
								}

							} catch (JSONException e) {
								e.printStackTrace();
								Log.i(TAG, "error json parse");
							} catch (ParseException e) {
								e.printStackTrace();
								Log.i(TAG, "error date format parse");
							}
						}
					},
					new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							Log.i(TAG, "error response");
							counter++;
							if (counter == daysBetweenDates) { //set chart data if all responses received
								setChartData(currency1, currency2, rateValues, xVals);
							}
						}
					}
			);
			mRequestQueue.add(jsonObjectRequest);//add request to queue
			fromDate.setTime(fromDate.getTime() + 86400000);//add 1 day

		}

		mRequestQueue.start();//start all requests

	}

	private void setChartData(String currency1, String currency2, ArrayList<Entry> rateValues, ArrayList<String> xVals) {
		if (rateValues.isEmpty()) {
			Toast.makeText(MainActivity.this, "Wrong currencies selected", Toast.LENGTH_LONG).show();
		} else {
			Collections.sort(xVals);
			Collections.sort(rateValues, new Comparator<Entry>() {
				@Override
				public int compare(Entry lhs, Entry rhs) {
					return lhs.getXIndex() - rhs.getXIndex();
				}
			});

			LineDataSet setComp1 = new LineDataSet(rateValues, currency2 + "/" + currency1);
			setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);


			final LineData data = new LineData(xVals, setComp1);

			mLineChart.setData(data);
			mLineChart.setDescription("Exchange rates from " + fromDateStr + " to " + toDateStr);
			mLineChart.invalidate();
			Log.i(TAG, "set chart data");

			progress.setVisibility(View.INVISIBLE);
		}
	}


	private int diffInDays(Date fromDate, Date toDate) {
		return (int)( (toDate.getTime() - fromDate.getTime())
				/ (86400000) );
	}

	private void getCurrenciesFrom() {

		String uri = String.format("http://api.fixer.io/%1$s",	fromDateStr);

		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
				Request.Method.GET,
				uri,
				null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							JSONObject rates = response.getJSONObject("rates");
							Iterator<String> keys = rates.keys();
							currencyList1.clear();
							while (keys.hasNext()) {
								currencyList1.add(keys.next());
							}

							if (!currencyList2.isEmpty()) {
								ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
										android.R.layout.simple_spinner_item);
								currencyList1.retainAll(currencyList2);
								adapter.addAll(currencyList1);

								fromCurrency.setAdapter(adapter);
								toCurrency.setAdapter(adapter);
							}
							progress.setVisibility(View.INVISIBLE);

						} catch (JSONException e) {
							e.printStackTrace();
							Log.i(TAG, "error json parse");
						}
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.i(TAG, "error response");
						getCurrenciesFrom();//repeat
					}
				}
		);
		Volley.newRequestQueue(this).add(jsonObjectRequest);
	}

	private void getCurrenciesTo() {

		String uri = String.format("http://api.fixer.io/%1$s",	toDateStr);

		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
				Request.Method.GET,
				uri,
				null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							JSONObject rates = response.getJSONObject("rates");
							Iterator<String> keys = rates.keys();
							currencyList2.clear();
							while (keys.hasNext()) {
								currencyList2.add(keys.next());
							}

							if (!currencyList1.isEmpty()) {
								ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
										android.R.layout.simple_spinner_item);
								currencyList2.retainAll(currencyList1);
								adapter.addAll(currencyList2);

								fromCurrency.setAdapter(adapter);
								toCurrency.setAdapter(adapter);
							}
							progress.setVisibility(View.INVISIBLE);

						} catch (JSONException e) {
							e.printStackTrace();
							Log.i(TAG, "error json parse");
						}
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.i(TAG, "error response");
						getCurrenciesTo();//repeat
					}
				}
		);
		Volley.newRequestQueue(this).add(jsonObjectRequest);
	}

	public boolean checker(String s) {
		return s.equals("test");
	}

}
