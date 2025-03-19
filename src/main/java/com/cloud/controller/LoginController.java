package com.cloud.controller;

import com.cloud.entity.FileStore;
import com.cloud.entity.User;
import com.cloud.utils.LogUtils;
import com.cloud.utils.MailUtils;
import com.qq.connect.QQConnectException;
import com.qq.connect.api.OpenID;
import com.qq.connect.api.qzone.UserInfo;
import com.qq.connect.javabeans.AccessToken;
import com.qq.connect.javabeans.qzone.UserInfoBean;
import com.qq.connect.oauth.Oauth;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * @author youzairichangdawang
 * @version 1.0
 * @Description 用户登录
 */
@Controller
public class LoginController extends BaseController {
    private Logger logger = LogUtils.getInstance(LoginController.class);

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @param code 验证码
     * @param map  各种提示信息
     * @return
     */
    @PostMapping("/register")
    public String register(User user, String code, Map<String, Object> map) {
        String userCode = (String) session.getAttribute(user.getEmail() + "_code");
        if (!code.equals(userCode)) {
            map.put("errorMsg", "验证码错误");
            return "index";
        }

        //用户名去空格
        user.setUserName(user.getUserName().trim());
        // 设置默认头像
        user.setImagePath("https://cdn.jsdelivr.net/gh/katomegumi99/cdn@main/images/head_megumi.jpg");
        user.setRegisterTime(new Date());
        user.setRole(1);

        if (userService.insert(user)) {// 向数据库中添加新的用户数据
            FileStore fileStore = FileStore.builder().userId(user.getUserId()).currentSize(0).build();
            fileStoreService.addFileStore(fileStore);
            // 为 user 封装 FileStoreId
            user.setFileStoreId(fileStore.getFileStoreId());
            userService.update(user);
            logger.info("注册用户成功！当前注册用户" + user);
            logger.info("注册仓库成功！当前注册仓库" + fileStore);
        } else {
            map.put("errorMsg", "服务器发生错误，注册失败");
            // 返回主页
            return "index";
        }
        session.removeAttribute(user.getEmail() + "_code");
        session.setAttribute("loginUser", user);

        // 重定向到主页
        return "redirect:/index";
    }

    /**
     * 用户登录
     *
     * @param user
     * @param map  各种提示信息
     * @return
     */
    @PostMapping("/login")
    public String login(User user, Map<String, Object> map) {
        // 通过邮箱从数据库中读取用户信息
        User userByEmail = userService.getUserByEmail(user.getEmail());
        if (userByEmail != null && userByEmail.getPassword().equals(user.getPassword())) {
            session.setAttribute("loginUser", userByEmail);
            logger.info("登录成功！" + userByEmail);
            return "redirect:/index";
        } else {
            User userByEmail1 = userService.getUserByEmail(user.getEmail());
            String errorMsg = userByEmail1 == null ? "该邮箱未注册" : "密码错误";
            logger.info("登录失败！请确认邮箱和密码是否正确！");
            //登录失败，将失败信息返回前端渲染
            map.put("errorMsg", errorMsg);
            return "index";
        }
    }


    /**
     * 向注册邮箱发送验证码, 并验证邮箱是否已使用
     */
    @ResponseBody
    @RequestMapping("/sendCode")
    public String sendCode(String userName, String email, String password) {
        User userByEmail = userService.getUserByEmail(email);
        if (userByEmail != null) {
            logger.info("发送验证码失败！邮箱已被注册！");
            return "exitEmail";
        }
        logger.info("开始发送邮件.../n" + "获取的到邮件发送对象为:" + mailSender);
        mailUtils = new MailUtils(mailSender);
        String code = mailUtils.sendCode(email, userName, password);
        session.setAttribute(email + "_code", code);
        return "success";
    }

    /**
     * qq登录
     */
    @GetMapping("/loginByQQ")
    public void login() {
        response.setContentType("text/html;charset=utf-8");
        try {
            response.sendRedirect(new Oauth().getAuthorizeURL(request));
            logger.info("请求QQ登录,开始跳转...");
        } catch (QQConnectException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * QQ登录回调地址
     * @return
     **/
    @GetMapping("/connection")
    public String connection() {
        try {
            AccessToken accessTokenObj = (new Oauth()).getAccessTokenByRequest(request);
            String accessToken = null, openID = null;
            long tokenExpireIn = 0L;
            if ("".equals(accessTokenObj.getAccessToken())) {
                logger.error("登录失败:没有获取到响应参数");
                return "accessTokenObj=>" + accessTokenObj + "; accessToken" + accessTokenObj.getAccessToken();
            } else {
                accessToken = accessTokenObj.getAccessToken();
                tokenExpireIn = accessTokenObj.getExpireIn();
                logger.error("accessToken" + accessToken);
                request.getSession().setAttribute("demo_access_token", accessToken);
                request.getSession().setAttribute("demo_token_expirein", String.valueOf(tokenExpireIn));
                // 利用获取到的accessToken 去获取当前用的openid -------- start
                OpenID openIDObj = new OpenID(accessToken);
                openID = openIDObj.getUserOpenID();
                UserInfo qzoneUserInfo = new UserInfo(accessToken, openID);
                UserInfoBean userInfoBean = qzoneUserInfo.getUserInfo();
                if (userInfoBean.getRet() == 0) {
                    logger.info("用户的OPEN_ID: " + openID);
                    logger.info("用户的昵称: " + removeNonBmpUnicode(userInfoBean.getNickname()));
                    logger.info("用户的头像URI: " + userInfoBean.getAvatar().getAvatarURL100());
                    //设置用户信息
                    User user = userService.getUserByOpenId(openID);
                    if (user == null){
                        user = User.builder()
                                .openId(openID).userName(removeNonBmpUnicode(userInfoBean.getNickname()))
                                .imagePath(userInfoBean.getAvatar().getAvatarURL100()).
                                registerTime(new Date()).build();
                        if (userService.insert(user)){
                            logger.info("注册用户成功！当前注册用户" + user);
                            FileStore store = FileStore.builder().userId(user.getUserId()).build();
                            if (fileStoreService.addFileStore(store) == 1){
                                user.setFileStoreId(store.getFileStoreId());
                                userService.update(user);
                                logger.info("注册仓库成功！当前注册仓库" + store);
                            }
                        } else {
                            logger.error("注册用户失败！");
                        }
                    }else {
                        user.setUserName(removeNonBmpUnicode(userInfoBean.getNickname()));
                        user.setImagePath(userInfoBean.getAvatar().getAvatarURL100());
                        userService.update(user);
                    }
                    logger.info("QQ用户登录成功！"+user);
                    session.setAttribute("loginUser", user);
                    return "redirect:/index";
                } else {
                    logger.error("很抱歉，我们没能正确获取到您的信息，原因是： " + userInfoBean.getMsg());
                }
            }
        } catch (QQConnectException e) {
        } finally {
            logger.error("登录成功!");
        }
        return "登录失败!请查看日志信息...";
    }

    /**
     * 处理名称中的特殊符号
     * @param str
     * @return
     */
    public String removeNonBmpUnicode(String str) {
        if (str == null) {
            return null;
        }
        str = str.replaceAll("[^\\u0000-\\uFFFF]", "");
        if ("".equals(str)) {
            str = "($ _ $)";
        }
        return str;
    }


    /**
     * 退出登录
     * @return
     */
    @GetMapping("/logout")
    public String logout() {
        logger.info("用户退出登录");
        // 使 HttpSession 立即失效
        session.invalidate();
        return "redirect:/";
    }
}
