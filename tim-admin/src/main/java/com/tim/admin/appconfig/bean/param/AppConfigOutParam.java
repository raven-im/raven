package com.tim.admin.appconfig.bean.param;

import com.tim.admin.appconfig.bean.model.AppConfigModel;
import lombok.Data;

@Data
public class AppConfigOutParam {

    private String key;
    private String secret;

    public AppConfigOutParam(AppConfigModel model) {
        this.key = model.getUid();
        this.secret = model.getSecret();
    }
}
