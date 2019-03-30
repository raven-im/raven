package com.tim.admin.appconfig.bean.param;


import com.tim.admin.appconfig.bean.model.AppConfigModel;

public class AppConfigOutParam {

    private String key;

    private String secret;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public AppConfigOutParam(AppConfigModel model) {
        this.key = model.getUid();
        this.secret = model.getSecret();
    }
}
