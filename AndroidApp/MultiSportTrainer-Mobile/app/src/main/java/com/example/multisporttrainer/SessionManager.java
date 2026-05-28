package com.example.multisporttrainer;

public class SessionManager {

    public static int loggedInUserId = -1;
    public static String loggedInFullName = "";
    public static String loggedInEmail = "";
    public static String loggedInRole = "";
    public static String loggedInSportFocus = "";

    public static boolean isLoggedIn() {
        return loggedInUserId != -1;
    }

    public static void clearSession() {
        loggedInUserId = -1;
        loggedInFullName = "";
        loggedInEmail = "";
        loggedInRole = "";
        loggedInSportFocus = "";
    }
}