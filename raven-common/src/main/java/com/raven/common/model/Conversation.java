package com.raven.common.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class Conversation implements Serializable {

    private String appKey;

    private String id;

    private int type;

    private List<String> uidList;

    private long timestamp;
}
