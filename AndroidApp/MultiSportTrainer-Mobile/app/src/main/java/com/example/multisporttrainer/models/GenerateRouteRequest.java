package com.example.multisporttrainer.models;

public class GenerateRouteRequest {

    private int userId;
    private String difficulty;

    public GenerateRouteRequest(int userId, String difficulty) {
        this.userId = userId;
        this.difficulty = difficulty;
    }
}
