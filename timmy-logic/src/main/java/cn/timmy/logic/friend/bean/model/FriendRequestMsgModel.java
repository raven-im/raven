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
@Table(name = "t_friend_request_msg")
public class FriendRequestMsgModel implements Serializable {

    private static final long serialVersionUID = -6304543919602149342L;

    @Id
    @GeneratedValue(generator = "JDBC")
    private Integer id;
    @Column
    private Integer request_id;
    @Column
    private String from_uid;
    @Column
    private String to_uid;
    @Column
    private String message;
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

    public Integer getRequest_id() {
        return request_id;
    }

    public void setRequest_id(Integer request_id) {
        this.request_id = request_id;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
}
