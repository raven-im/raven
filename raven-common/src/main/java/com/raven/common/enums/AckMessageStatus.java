package com.raven.common.enums;

public enum AckMessageStatus {

    SUCCESS(1),

    REFUSE(2);

    private int status;

    AckMessageStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
