package com.example.multisporttrainer.models;

public class SaveResultRequest {

    private int sessionId;
    private int userId;
    private int score;
    private double accuracy;
    private int mistakes;
    private int durationSeconds;

    public SaveResultRequest(
            int sessionId,
            int userId,
            int score,
            double accuracy,
            int mistakes,
            int durationSeconds
    ) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.score = score;
        this.accuracy = accuracy;
        this.mistakes = mistakes;
        this.durationSeconds = durationSeconds;
    }
}
