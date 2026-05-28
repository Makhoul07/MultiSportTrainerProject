package com.example.multisporttrainer.models;

public class SaveResultRequest {

    private int sessionId;
    private int userId;
    private int score;
    private double accuracy;
    private int mistakes;
    private int durationSeconds;
    private double averageReactionSeconds;
    private double bestReactionSeconds;

    public SaveResultRequest(
            int sessionId,
            int userId,
            int score,
            double accuracy,
            int mistakes,
            int durationSeconds,
            double averageReactionSeconds,
            double bestReactionSeconds
    ) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.score = score;
        this.accuracy = accuracy;
        this.mistakes = mistakes;
        this.durationSeconds = durationSeconds;
        this.averageReactionSeconds = averageReactionSeconds;
        this.bestReactionSeconds = bestReactionSeconds;
    }
}