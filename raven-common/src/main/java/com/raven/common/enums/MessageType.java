package com.raven.common.enums;

public enum MessageType {

    TEXT("AH:TXT"),
    IMAGE("AH:IMG"),
    VOICE("AH:VOICE"),
    VIDEO("AH:VIDEO");

    private String type;

    MessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
