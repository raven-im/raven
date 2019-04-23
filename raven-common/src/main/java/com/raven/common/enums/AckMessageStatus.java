package com.raven.common.enums;

public enum AckMessageStatus {

    SUCCESS(1),

    REFUSE(2);

    private int status;

    private AckMessageStatus(int state) {

    }

    public int getStatus() {
        return status;
    }
}
