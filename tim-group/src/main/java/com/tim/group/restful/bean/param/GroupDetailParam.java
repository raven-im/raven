package com.tim.group.restful.bean.param;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupDetailParam {

    private String name;
    private String portrait;
    private List<String> members;
    private Date time;
}
