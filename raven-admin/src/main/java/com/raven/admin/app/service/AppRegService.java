package com.raven.admin.app.service;


import com.raven.admin.app.bean.model.AppRegModel;

public interface AppRegService {

    AppRegModel createApp();

    AppRegModel getApp(String key);

    void delApp(String key);
}
