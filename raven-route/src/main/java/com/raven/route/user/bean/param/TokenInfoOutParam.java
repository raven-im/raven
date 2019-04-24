package com.raven.route.user.bean.param;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenInfoOutParam {

    private String appKey;

    private String uid;

    private String token;
}
