package com.raven.admin.appconfig.service.impl;

import com.raven.admin.appconfig.bean.model.AppConfigModel;
import com.raven.admin.appconfig.mapper.AppConfigMapper;
import com.raven.admin.appconfig.service.AppConfigService;
import com.raven.common.utils.DateTimeUtils;
import com.raven.common.utils.UidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Throwable.class)
@Slf4j
public class AppConfigServiceImpl implements AppConfigService {

    @Autowired
    private AppConfigMapper mapper;

    @Override
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
    @Cacheable(cacheNames = "UnboundedCache", key = "methodName+'_'+#uid")
    public AppConfigModel getApp(String uid) {
        AppConfigModel model = new AppConfigModel();
        model.setUid(uid);
        return mapper.selectOne(model);
    }

    @Override
    public void delApp(String uid) {
        AppConfigModel model = new AppConfigModel();
        model.setUid(uid);
        mapper.delete(model);
    }
}
