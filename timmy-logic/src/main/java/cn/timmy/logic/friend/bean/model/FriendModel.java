package cn.timmy.logic.friend.bean.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/26
 */
@Table(name = "t_friend")
public class FriendModel implements Serializable {

    private static final long serialVersionUID = -4586016257732250755L;
    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    @Column
    private String uid;
    @Column
    private String friend_uid;
    @Column
    private String alias;
    @Column
    private Date create_dt;
    @Column
    private Date update_dt;
    @Column
    private Integer state;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFriend_uid() {
        return friend_uid;
    }

    public void setFriend_uid(String friend_uid) {
        this.friend_uid = friend_uid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
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

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
