package com.raven.admin.group.bean.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Table(name = "t_group")
public class GroupModel implements Serializable {

    private static final long serialVersionUID = 9129371215142758834L;

    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;

    @Column
    private String uid;

    @Column
    private String name;

    @Column(name = "portrait_url")
    private String portrait;

    @Column(name = "creator_uid")
    private String owner;

    @Column(name = "create_dt")
    private Date createDate;

    @Column(name = "update_dt")
    private Date updateDate;

    @Column
    private Integer status;

    @Column(name = "app_key")
    private String appKey;
}

