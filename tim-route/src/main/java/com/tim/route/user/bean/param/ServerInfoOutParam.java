package com.tim.route.user.bean.param;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServerInfoOutParam {

    private String appKey;

    private String uid;

    private String ip;

    private long port;
}
