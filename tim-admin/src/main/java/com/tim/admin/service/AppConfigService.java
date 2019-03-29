package com.tim.admin.service;


import com.tim.admin.bean.model.AppConfigModel;

public interface AppConfigService {

    AppConfigModel createApp();
    AppConfigModel getApp(String key);
    void delApp(String key);
}
