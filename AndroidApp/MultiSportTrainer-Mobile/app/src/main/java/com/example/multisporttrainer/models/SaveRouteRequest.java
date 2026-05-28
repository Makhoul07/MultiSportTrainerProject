package com.example.multisporttrainer.models;

import java.util.List;

public class SaveRouteRequest {

    private int userId;
    private int sessionId;
    private String routeType;
    private List<Integer> coneSequence;

    public SaveRouteRequest(
            int userId,
            int sessionId,
            String routeType,
            List<Integer> coneSequence
    ) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.routeType = routeType;
        this.coneSequence = coneSequence;
    }
}