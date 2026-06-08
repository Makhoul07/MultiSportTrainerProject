package com.example.multisporttrainer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Persists the logged-in user session to SharedPreferences so it survives the
 * app being closed/reopened (and process death). This is the storage layer;
 * {@link SessionManager} mirrors these values into static fields for the rest
 * of the app to read.
 */
public final class SessionPreferences {

    private static final String PREFS_NAME = "mst_session";

    private static final String KEY_USER_ID = "userId";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_SPORT_FOCUS = "sportFocus";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_LAST_TAB = "lastTab";

    private SessionPreferences() {
        // static helper only
    }

    private static SharedPreferences prefs(Context context) {
        return context
                .getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void save(
            Context context,
            int userId,
            String fullName,
            String email,
            String role,
            String sportFocus
    ) {
        prefs(context)
                .edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_FULL_NAME, fullName)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .putString(KEY_SPORT_FOCUS, sportFocus)
                .apply();
    }

    /** True when a userId has been persisted (i.e. a session exists). */
    public static boolean hasSession(Context context) {
        return getUserId(context) != -1;
    }

    public static void clear(Context context) {
        prefs(context).edit().clear().apply();
    }

    public static int getUserId(Context context) {
        return prefs(context).getInt(KEY_USER_ID, -1);
    }

    public static String getFullName(Context context) {
        return prefs(context).getString(KEY_FULL_NAME, "");
    }

    public static String getEmail(Context context) {
        return prefs(context).getString(KEY_EMAIL, "");
    }

    public static String getRole(Context context) {
        return prefs(context).getString(KEY_ROLE, "");
    }

    public static String getSportFocus(Context context) {
        return prefs(context).getString(KEY_SPORT_FOCUS, "");
    }

    /** Persist the JWT bearer token used to authenticate API requests. */
    public static void saveToken(Context context, String token) {
        prefs(context).edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getToken(Context context) {
        return prefs(context).getString(KEY_TOKEN, "");
    }

    public static void clearToken(Context context) {
        prefs(context).edit().remove(KEY_TOKEN).apply();
    }

    /** Remember the bottom-nav tab the user is on, to restore it on next launch. */
    public static void saveLastTab(Context context, int itemId) {
        prefs(context).edit().putInt(KEY_LAST_TAB, itemId).apply();
    }

    public static int getLastTab(Context context, int defaultItemId) {
        return prefs(context).getInt(KEY_LAST_TAB, defaultItemId);
    }
}
