package com.tim.common.model;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * @author: bbpatience
 * @date: 2019/4/9
 * @description: ConversationModel
 **/
@Data
public class ConverInfo implements Serializable {


    private String id;

    private int type;

    private List<String> uidList;

    private String groupId;

    public ConverInfo setId(String id) {
        this.id = id;
        return this;
    }

    public ConverInfo setUidList(List<String> uidList) {
        this.uidList = uidList;
        return this;
    }

    public ConverInfo setType(int type) {
        this.type = type;
        return this;
    }

    public ConverInfo setGroupId(String groupId) {
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


}
