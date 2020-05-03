package com.raven.common.dubbo;

public interface AccessService {
    /*
    Route the message to the specific access server.  consistency hash

    Router --> Access

    all outbound request.

    use uid as key for consistent hash.  1st parameter in method for dubbo.
    */
    void outboundMsgSend(String uid, String msg);

    /*
    Assign a new Access to clients. return the specific access server (consistent hash)

    API --> Access

    use uid as key for consistent hash.  1st parameter in method for dubbo.

    Should be same with "outboundMsgSend" method.

    1. get Access node.     consistent hash
    2. routing msg to that access.  consistent hash

    consistent hash (all gateway server) ,  so they can route to the same access node.
 */
    String hashRouting(String uid);
}
