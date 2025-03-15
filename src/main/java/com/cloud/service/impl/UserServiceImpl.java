package com.cloud.service.impl;

import com.cloud.entity.User;
import com.cloud.entity.UserDTO;
import com.cloud.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author youzairichangdawang
 * @version 1.0
 * @Description TODO
 */
@Service
public class UserServiceImpl extends BaseService implements UserService {

    /**
     * 获取已注册的用户数量
     * @return
     */
    @Override
    public Integer getUsersCount() {
        return userMapper.getUserCount();
    }

    /**
     * 获取全部的用户
     * @return
     */
    @Override
    public List<UserDTO> getUsers() {
        return userMapper.getUsers();
    }

    @Override
    public User queryById(Integer userId) {
        return userMapper.getUserById(userId);
    }

    @Override
    public boolean deleteById(Integer userId) {
        if(userMapper.deleteById(userId) == 1){
            return true;
        }
        return false;
    }

    @Override
    public boolean insert(User user) {
        if(userMapper.insert(user) == 1){
            return true;
        }
        return false;
    }

    @Override
    public boolean update(User user) {
        if(userMapper.update(user) == 1){
            return true;
        }
        return false;
    }

    @Override
    public User getUserByEmail(String email) {
        return userMapper.getUserByEmail(email);
    }

    @Override
    public User getUserByOpenId(String openId) {
        return userMapper.getUserByOpenId(openId);
    }
}
