package com.example.marketplace.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.marketplace.R;

public class HomeFragment extends Fragment {

    private TextView textViewSignUpLink;
    private TextView textViewLoginLink;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // check if token exists in shared preferences and navigate accordingly
        // if it does exist go to products list else do nothing
        String token = getTokenFromSharedPreferences();
        Log.d("Token", "Token found: " + token);
        if (token != null) {
            NavHostFragment.findNavController(HomeFragment.this)
                    .navigate(R.id.action_nav_home_to_nav_products_list);
        }

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home, container, false);


        // Find the TextViews for sign-up and login links
        textViewSignUpLink = root.findViewById(R.id.textViewSignUpLink);
        textViewLoginLink = root.findViewById(R.id.textViewLoginLink);

        // Set up click listener for sign-up link
        textViewSignUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Sign up", "Sign up clicked");
                // Navigate to SignUpFragment using the action ID
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_nav_home_to_signUpFragment);
            }
        });

        // Set up click listener for login link
        textViewLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Navigate to LoginFragment using the action ID
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_nav_home_to_loginFragment);
            }
        });

        return root;
    }
    private String getTokenFromSharedPreferences() {
        SharedPreferences preferences = getActivity().getSharedPreferences("MY_APP", Context.MODE_PRIVATE);
        return preferences.getString("TOKEN", null);
    }
}
