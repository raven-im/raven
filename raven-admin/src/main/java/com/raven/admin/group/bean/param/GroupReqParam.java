package com.raven.admin.group.bean.param;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupReqParam {

    private String groupId;

    private String name;

    private String portrait;

    private List<String> members;
}
