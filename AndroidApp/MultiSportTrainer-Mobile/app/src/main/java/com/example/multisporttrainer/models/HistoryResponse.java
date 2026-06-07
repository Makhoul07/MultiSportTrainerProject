package com.example.multisporttrainer.models;

public class HistoryResponse {

    private int resultId;
    private int sessionId;
    private String trainingType;
    private String routeType;
    private String difficulty;
    private int conesCount;
    private int rounds;
    private int score;
    private double accuracy;
    private int mistakes;
    private int durationSeconds;
    private String createdAt;

    public int getResultId() {
        return resultId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public String getTrainingType() {
        return trainingType;
    }

    public String getRouteType() {
        return routeType;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getConesCount() {
        return conesCount;
    }

    public int getRounds() {
        return rounds;
    }

    public int getScore() {
        return score;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public int getMistakes() {
        return mistakes;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}