package com.example.multisporttrainer.models;

public class StatisticsResponse {

    private int totalTrainings;
    private int bestScore;
    private double avgScore;
    private double avgAccuracy;
    private int totalTimeSeconds;

    public int getTotalTrainings() {
        return totalTrainings;
    }

    public int getBestScore() {
        return bestScore;
    }

    public double getAvgScore() {
        return avgScore;
    }

    public double getAvgAccuracy() {
        return avgAccuracy;
    }

    public int getTotalTimeSeconds() {
        return totalTimeSeconds;
    }
}
