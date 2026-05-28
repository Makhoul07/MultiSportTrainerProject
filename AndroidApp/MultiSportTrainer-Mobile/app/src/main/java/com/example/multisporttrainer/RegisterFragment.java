package com.example.multisporttrainer;

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
import com.example.multisporttrainer.models.RegisterRequest;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private EditText fullNameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private MaterialButton registerButton;
    private TextView alreadyHaveAccountText;

    public RegisterFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        fullNameInput = view.findViewById(R.id.registerFullNameInput);
        emailInput = view.findViewById(R.id.registerEmailInput);
        passwordInput = view.findViewById(R.id.registerPasswordInput);
        confirmPasswordInput = view.findViewById(R.id.registerConfirmPasswordInput);
        registerButton = view.findViewById(R.id.registerButton);
        alreadyHaveAccountText = view.findViewById(R.id.alreadyHaveAccountText);

        registerButton.setOnClickListener(v -> registerUser());

        alreadyHaveAccountText.setOnClickListener(v -> {
            ((AuthActivity) requireActivity()).loadFragment(new LoginFragment());
        });

        return view;
    }

    private void registerUser() {
        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) {
            fullNameInput.setError("Full name is required");
            return;
        }

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

        if (!isValidPassword(password)) {
            passwordInput.setError("Weak password");

            Toast.makeText(
                    getContext(),
                    "Password must be at least 8 characters and include letters, numbers, a symbol, and one capital letter",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("Creating...");

        ApiService apiService = RetrofitClient
                .getInstance()
                .create(ApiService.class);

        RegisterRequest request = new RegisterRequest(fullName, email, password);

        apiService.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<AuthResponse> call,
                    @NonNull Response<AuthResponse> response
            ) {
                registerButton.setEnabled(true);
                registerButton.setText("CREATE ACCOUNT");

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    Toast.makeText(
                            getContext(),
                            authResponse.getMessage() + ". Please login.",
                            Toast.LENGTH_SHORT
                    ).show();

                    ((AuthActivity) requireActivity()).loadFragment(new LoginFragment());

                } else if (response.code() == 400) {
                    Toast.makeText(
                            getContext(),
                            "Registration failed. Email may already exist or password is weak.",
                            Toast.LENGTH_LONG
                    ).show();

                } else {
                    Toast.makeText(
                            getContext(),
                            "Registration failed.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<AuthResponse> call,
                    @NonNull Throwable t
            ) {
                registerButton.setEnabled(true);
                registerButton.setText("CREATE ACCOUNT");

                Toast.makeText(
                        getContext(),
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private boolean isValidPassword(String password) {
        boolean hasMinimumLength = password.length() >= 8;
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSymbol = password.matches(".*[^a-zA-Z0-9].*");
        boolean hasCapitalLetter = password.matches(".*[A-Z].*");

        return hasMinimumLength
                && hasLetter
                && hasNumber
                && hasSymbol
                && hasCapitalLetter;
    }
}