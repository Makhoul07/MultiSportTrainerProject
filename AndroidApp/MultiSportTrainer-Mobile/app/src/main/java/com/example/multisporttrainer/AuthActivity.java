package com.example.multisporttrainer;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.concurrent.Executor;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Session is restored on app start (MultiSportTrainerApp). If the user is
        // already logged in, gate entry behind a biometric prompt; otherwise show
        // the normal login screen.
        if (SessionManager.isLoggedIn()) {
            authenticateWithBiometricsOrProceed();
        } else if (savedInstanceState == null) {
            loadFragment(new LoginFragment());
        }
    }

    private void authenticateWithBiometricsOrProceed() {
        BiometricManager biometricManager = BiometricManager.from(this);
        int status = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        );

        // No biometric hardware, none enrolled, or temporarily unavailable: never
        // block a logged-in user — go straight into the app.
        if (status != BiometricManager.BIOMETRIC_SUCCESS) {
            goToMainActivity();
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt biometricPrompt = new BiometricPrompt(
                this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result
                    ) {
                        goToMainActivity();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        // "Use password" button, cancel, or lockout: fall back to
                        // manual password login.
                        showLoginFallback();
                    }

                    // onAuthenticationFailed (a single non-matching scan) is left to
                    // the system prompt, which stays open for the user to retry.
                }
        );

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock MultiSport Trainer")
                .setSubtitle("Confirm your fingerprint to continue")
                .setNegativeButtonText("Use password")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void goToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showLoginFallback() {
        loadFragment(new LoginFragment());
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_fragment_container, fragment)
                .commit();
    }
}
