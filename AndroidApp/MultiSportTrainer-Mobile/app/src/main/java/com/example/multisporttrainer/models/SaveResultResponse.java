package com.example.multisporttrainer.models;

public class SaveResultResponse {

    private int resultId;
    private int sessionId;
    private int userId;
    private int score;
    private double accuracy;
    private int mistakes;
    private int durationSeconds;
    private String createdAt;
    private String message;

    public int getResultId() {
        return resultId;
    }

    public String getMessage() {
        return message;
    }
}
