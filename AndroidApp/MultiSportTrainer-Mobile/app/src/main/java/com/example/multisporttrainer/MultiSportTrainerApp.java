package com.example.multisporttrainer;

import android.app.Application;

import com.example.multisporttrainer.api.RetrofitClient;

/**
 * Application entry point. Runs once when the process starts, before any
 * activity, so the persisted user session is restored into {@link SessionManager}
 * regardless of which activity Android brings up first (including after the OS
 * kills and restores the process directly into MainActivity).
 */
public class MultiSportTrainerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Give the API client an app context so its auth interceptor can read the token.
        RetrofitClient.init(this);
        SessionManager.loadFromPreferences(this);
    }
}
