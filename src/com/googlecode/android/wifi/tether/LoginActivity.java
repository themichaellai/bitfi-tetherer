package com.googlecode.android.wifi.tether;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	public static final String TAG = "TETHER -> LoginActivity";

	private EditText usernameField;
	private EditText passwordField;
	private Button loginButton;

	private OnClickListener loginButtonListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate() called!");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		usernameField = (EditText) findViewById(R.id.usernameField);
		passwordField = (EditText) findViewById(R.id.passwordField);
		loginButton = (Button) findViewById(R.id.loginButton);

		loginButtonListener = new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "Clicked login");
				String username = usernameField.getText().toString();
				String password = passwordField.getText().toString();
				try {
					verifyCredentials(username, password,
							"http://54.210.58.4/login");
				} catch (ClientProtocolException e) {
					Log.e(TAG, e.toString());
				} catch (IOException e) {
					Log.e(TAG, e.toString());
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				// TODO: make API call
				// intent.setAction(TetherService.SERVICEMANAGE_INTENT);
				// intent.putExtra("state", TetherService.SERVICE_START);
				// sendBroadcast(intent);
			}
		};
		loginButton.setOnClickListener(loginButtonListener);
	}

	private void verifyCredentials(String username, String password, String url)
			throws ClientProtocolException, IOException, URISyntaxException {
		RetrieveCredentialsTask task = new RetrieveCredentialsTask() {
			@Override
			protected void onPostExecute(Boolean result) {
				LoginActivity.this.credentialCallback(result);
			}
		};
		task.execute(username, password, url);
	}

	public void credentialCallback(boolean isValid) {
		Intent mainActivityIntent = new Intent(LoginActivity.this,
				MainActivity.class);
		if (isValid) {
			startActivity(mainActivityIntent);
		} else {
			Context ctx = getApplicationContext();
			CharSequence text = "Invalid username/password!";
			Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show();
		}
	}
}

abstract class RetrieveCredentialsTask extends AsyncTask<String, Void, Boolean> {

	private Exception exception;

	protected Boolean doInBackground(String... params) {
		try {
			String username = params[0];
			String password = params[1];
			String path = params[2];
			Uri uri = Uri.parse(path).buildUpon()
					.appendQueryParameter("email", username.toString())
					.appendQueryParameter("password", password.toString())
					.build();

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient
			// .execute(new HttpGet(uri.toString()));
					.execute(new HttpGet(uri.toString()));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				String responseString = out.toString();
				return true;
			} else {
				response.getEntity().getContent().close();
				// throw new IOException(statusLine.getReasonPhrase());
				return false;
			}
		} catch (Exception e) {
			this.exception = e;
			return null;
		}
	}
	// protected abstract void onPostExecute(Boolean feed)
}