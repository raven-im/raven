package com.raven.admin.group.bean.param;

import com.raven.admin.group.bean.model.GroupMemberModel;
import com.raven.admin.group.bean.model.GroupModel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    public GroupOutParam(GroupModel model, List<GroupMemberModel> memberModels) {
        this.groupId = model.getUid();
        this.time = model.getUpdateDate();
        List<String> members = memberModels.stream()
                .map(x -> x.getMemberUid())
                .collect(Collectors.toList());
        this.details = new GroupDetailParam(model.getName(), model.getPortrait(),
                members, model.getUpdateDate());
    }
}
