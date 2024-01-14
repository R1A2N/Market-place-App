package com.example.marketplace.ui.signup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.marketplace.R;
import com.example.marketplace.databinding.FragmentSignUpBinding;
import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class SignUpFragment extends Fragment {

	private FragmentSignUpBinding binding;

	private EditText editTextNameSignUp, editTextEmailSignUp, editTextPasswordSignUp;
	private Button buttonSignUp;
	private Spinner spinnerUserType;

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		SignUpViewModel signUpViewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

		binding = FragmentSignUpBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		// Add signup UI components
		editTextNameSignUp = root.findViewById(R.id.editTextNameSignUp);
		editTextEmailSignUp = root.findViewById(R.id.editTextEmailSignUp);
		editTextPasswordSignUp = root.findViewById(R.id.editTextPasswordSignUp);
		buttonSignUp = root.findViewById(R.id.buttonSignUp);
		spinnerUserType = root.findViewById(R.id.spinnerUserType);  // Initialize Spinner

		// Populate the Spinner with user types (Buyer/Seller)
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
				R.array.user_types, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerUserType.setAdapter(adapter);

		buttonSignUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Perform signup when the button is clicked
				performSignUp();
			}
		});

		return root;
	}

	private void performSignUp() {
		String name = editTextNameSignUp.getText().toString().trim();
		String email = editTextEmailSignUp.getText().toString().trim();
		String password = editTextPasswordSignUp.getText().toString().trim();
		boolean isBuyer = spinnerUserType.getSelectedItemPosition() == 0;  // 0 for Buyer, 1 for Seller

		// Validate input (you may add more validation as needed)

		SignUpRequest signUpRequest = new SignUpRequest(name, email, password, isBuyer);

		// Perform signup in a background thread using Retrofit
		RetrofitClient retrofitClient =  new RetrofitClient();
		SignUpService signUpService = retrofitClient.getClient().create(SignUpService.class);
		Call<Void> call = signUpService.signUp(signUpRequest);

		call.enqueue(new Callback<Void>() {
			@Override
			public void onResponse(Call<Void> call, Response<Void> response) {
				if (response.isSuccessful()) {
					// Signup successful
					Toast.makeText(requireContext(), "Signup successful", Toast.LENGTH_SHORT).show();
					// navigate to login screen
					Navigation.findNavController(requireView()).navigate(R.id.action_signupFragment_to_loginFragment);
				} else {
					// Signup failed
					Toast.makeText(requireContext(), "Signup failed", Toast.LENGTH_SHORT).show();
					Log.e("SignUpService", "Failed. Check server logs for details.");
				}
			}

			@Override
			public void onFailure(Call<Void> call, Throwable t) {
				// Handle failure
				Toast.makeText(requireContext(), "Signup failed", Toast.LENGTH_SHORT).show();
				Log.e("SignUpService", "Failed. Check server logs for details.", t);
			}
		});
	}

	public class SignUpRequest {
		@SerializedName("name")
		private String name;

		@SerializedName("email")
		private String email;

		@SerializedName("password")
		private String password;

		@SerializedName("is_buyer")
		private boolean isBuyer;

		public SignUpRequest(String name, String email, String password, boolean isBuyer) {
			this.name = name;
			this.email = email;
			this.password = password;
			this.isBuyer = isBuyer;
		}
	}

	public interface SignUpService {
		@POST("user/signup")
		Call<Void> signUp(@Body SignUpRequest signUpRequest);
	}

	public class RetrofitClient {
		private  Retrofit retrofit;
		private static final String BASE_URL = "http://192.168.11.128:8080/";

		public  Retrofit getClient() {
			if (retrofit == null) {
				retrofit = new Retrofit.Builder()
						.baseUrl(BASE_URL)
						.addConverterFactory(GsonConverterFactory.create())
						.build();
			}
			return retrofit;
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
