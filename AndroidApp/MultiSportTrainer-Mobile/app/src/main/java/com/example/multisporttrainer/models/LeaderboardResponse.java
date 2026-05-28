package com.example.multisporttrainer.models;

public class LeaderboardResponse {

    private int userId;
    private String fullName;
    private int bestScore;
    private double avgAccuracy;
    private int totalTrainings;

    public int getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public int getBestScore() {
        return bestScore;
    }

    public double getAvgAccuracy() {
        return avgAccuracy;
    }

    public int getTotalTrainings() {
        return totalTrainings;
    }
}