package com.tim.admin.appconfig.service;


import com.tim.admin.appconfig.bean.model.AppConfigModel;

public interface AppConfigService {

    AppConfigModel createApp();
    AppConfigModel getApp(String key);
    void delApp(String key);
}
