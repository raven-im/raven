package com.raven.admin.group.bean.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Table(name="t_group_member")
public class GroupMemberModel implements Serializable {

    private static final long serialVersionUID = 9121371215142758834L;

    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;

    @Column(name = "guid")
    private String groupId;

	@Column(name = "uid")
    private String memberUid;

    @Column(name = "create_dt")
    private Date createDate;

    @Column(name = "update_dt")
    private Date updateDate;

    @Column
    private Integer status;
}

