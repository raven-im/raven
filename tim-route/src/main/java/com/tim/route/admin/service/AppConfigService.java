package com.tim.route.admin.service;


import com.tim.route.admin.bean.model.AppConfigModel;

public interface AppConfigService {

    AppConfigModel createApp();
    AppConfigModel getApp(String key);
    void delApp(String key);
}
