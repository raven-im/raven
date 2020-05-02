package com.raven.common.dubbo;

/*
    For Access Service & Api Service, send their request to the IM System.

    Access --> Router
    Api ---> Router

    all incoming request.
 */
public interface MessageService {
    void singleMsgSend(String msg);
    void groupMsgSend(String msg);
}
