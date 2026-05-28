package com.example.multisporttrainer.models;

import java.util.List;

public class SaveRouteResponse {

    private int routeId;
    private int userId;
    private int sessionId;
    private String routeType;
    private List<Integer> coneSequence;
    private String message;

    public int getRouteId() {
        return routeId;
    }

    public String getMessage() {
        return message;
    }
}