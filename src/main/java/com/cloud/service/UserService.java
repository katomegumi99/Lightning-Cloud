package com.cloud.service;

import com.cloud.entity.User;
import com.cloud.entity.UserDTO;

import java.awt.print.PrinterJob;
import java.util.List;

public interface UserService {

    // 获取所有用户数量
    Integer getUsersCount();

    // 获取全部的用户
    List<UserDTO> getUsers();

    User queryById(Integer userId);

    boolean deleteById(Integer userId);

    boolean insert(User user);

    boolean update(User user);

    // 通过邮箱获取用户信息
    User getUserByEmail(String email);

    User getUserByOpenId(String openID);

}
