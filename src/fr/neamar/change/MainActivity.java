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
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
		
		Button convert = (Button) findViewById(R.id.button_convert);
		convert.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String value = valueText.getText().toString();
				String from = (String) fromSpinner.getSelectedItem();
				String to = (String) toSpinner.getSelectedItem();
				
				if(value.equals(""))
					value = "1";
				
				if(from.equals(to))
				{
					Toast.makeText(MainActivity.this, "From and to currencies must be different.", Toast.LENGTH_SHORT).show();
					toSpinner.requestFocus();
				}
				else
				{
					new ConvertTask().execute(from, to, value);
				}
			}
		});
	}

	private class ConvertTask extends AsyncTask<String, Void, String> {
		private final ProgressDialog dialog = new ProgressDialog(
				MainActivity.this);

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
			HttpGet httpget = new HttpGet(
					"http://www.google.com/ig/calculator?hl=en&q=" + value
							+ from + "=?" + to);

			try {
				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httpget);

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));
				String result = reader.readLine();

				try {
					JSONObject jsonResult = new JSONObject(result);
					String error = jsonResult.getString("error");
					if(!error.equals(""))
						return "An unexpected error occurred";
					String lhs = jsonResult.getString("lhs");
					String rhs = jsonResult.getString("rhs");
					
					return "<strong>" + lhs + "</strong><br /><small>is</small><br /><strong>" + rhs + "</strong>";
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
			if (this.dialog.isShowing())
				this.dialog.dismiss();

			resultsText.setVisibility(View.VISIBLE);
			resultsText.setText(Html.fromHtml(results));
		}
	}
}
