package com.cloud.mapper;

import com.cloud.entity.User;
import com.cloud.entity.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    // 获取已注册的用户数量
    Integer getUserCount();

    // 获取全部的用户
    List<UserDTO> getUsers();

    User getUserById(@Param("userId") Integer userId);

    int deleteById(@Param("userId") Integer userId);

    int insert(User user);

    int update(User user);

    User getUserByEmail(@Param("email") String email);

    User getUserByOpenId(@Param("openId") String openId);
}
