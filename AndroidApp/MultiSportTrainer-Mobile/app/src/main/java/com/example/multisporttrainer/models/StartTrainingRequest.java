package com.example.multisporttrainer.models;

public class StartTrainingRequest {

    private int userId;
    private String routeType;
    private String difficulty;
    private String trainingType;
    private int conesCount;
    private int rounds;
    private boolean distractionsEnabled;

    public StartTrainingRequest(
            int userId,
            String routeType,
            String difficulty,
            String trainingType,
            int conesCount,
            int rounds,
            boolean distractionsEnabled
    ) {
        this.userId = userId;
        this.routeType = routeType;
        this.difficulty = difficulty;
        this.trainingType = trainingType;
        this.conesCount = conesCount;
        this.rounds = rounds;
        this.distractionsEnabled = distractionsEnabled;
    }
}