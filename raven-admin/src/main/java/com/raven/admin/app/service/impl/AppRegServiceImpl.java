package com.raven.admin.app.service.impl;

import com.raven.admin.app.bean.model.AppRegModel;
import com.raven.admin.app.mapper.AppRegMapper;
import com.raven.admin.app.service.AppRegService;
import com.raven.common.utils.DateTimeUtils;
import com.raven.common.utils.UidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AppRegServiceImpl implements AppRegService {

    @Autowired
    private AppRegMapper mapper;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public AppRegModel createApp() {
        AppRegModel model = new AppRegModel();
        model.setUid(UidUtil.uuid());
        model.setSecret(UidUtil.uuid());
        model.setCreateDate(DateTimeUtils.currentUTC());
        model.setUpdateDate(DateTimeUtils.currentUTC());
        mapper.insert(model);
        return model;
    }

    @Override
    public AppRegModel getApp(String uid) {
        AppRegModel model = new AppRegModel();
        model.setUid(uid);
        return mapper.selectOne(model);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void delApp(String uid) {
        AppRegModel model = new AppRegModel();
        model.setUid(uid);
        mapper.delete(model);
    }
}
