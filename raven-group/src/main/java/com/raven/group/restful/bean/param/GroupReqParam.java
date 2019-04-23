package com.raven.group.restful.bean.param;

import java.util.List;
import lombok.Data;

/**
 * @author: bbpatience
 * @date: 2019/3/30
 * @description: GroupReqParam
 **/
@Data
public class GroupReqParam {
    private String groupId;
    private String name;
    private String portrait;
    private List<String> members;
}
