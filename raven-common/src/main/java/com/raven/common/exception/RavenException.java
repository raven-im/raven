package com.raven.common.exception;

public class RavenException extends RuntimeException {

    private static final long serialVersionUID = 7420589413273636443L;

    public RavenException(String errorMessage, Object... args) {
        super(String.format(errorMessage, args));
    }

    public RavenException(String errorMessage, Exception cause, Object... args) {
        super(String.format(errorMessage, args), cause);
    }

    public RavenException(Exception cause) {
        super(cause.getMessage(), cause);
    }
}
