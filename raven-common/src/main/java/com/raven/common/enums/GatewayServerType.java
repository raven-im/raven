package com.raven.common.enums;

public enum GatewayServerType {

    TCP("tcp"),

    WEBSOCKET("websocket");

    private String type;

    GatewayServerType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
