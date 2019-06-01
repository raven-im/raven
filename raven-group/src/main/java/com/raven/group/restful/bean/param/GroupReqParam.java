package com.raven.group.restful.bean.param;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: bbpatience
 * @date: 2019/3/30
 * @description: GroupReqParam
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupReqParam {

    private String groupId;

    private String name;

    private String portrait;

    private List<String> members;
}
