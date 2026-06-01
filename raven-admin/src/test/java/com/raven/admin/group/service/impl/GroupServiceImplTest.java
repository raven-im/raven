package com.raven.admin.group.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.raven.admin.group.bean.param.GroupReqParam;
import com.raven.admin.group.validator.GroupValidator;
import com.raven.admin.group.validator.MemberNotInValidator;
import com.raven.common.result.Result;
import com.raven.common.result.ResultCode;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GroupServiceImplTest {

    @InjectMocks
    private GroupServiceImpl service;

    @Mock
    private GroupValidator groupValidator;

    @Mock
    private MemberNotInValidator memberNotValidator;

    @Test
    public void joinGroupRejectsEmptyMemberList() {
        GroupReqParam param = new GroupReqParam();
        param.setMembers(Collections.emptyList());

        Result result = service.joinGroup(param);

        assertEquals(ResultCode.COMMON_INVALID_PARAMETER.getCode(), result.getCode().intValue());
    }

    @Test
    public void quitGroupReportsMissingMember() {
        GroupReqParam param = new GroupReqParam();
        param.setGroupId("group-id");
        param.setMembers(Collections.singletonList("missing-member"));
        when(groupValidator.isValid("group-id")).thenReturn(true);
        when(memberNotValidator.isValid("group-id", param.getMembers())).thenReturn(false);
        when(memberNotValidator.errorCode()).thenReturn(ResultCode.GROUP_ERROR_MEMBER_NOT_IN);

        Result result = service.quitGroup(param);

        assertEquals(ResultCode.GROUP_ERROR_MEMBER_NOT_IN.getCode(), result.getCode().intValue());
    }
}
