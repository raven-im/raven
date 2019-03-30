package com.tim.route.user.bean.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

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

    @Column
	private Date create_dt;

    @Column
    private Date update_dt;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Date getCreateDate() {
        return create_dt;
    }

    public void setCreateDate(Date create_dt) {
        this.create_dt = create_dt;
    }

    public Date getUpdateDate() {
        return update_dt;
    }

    public void setUpdateDate(Date update_dt) {
        this.update_dt = update_dt;
    }
}

