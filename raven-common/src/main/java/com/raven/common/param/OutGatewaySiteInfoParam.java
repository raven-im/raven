package com.raven.common.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutGatewaySiteInfoParam {

    private String ip;

    private int port;

    @Override
    public String toString() {
        return "ServerInfoOutParam{" +
            "ip='" + ip + '\'' +
            ", port=" + port +
            '}';
    }
}
