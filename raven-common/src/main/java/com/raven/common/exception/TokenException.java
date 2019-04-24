package com.raven.common.exception;

/**
 * @author: bbpatience
 * @date: 2019/3/30
 * @description: TokenException
 **/
public class TokenException extends Exception {

    public TokenException(String message, Throwable e) {
        super(message, e);
    }

    public TokenException(Throwable e) {
        super(e);
    }

    public TokenException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
