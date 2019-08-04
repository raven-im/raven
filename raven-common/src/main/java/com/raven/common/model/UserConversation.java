package com.raven.common.model;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConversation implements Serializable {

    private String id;

    private int type;

    private List<String> uidList;

    private String groupId;

    private long readMsgId; // 已读消息ID

    MsgContent lastContent; // 最后一条消息

}
