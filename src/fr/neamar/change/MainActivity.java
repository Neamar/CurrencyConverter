package fr.neamar.change;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public TextView resultsText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		resultsText = (TextView) findViewById(R.id.text_result);
		final EditText valueText = (EditText) findViewById(R.id.text_value);
		final Spinner fromSpinner = (Spinner) findViewById(R.id.spinner_from);
		final Spinner toSpinner = (Spinner) findViewById(R.id.spinner_to);
		toSpinner.setSelection(1);
		final Button convert = (Button) findViewById(R.id.button_convert);

		// Format number
		valueText.addTextChangedListener(new NumberTextWatcher(valueText));

		// "Enter" means click on convert:
		valueText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					convert.performClick();
					return true;
				}
				return false;
			}
		});

		convert.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String value = valueText.getText().toString();
				String from = (String) fromSpinner.getSelectedItem();
				String to = (String) toSpinner.getSelectedItem();

				if (value.equals(""))
					value = "1";

				if (from.equals(to)) {
					Toast.makeText(MainActivity.this, "From and to currencies must be different.",
							Toast.LENGTH_SHORT).show();
					toSpinner.requestFocus();
				} else {
					new ConvertTask().execute(from, to, value);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Click on title in actionbar
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.menu_share:
			shareResult();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void shareResult() {
		if (resultsText.getVisibility() == View.VISIBLE) {
			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, resultsText.getText()
					.toString());

			startActivity(Intent.createChooser(sharingIntent, "Share rate"));
		} else {
			Toast.makeText(this, "Please convert a value before sharing.", Toast.LENGTH_SHORT)
					.show();
		}
	}

	private class ConvertTask extends AsyncTask<String, Void, String> {
		private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

		@Override
		protected void onPreExecute() {
			this.dialog.setMessage("Fetching real time convert rate...");
			this.dialog.show();

		}

		@Override
		protected String doInBackground(String... queries) {
			String from = queries[0].substring(0, 3);
			String to = queries[1].substring(0, 3);
			String value = queries[2];

			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet("http://www.google.com/ig/calculator?hl=en&q=" + value
					+ from + "=?" + to);

			try {
				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httpget);

				BufferedReader reader = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent(), "ISO-8859-1"));
				String result = reader.readLine();
				try {
					JSONObject jsonResult = new JSONObject(result);
					String error = jsonResult.getString("error");
					if (!error.equals(""))
						return "An unexpected error occurred";
					String lhs = jsonResult.getString("lhs");
					String rhs = jsonResult.getString("rhs");

					return "<strong>" + lhs + "</strong><br /><small>is</small><br /><strong>"
							+ rhs + "</strong>";
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}

			return "Unable to get conversion rates.";
		}

		@Override
		protected void onPostExecute(String results) {
			if (this.dialog.isShowing()) {
				try {
					this.dialog.dismiss();
				} catch (IllegalArgumentException e) {
				}
			}

			resultsText.setVisibility(View.VISIBLE);
			resultsText.setText(Html.fromHtml(results));
		}
	}
}
