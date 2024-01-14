package com.example.marketplace.ui.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.marketplace.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginFragment extends Fragment {

	private LoginViewModel loginViewModel;

	private EditText editTextUsername, editTextPassword;
	private Button buttonLogin;
	private TextView textViewSignUpLink;

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

		View root = inflater.inflate(R.layout.fragment_login, container, false);

		// Initialize UI components
		editTextUsername = root.findViewById(R.id.editTextUsername);
		editTextPassword = root.findViewById(R.id.editTextPassword);
		buttonLogin = root.findViewById(R.id.buttonLogin);
		textViewSignUpLink = root.findViewById(R.id.textViewSignUpLink);

		// Set up click listener for the login button
		buttonLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Perform login when the button is clicked
				performLogin();
			}
		});

		// Set up click listener for the signup link
		textViewSignUpLink.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_signupFragment);
			}
		});

		return root;
	}

	private void performLogin() {
		String username = editTextUsername.getText().toString().trim();
		String password = editTextPassword.getText().toString().trim();

		// Validate input (you may add more validation as needed)

		// Create a JSON string with login data
		String jsonData = "{\"email\":\"" + username + "\",\"password\":\"" + password + "\"}";

		// Perform login in a background thread
		new LoginTask().execute(jsonData);
	}

	private class LoginTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			try {
				URL url = new URL("http://192.168.11.128:8080/user/login");
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

				try {
					urlConnection.setRequestMethod("POST");
					urlConnection.setRequestProperty("Content-Type", "application/json");
					urlConnection.setDoOutput(true);

					// Write the JSON data to the output stream
					try (OutputStream os = urlConnection.getOutputStream()) {
						byte[] input = params[0].getBytes(StandardCharsets.UTF_8);
						os.write(input, 0, input.length);
					}

					if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
						InputStream in = new BufferedInputStream(urlConnection.getInputStream());
						String response = convertInputStreamToString(in);
						Log.d("Response", "JSON Response: " + response);
						// Parse the JSON response and extract the token
						return response; // Return jsonResponse directly
					} else {
						return null;
					}
				} finally {
					urlConnection.disconnect();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		private String convertInputStreamToString(InputStream inputStream) throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder sb = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}

			inputStream.close();
			return sb.toString();
		}

		@Override
		protected void onPostExecute(String jsonResponse) {
			Log.d("Response", "JSON Response: " + jsonResponse);

			if (jsonResponse != null) {
				// Login successful, save the token and ID
				saveTokenAndID(jsonResponse);

				Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show();

				// Navigate to the next screen (ProductsListFragment)
				Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_productsListFragment);
			} else {
				// Login failed
				Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show();
			}
		}

		private void saveTokenAndID(String jsonResponse) {
			try {
				// Parse the JSON response and extract the token
				JSONObject jsonObject = new JSONObject(jsonResponse);
				String token = jsonObject.getString("token");
				int ID = jsonObject.getInt("ID");
				Boolean is_buyer =jsonObject.getBoolean("is_buyer");


				// Save the token and ID in SharedPreferences
				SharedPreferences preferences = requireActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
				preferences.edit().putString("TOKEN", token).putInt("ID", ID).putBoolean("IS_BUYER",is_buyer).apply();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
