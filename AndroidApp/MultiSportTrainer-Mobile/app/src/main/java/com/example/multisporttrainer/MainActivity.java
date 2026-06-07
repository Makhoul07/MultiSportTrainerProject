package com.example.multisporttrainer;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    // Re-lock with biometrics when returning from the background. isInBackground
    // marks a real background trip (set in onStop); isAuthenticating guards the
    // biometric dialog's own lifecycle blips from re-arming the lock or looping.
    private boolean isInBackground = false;
    private boolean isAuthenticating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Restore the last tab the user was on (defaulting to Home), then attach
        // the listener so it doesn't re-fire for the initial selection.
        int lastTab = SessionPreferences.getLastTab(this, R.id.nav_home);
        if (!isKnownTab(lastTab)) {
            lastTab = R.id.nav_home;
        }

        loadFragment(fragmentForItem(lastTab));
        bottomNavigationView.setSelectedItemId(lastTab);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            SessionPreferences.saveLastTab(this, itemId);
            loadFragment(fragmentForItem(itemId));
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Came back from the background: require a fresh biometric unlock.
        if (isInBackground) {
            isInBackground = false;
            promptBiometricOnResume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Only treat this as a real background trip. Skip it while the biometric
        // dialog is up (its lifecycle blips) and on configuration changes (rotation).
        if (!isAuthenticating && !isChangingConfigurations()) {
            isInBackground = true;
        }
    }

    private void promptBiometricOnResume() {
        // No biometrics available/enrolled: don't lock the user out.
        if (!BiometricHelper.canAuthenticate(this)) {
            return;
        }

        isAuthenticating = true;

        BiometricHelper.authenticate(this, new BiometricHelper.Callback() {
            @Override
            public void onSucceeded() {
                isAuthenticating = false;
            }

            @Override
            public void onFailed() {
                isAuthenticating = false;

                // Boot the user out to the login screen (session left intact).
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                intent.putExtra(AuthActivity.EXTRA_FORCE_LOGIN, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private Fragment fragmentForItem(int itemId) {
        if (itemId == R.id.nav_training) {
            return new TrainingFragment();
        } else if (itemId == R.id.nav_stats) {
            return new StatisticsFragment();
        } else if (itemId == R.id.nav_history) {
            return new HistoryFragment();
        } else if (itemId == R.id.nav_profile) {
            return new ProfileFragment();
        }
        return new HomeFragment();
    }

    private boolean isKnownTab(int itemId) {
        return itemId == R.id.nav_home
                || itemId == R.id.nav_training
                || itemId == R.id.nav_stats
                || itemId == R.id.nav_history
                || itemId == R.id.nav_profile;
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void navigateTo(Fragment fragment, int navItemId) {
        bottomNavigationView.setSelectedItemId(navItemId);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}