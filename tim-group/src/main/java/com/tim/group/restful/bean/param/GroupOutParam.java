package com.tim.group.restful.bean.param;

import com.tim.group.restful.bean.model.GroupModel;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupOutParam {

    private String groupId;
    private GroupDetailParam details;
    private Date time;

    public GroupOutParam(GroupModel model) {
        this.groupId = model.getUid();
        this.time = model.getCreateDate();
    }
}
