package auth.utils;

import auth.handler.AuthServerHandler;
import com.google.protobuf.Internal;
import io.netty.buffer.ByteBuf;
import protobuf.protos.Auth;
import protobuf.utils.MessageProtoNum;
import protobuf.utils.Utils;

/**
 * Created by win7 on 2016/3/3.
 */
public class RouteUtil {

    public static void sendResponse(int code, String desc, long netId, String userId) {
        Auth.Response.Builder sb = Auth.Response.newBuilder();
        sb.setCode(code);
        sb.setDesc(desc);
        ByteBuf byteBuf = Utils
            .pack2Server(sb.build(), MessageProtoNum.RESPONSE, netId, Internal.Dest.Client,
                userId);
        AuthServerHandler.getGateAuthConnection().writeAndFlush(byteBuf);
    }
}
