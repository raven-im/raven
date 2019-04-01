package com.tim.admin.appconfig.bean.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Table(name="t_app_config")
public class AppConfigModel implements Serializable {

    private static final long serialVersionUID = 9129371215147758834L;

    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;

    @Column
    private String uid;

	@Column
    private String secret;

    @Column(name = "create_dt")
	private Date createDate;

    @Column(name = "update_dt")
    private Date updateDate;

}

