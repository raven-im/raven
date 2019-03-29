package com.tim.route.user.bean.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/12
 */
@Data
@Table(name = "t_user")
public class UserModel implements Serializable {

    private static final long serialVersionUID = -9163785798194244528L;
    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;
    @Column
    private String uid;
    @Column
    private String username;
    @Column
    private String password;
    @Column
    private Date create_dt;
    @Column
    private Date update_dt;
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
}
