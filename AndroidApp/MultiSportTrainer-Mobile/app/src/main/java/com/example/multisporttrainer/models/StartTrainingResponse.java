package com.example.multisporttrainer.models;

public class StartTrainingResponse {

    private int sessionId;
    private int userId;
    private String routeType;
    private String difficulty;
    private String trainingType;
    private int conesCount;
    private int rounds;
    private boolean distractionsEnabled;
    private String startedAt;
    private String status;
    private String message;

    public int getSessionId() {
        return sessionId;
    }

    public String getMessage() {
        return message;
    }
}