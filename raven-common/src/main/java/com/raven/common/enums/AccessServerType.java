package com.raven.common.enums;

public enum AccessServerType {

    TCP("tcp"),

    WEBSOCKET("websocket"),

    INTERNAL("internal");

    private String type;

    AccessServerType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
