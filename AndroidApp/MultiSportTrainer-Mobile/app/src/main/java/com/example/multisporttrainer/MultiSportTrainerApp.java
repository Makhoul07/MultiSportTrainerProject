package com.example.multisporttrainer;

import android.app.Application;

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
        SessionManager.loadFromPreferences(this);
    }
}
