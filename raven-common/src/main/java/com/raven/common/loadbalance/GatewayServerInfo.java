package com.raven.common.loadbalance;


import com.raven.common.utils.JsonHelper;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatewayServerInfo {

    private String ip;

    private int tcpPort;

    private int wsPort;

    private int internalPort;

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
            internalPort == that.internalPort &&
            Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, tcpPort, wsPort, internalPort);
    }

    @Override
    public String toString() {
        return JsonHelper.toJsonString(this);
    }
}
