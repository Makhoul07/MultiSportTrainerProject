package com.example.multisporttrainer;

import java.util.ArrayList;
import java.util.List;

public class CurrentTrainingData {

    public static int sessionId = -1;

    public static String routeType = "Custom";
    // The user's original selection ("custom_route"/"generated_route"), preserved
    // even when an AI-generated route is sent to the Pi as routeType "Custom".
    public static String originalRouteType = "custom_route";
    public static String difficulty = "Medium";
    public static String trainingType = "Football Dribbling";

    public static int conesCount = 3;
    public static int rounds = 3;
    public static boolean distractionsEnabled = true;

    public static List<Integer> coneSequence = new ArrayList<>();

    public static boolean routeSaved = false;
    public static boolean resultSaved = false;

    public static int score = 870;
    public static double accuracy = 88.0;
    public static int mistakes = 2;
    public static int durationSeconds = 102;

    public static void clear() {
        sessionId = -1;

        routeType = "Custom";
        originalRouteType = "custom_route";
        difficulty = "Medium";
        trainingType = "Football Dribbling";

        conesCount = 4;
        rounds = 3;
        distractionsEnabled = true;

        coneSequence.clear();

        routeSaved = false;
        resultSaved = false;

        score = 870;
        accuracy = 88.0;
        mistakes = 2;
        durationSeconds = 102;
    }
}