package com.raven.admin.appconfig.service;


import com.raven.admin.appconfig.bean.model.AppConfigModel;

public interface AppConfigService {

    AppConfigModel createApp();

    AppConfigModel getApp(String key);

    void delApp(String key);
}
