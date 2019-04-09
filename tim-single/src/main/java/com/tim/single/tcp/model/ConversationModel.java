package com.tim.single.tcp.model;

import com.tim.common.protos.Common.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: bbpatience
 * @date: 2019/4/9
 * @description: ConversationModel
 **/
@Data
@AllArgsConstructor
public class ConversationModel {

    private String id;
    private ConversationType type;
    private String name;
    private long lastUpdateTime;
    private String lastMsgContent;
}
