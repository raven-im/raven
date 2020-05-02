package com.raven.common.loadbalance;


import com.raven.common.utils.JsonHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatewayServerInfo {

    private String ip;

    private int tcpPort;

    private int wsPort;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GatewayServerInfo that = (GatewayServerInfo) o;
        return tcpPort == that.tcpPort &&
                wsPort == that.wsPort &&
                Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, tcpPort, wsPort);
    }

    @Override
    public String toString() {
        return JsonHelper.toJsonString(this);
    }
}
