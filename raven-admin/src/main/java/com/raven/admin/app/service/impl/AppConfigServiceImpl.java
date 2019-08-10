package com.raven.admin.app.service.impl;

import com.raven.admin.app.bean.model.AppConfigModel;
import com.raven.admin.app.mapper.AppConfigMapper;
import com.raven.admin.app.service.AppConfigService;
import com.raven.common.utils.DateTimeUtils;
import com.raven.common.utils.UidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AppConfigServiceImpl implements AppConfigService {

    @Autowired
    private AppConfigMapper mapper;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public AppConfigModel createApp() {
        AppConfigModel model = new AppConfigModel();
        model.setUid(UidUtil.uuid());
        model.setSecret(UidUtil.uuid());
        model.setCreateDate(DateTimeUtils.currentUTC());
        model.setUpdateDate(DateTimeUtils.currentUTC());
        mapper.insert(model);
        return model;
    }

    @Override
    public AppConfigModel getApp(String uid) {
        AppConfigModel model = new AppConfigModel();
        model.setUid(uid);
        return mapper.selectOne(model);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void delApp(String uid) {
        AppConfigModel model = new AppConfigModel();
        model.setUid(uid);
        mapper.delete(model);
    }
}
