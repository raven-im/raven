package com.tim.client.group.bean;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: bbpatience
 * @date: 2019/3/30
 * @description: GroupReqParam
 **/
@Data
@AllArgsConstructor
public class GroupReqParam {
    private String name;
    private String portrait;
    private List<String> members;
}
