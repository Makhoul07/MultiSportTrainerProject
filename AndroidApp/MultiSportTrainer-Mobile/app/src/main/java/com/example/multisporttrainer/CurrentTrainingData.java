package com.example.multisporttrainer;

import java.util.ArrayList;
import java.util.List;

public class CurrentTrainingData {

    public static int sessionId = -1;

    public static String routeType = "Custom";
    public static String difficulty = "Medium";
    public static String trainingType = "Football Dribbling";

    public static int conesCount = 4;
    public static int rounds = 3;
    public static boolean distractionsEnabled = true;

    public static List<Integer> coneSequence = new ArrayList<>();

    public static boolean routeSaved = false;
    public static boolean resultSaved = false;

    public static int score = 870;
    public static double accuracy = 88.0;
    public static int mistakes = 2;
    public static int durationSeconds = 102;
    public static double averageReactionSeconds = 1.4;
    public static double bestReactionSeconds = 0.9;

    public static void clear() {
        sessionId = -1;

        routeType = "Custom";
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
        averageReactionSeconds = 1.4;
        bestReactionSeconds = 0.9;
    }
}