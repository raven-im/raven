package com.tim.common.loadbalance;


import static com.tim.common.utils.Constants.DEFAULT_SEPARATES_SIGN;

import java.util.Objects;
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

    public Server(String address) {
        String[] array = address.split(DEFAULT_SEPARATES_SIGN);
        this.ip = array[0];
        this.port = Integer.parseInt(array[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Server server = (Server) o;
        return port == server.port &&
            Objects.equals(ip, server.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }

    @Override
    public String toString() {
        return "Server{" +
            "ip='" + ip + '\'' +
            ", port=" + port +
            '}';
    }
}
