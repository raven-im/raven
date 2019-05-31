package com.raven.common.param;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServerInfoOutParam {

    private String ip;

    private long port;
}
