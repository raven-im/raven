package com.tim.route.admin.service.impl;

import com.tim.route.admin.bean.model.AppConfigModel;
import com.tim.route.admin.mapper.AppConfigMapper;
import com.tim.route.admin.service.AppConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class AppConfigServiceImpl implements AppConfigService {

    private AppConfigMapper mapper;

    @Autowired
    public AppConfigServiceImpl(AppConfigMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public AppConfigModel createApp() {
        AppConfigModel bean = new AppConfigModel();
        mapper.insert(bean);
        return bean;
    }

    @Override
    public AppConfigModel getApp(String key) {
        AppConfigModel bean = new AppConfigModel();
        bean.setKey(key);
        return mapper.selectOne(bean);
    }

    @Override
    public void delApp(String key) {
        AppConfigModel bean = new AppConfigModel();
        bean.setKey(key);
        mapper.delete(bean);
    }
}
