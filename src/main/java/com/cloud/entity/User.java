package com.cloud.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author youzairichangdawang
 * @version 1.0
 * @Description 用户
 */
@AllArgsConstructor
@Data
@Builder
public class User implements Serializable {
    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 用户的openid
     */
    private String openId;
    /**
     * 文件仓库ID
     */
    private Integer fileStoreId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户邮箱
     */
    private String email;
    /**
     * 用户密码
     */
    private String password;
    /**
     * 注册时间
     */
    private Date registerTime;
    /**
     * 头像地址
     */
    private String imagePath;
    /**
     * 用户角色，0管理员，1普通用户
     */
    private Integer role;
}
