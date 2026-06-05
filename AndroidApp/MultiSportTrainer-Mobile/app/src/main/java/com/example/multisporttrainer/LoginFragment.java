package com.example.multisporttrainer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.multisporttrainer.api.ApiService;
import com.example.multisporttrainer.api.RetrofitClient;
import com.example.multisporttrainer.models.AuthResponse;
import com.example.multisporttrainer.models.LoginRequest;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private EditText emailInput;
    private EditText passwordInput;
    private MaterialButton loginButton;
    private TextView createAccountText;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        emailInput = view.findViewById(R.id.loginEmailInput);
        passwordInput = view.findViewById(R.id.loginPasswordInput);
        loginButton = view.findViewById(R.id.loginButton);
        createAccountText = view.findViewById(R.id.createAccountText);

        loginButton.setOnClickListener(v -> loginUser());

        createAccountText.setOnClickListener(v -> {
            ((AuthActivity) requireActivity()).loadFragment(new RegisterFragment());
        });

        //test
        //Toast.makeText(getContext(), "LoginFragment loaded", Toast.LENGTH_LONG).show();

        return view;
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        if (password.length() < 4) {
            passwordInput.setError("Password must be at least 4 characters");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        ApiService apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);

        LoginRequest request = new LoginRequest(email, password);

        apiService.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<AuthResponse> call,
                    @NonNull Response<AuthResponse> response
            ) {
                loginButton.setEnabled(true);
                loginButton.setText("LOGIN");

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    ProfileData.fullName = authResponse.getFullName();
                    ProfileData.email = authResponse.getEmail();
                    ProfileData.role = authResponse.getRole();
                    SessionManager.loggedInUserId = authResponse.getUserId();
                    SessionManager.loggedInFullName = authResponse.getFullName();
                    SessionManager.loggedInEmail = authResponse.getEmail();
                    SessionManager.loggedInRole = authResponse.getRole();
                    SessionManager.loggedInSportFocus = authResponse.getSportFocus();

                    Toast.makeText(
                            getContext(),
                            authResponse.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    startActivity(intent);
                    requireActivity().finish();

                } else if (response.code() == 401) {
                    Toast.makeText(
                            getContext(),
                            "Invalid email or password",
                            Toast.LENGTH_SHORT
                    ).show();

                } else {
                    Toast.makeText(
                            getContext(),
                            "Login failed. Check your information.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<AuthResponse> call,
                    @NonNull Throwable t
            ) {
                loginButton.setEnabled(true);
                loginButton.setText("LOGIN");

                Toast.makeText(
                        getContext(),
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}