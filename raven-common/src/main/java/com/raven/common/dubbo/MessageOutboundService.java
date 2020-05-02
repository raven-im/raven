package com.raven.common.dubbo;

/*
    Route the message to the specific access server.  consistency hash

    Router --> Access

    all outbound request.
 */
public interface MessageOutboundService {
    void outboundMsgSend(String msg);
}
