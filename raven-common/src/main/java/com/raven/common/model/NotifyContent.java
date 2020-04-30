package com.raven.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyContent implements Serializable {

    private long id;
    private int type;
    private String content;
    private String fromUid;
    private String targetUid;
    private long time;

}
