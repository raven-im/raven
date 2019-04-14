package com.tim.group.restful.bean.param;

import com.tim.group.restful.bean.model.GroupMemberModel;
import com.tim.group.restful.bean.model.GroupModel;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupOutParam {

    private String groupId;
    private String converId;
    private GroupDetailParam details;
    private Date time;

    public GroupOutParam(GroupModel model) {
        this.groupId = model.getUid();
        this.converId = model.getConverId();
        this.time = model.getCreateDate();
    }

    public GroupOutParam(GroupModel model, List<GroupMemberModel> memberModels) {
        this.groupId = model.getUid();
        this.converId = model.getConverId();
        this.time = model.getUpdateDate();
        List<String> members = memberModels.stream()
            .map(x -> x.getMemberUid())
            .collect(Collectors.toList());
        this.details = new GroupDetailParam(model.getName(), model.getPortrait(),
            members, model.getUpdateDate());
    }
}
