package com.tim.common.loadbalance;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Server {

    private String ip;

    private int port;

    public String getUrl() {
        return ip + ":" + port;
    }

}
