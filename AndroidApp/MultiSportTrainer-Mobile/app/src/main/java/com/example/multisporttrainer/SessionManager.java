package com.example.multisporttrainer;

import android.content.Context;

public class SessionManager {

    public static int loggedInUserId = -1;
    public static String loggedInFullName = "";
    public static String loggedInEmail = "";
    public static String loggedInRole = "";
    public static String loggedInSportFocus = "";

    public static boolean isLoggedIn() {
        return loggedInUserId != -1;
    }

    /** Restore the persisted session (if any) into the static fields. */
    public static void loadFromPreferences(Context context) {
        loggedInUserId = SessionPreferences.getUserId(context);
        loggedInFullName = SessionPreferences.getFullName(context);
        loggedInEmail = SessionPreferences.getEmail(context);
        loggedInRole = SessionPreferences.getRole(context);
        loggedInSportFocus = SessionPreferences.getSportFocus(context);
    }

    public static void clearSession() {
        loggedInUserId = -1;
        loggedInFullName = "";
        loggedInEmail = "";
        loggedInRole = "";
        loggedInSportFocus = "";
    }

    /** Clear both the in-memory session and the persisted SharedPreferences. */
    public static void clearSession(Context context) {
        clearSession();
        SessionPreferences.clear(context);
    }
}
