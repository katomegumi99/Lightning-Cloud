package com.cloud.controller;

import com.cloud.entity.User;
import com.cloud.service.*;
import com.cloud.utils.MailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author youzairichangdawang
 * @version 1.0
 * @Description: 控制器基类
 */
public class BaseController {
    @Autowired
    protected UserService userService;
    @Autowired
    protected MyFileService myFileService;
    @Autowired
    protected FileFolderService fileFolderService;
    @Autowired
    protected FileStoreService fileStoreService;
    @Autowired
    protected TempFileService tempFileService;

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected HttpSession session;
    protected User loginUser;

    @Autowired
    protected JavaMailSenderImpl mailSender;
    protected MailUtils mailUtils;

    /**
     * 在每个子类方法调用之前先调用
     * 设置request,response,session这三个对象
     * @param request
     * @param response
     */
    @ModelAttribute
    public void setReqAndRes(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.session = request.getSession(true);
        loginUser = (User) session.getAttribute("loginUser");
    }
}
