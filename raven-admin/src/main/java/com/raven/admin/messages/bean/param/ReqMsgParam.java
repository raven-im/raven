package com.raven.admin.messages.bean.param;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: bbpatience
 * @date: 2019/8/15
 * @description: ReqMsgParam
 **/
@Data
@AllArgsConstructor
public class ReqMsgParam {
    private String fromUid;
    private String targetUid;
    private String content;
}
