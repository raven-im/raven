package com.tim.common.model;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class ConverListInfo implements Serializable {

    private String id;

    private int type;

    private List<String> uidList;

    private String groupId;

    private int unCount; // 未读数

    MsgContent lastContent; // 最后一条消息

    public ConverListInfo setUnCount(int unCount) {
        this.unCount = unCount;
        return this;
    }

    public ConverListInfo setLastContent(MsgContent lastContent) {
        this.lastContent = lastContent;
        return this;
    }

    public ConverListInfo setId(String id) {
        this.id = id;
        return this;
    }

    public ConverListInfo setUidList(List<String> uidList) {
        this.uidList = uidList;
        return this;
    }

    public ConverListInfo setType(int type) {
        this.type = type;
        return this;
    }

    public ConverListInfo setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getId() {
        return id;
    }


    public int getType() {
        return type;
    }

    public List<String> getUidList() {
        return uidList;
    }

    public String getGroupId() {
        return groupId;
    }

    public int getUnCount() {
        return unCount;
    }

    public MsgContent getLastContent() {
        return lastContent;
    }
}
