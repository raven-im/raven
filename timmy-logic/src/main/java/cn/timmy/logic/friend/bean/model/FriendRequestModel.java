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
 * Date Created on 2018/6/28
 */
@Table(name = "t_friend_request")
public class FriendRequestModel implements Serializable {

    private static final long serialVersionUID = 1044775651005498965L;

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    @Column
    private String from_uid;
    @Column
    private String to_uid;
    @Column
    private Date create_dt;
    @Column
    private Date update_dt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getFrom_uid() {
        return from_uid;
    }

    public void setFrom_uid(String from_uid) {
        this.from_uid = from_uid;
    }

    public String getTo_uid() {
        return to_uid;
    }

    public void setTo_uid(String to_uid) {
        this.to_uid = to_uid;
    }
}
