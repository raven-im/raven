package com.raven.common.exception;

/**
 * @author: bbpatience
 * @date: 2019/3/30
 * @description: TokenException
 **/
public class TokenException extends Exception {

    private TokenExceptionType type;

    public TokenException(String message, Throwable e) {
        super(message, e);
    }

    public TokenException(Throwable e) {
        super(e);
    }

    public TokenException(String message, TokenExceptionType type) {
        super(message);
        this.type = type;
    }

    public TokenExceptionType getType() {
        return type;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
