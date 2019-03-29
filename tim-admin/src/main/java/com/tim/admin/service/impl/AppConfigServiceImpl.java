package com.tim.admin.service.impl;

import com.tim.common.utils.DateTimeUtils;
import com.tim.common.utils.UidUtil;
import com.tim.admin.bean.model.AppConfigModel;
import com.tim.admin.mapper.AppConfigMapper;
import com.tim.admin.service.AppConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Throwable.class)
@Slf4j
public class AppConfigServiceImpl implements AppConfigService {

    private AppConfigMapper mapper;

    @Autowired
    public AppConfigServiceImpl(AppConfigMapper mapper) {
        this.mapper = mapper;
    }

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
