package com.example.multisporttrainer;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Session is restored on app start (MultiSportTrainerApp). If the user is
        // already logged in, skip the auth screens entirely.
        if (SessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            loadFragment(new LoginFragment());
        }
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_fragment_container, fragment)
                .commit();
    }
}