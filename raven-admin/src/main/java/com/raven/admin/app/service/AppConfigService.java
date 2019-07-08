package com.raven.admin.app.service;


import com.raven.admin.app.bean.model.AppConfigModel;

public interface AppConfigService {

    AppConfigModel createApp();

    AppConfigModel getApp(String key);

    void delApp(String key);
}
