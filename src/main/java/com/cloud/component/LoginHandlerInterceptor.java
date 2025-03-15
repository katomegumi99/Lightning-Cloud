package com.cloud.component;

import com.cloud.entity.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author youzairichangdawang
 * @version 1.0
 * @Description 登录拦截器
 */
public class LoginHandlerInterceptor implements HandlerInterceptor {

    /**
     * 登录前进行校验
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object loginUser = request.getSession().getAttribute("loginUser");

        // 判断是否登录
        if ((loginUser == null)) {
            // 未登录
            response.sendRedirect("/lightning-cloud/");
            return false;
        }else {
            // 已登录
            return true;
        }
    }
}
