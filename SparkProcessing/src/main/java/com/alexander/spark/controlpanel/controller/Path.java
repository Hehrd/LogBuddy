package com.alexander.spark.controlpanel.controller;

public enum Path {
    CONTROL_PLANE_PATH("/control-plane");

    private final String value;

    Path(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

