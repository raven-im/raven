package cn.timmy.logic.user.bean;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/12
 */
@Table(name = "t_user")
public class UserModel implements Serializable {

    private static final long serialVersionUID = -9163785798194244528L;
    @Id
    private Long uid;
    @Column
    private String username;
    @Column
    private String password;
    @Column
    private Date create_dt;
    @Column
    private Date update_dt;
    @Column
    private String pwdsalt;
    @Column
    private String mobile;
    @Column
    private String email;
    @Column
    private String name;
    @Column
    private String portrait_url;
    @Column
    private Integer status;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreate_dt() {
        return create_dt;
    }

    public void setCreate_dt(Date create_dt) {
        this.create_dt = create_dt;
    }

    public Date getUpdate_dt() {
        return update_dt;
    }

    public void setUpdate_dt(Date update_dt) {
        this.update_dt = update_dt;
    }

    public String getPwdsalt() {
        return pwdsalt;
    }

    public void setPwdsalt(String pwdsalt) {
        this.pwdsalt = pwdsalt;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPortrait_url() {
        return portrait_url;
    }

    public void setPortrait_url(String portrait_url) {
        this.portrait_url = portrait_url;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
