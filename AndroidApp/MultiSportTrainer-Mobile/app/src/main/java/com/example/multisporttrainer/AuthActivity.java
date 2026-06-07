package com.example.multisporttrainer;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class AuthActivity extends AppCompatActivity {

    /** When true, show the login screen directly and skip the biometric gate. */
    public static final String EXTRA_FORCE_LOGIN = "force_login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        boolean forceLogin = getIntent().getBooleanExtra(EXTRA_FORCE_LOGIN, false);

        // Session is restored on app start (MultiSportTrainerApp). If the user is
        // already logged in, gate entry behind a biometric prompt; otherwise (or
        // when a failed re-auth forces it) show the normal login screen.
        if (!forceLogin && SessionManager.isLoggedIn()) {
            authenticateWithBiometricsOrProceed();
        } else if (savedInstanceState == null) {
            loadFragment(new LoginFragment());
        }
    }

    private void authenticateWithBiometricsOrProceed() {
        // No biometric hardware, none enrolled, or temporarily unavailable: never
        // block a logged-in user — go straight into the app.
        if (!BiometricHelper.canAuthenticate(this)) {
            goToMainActivity();
            return;
        }

        BiometricHelper.authenticate(this, new BiometricHelper.Callback() {
            @Override
            public void onSucceeded() {
                goToMainActivity();
            }

            @Override
            public void onFailed() {
                // "Use password", cancel, or lockout: fall back to manual login.
                showLoginFallback();
            }
        });
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
