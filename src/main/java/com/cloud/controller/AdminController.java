package com.cloud.controller;

import com.cloud.entity.*;
import com.cloud.service.MyFileService;
import com.cloud.service.impl.BaseService;
import com.cloud.utils.FtpUtil;
import com.cloud.utils.LogUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.Getter;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author youzairichangdawang
 * @version 1.0
 * @Description:
 */
@Controller
public class AdminController extends BaseController {
    private Logger logger = LogUtils.getInstance(AdminController.class);

    /**
     * 用户管理页面
     *
     * @param map     封装用于页面显示的信息
     * @param current 当前页
     * @return
     */
    @GetMapping("/manages-users")
    public String manageUsers(Map<String, Object> map, Integer current) {
        // 判断权限是否满足
        if (loginUser.getRole() == 1) {
            logger.error("当前登录用户：" + loginUser.getUserName() + "无管理员权限");
            return "redirect:/error401Page";
        }

        // 获取已注册的用户数量
        Integer usersCount = userService.getUsersCount();

        // 查询当前页数，如果为空，默认为0
        current = (current == null || current < 0) ? 0 : current;

        // 获取统计信息
        FileStoreStatistics statistics = myFileService.getCountStatistics(loginUser.getFileStoreId());

        // 分页获取20个用户信息
        Page<Object> page = PageHelper.startPage(current, 20);
        List<UserDTO> users = userService.getUsers();
        map.put("statistics", statistics);
        map.put("users", users);
        map.put("page", page);
        map.put("usersCount", usersCount);
        logger.info("用户管理域的内容：" + map);

        return "admin/manage-users";
    }


    /**
     * 修改用户的权限和最大容量
     *
     * @param userId     用户id
     * @param permission 权限
     * @param size       容量
     * @return
     */
    @GetMapping("/updateStoreInfo")
    @ResponseBody
    public String updateStoreInfo(Integer userId, Integer permission, Integer size) {
        // 修改权限和容量
        Integer result = fileStoreService.updatePermission(userId, permission, size);

        if (result == 1) {
            logger.info("修改用户" + userService.queryById(userId).getUserName() + "：的权限和仓库大小成功！");
            return "200";
        } else {
            //更新失败，返回500状态码
            logger.error("修改用户" + userService.queryById(userId).getUserName() + "：的权限和仓库大小失败！");
            return "500";
        }
    }

    /**
     * 删除用户
     *
     * @param userId
     * @param cur    当前页
     * @return
     */
    @GetMapping("/deleteUser")
    public String deleteUser(Integer userId, Integer cur) {
        // 查询当前页数，如果为空，默认为1
        cur = (cur == null || cur < 0) ? 1 : cur;

        // 获取用户信息
        User user = userService.queryById(userId);

        // 根据userId查询用户的仓库
        FileStore fileStore = fileStoreService.getFileStoreByUserId(userId);

        // 根据仓库Id查询仓库根目录下的所有文件夹
        List<FileFolder> folders = fileFolderService.getRootFoldersByFileStoreId(fileStore.getFileStoreId());

        // 循环删除文件夹
        for (FileFolder fileFolder : folders) {
            this.deleteFolder(fileFolder);
        }
        // 获得仓库根目录下的所有文件
        List<MyFile> files = myFileService.getRootFilesByFileStoreId(fileStore.getFileStoreId());

        // 删除该用户仓库根目录下的所有文件
        for (MyFile file : files) {
            String filePath = file.getMyFilePath();
            String fileName = file.getMyFileName() + file.getPostfix();

            // 从FTP服务器上删除文件
            boolean deleteResult = FtpUtil.deleteFile("/" + filePath, fileName);
            if (deleteResult) {
                //删除文件表中的数据
                myFileService.deleteByFileId(file.getMyFileId());

                // 删除成功
                fileStoreService.subSize(file.getFileStoreId(), Integer.valueOf(file.getSize()));

            }
            logger.info("删除文件成功!" + file);
        }
        if (FtpUtil.deleteFolder("/" + userId)) {
            logger.info("清空FTP上该用户的文件成功");
        } else {
            logger.error("清空FTP上该用户的文件失败");
        }

        userService.deleteById(userId);
        fileStoreService.deleteById(fileStore.getFileStoreId());
        return "redirect:/manages-users?cur=" + cur;
    }

    /**
     * 循环删除文件夹里面的文件和子文件夹
     *
     * @param fileFolder
     */
    public void deleteFolder(FileFolder fileFolder) {
        // 获取当前文件夹下的所有子文件夹
        List<FileFolder> folders = fileFolderService.getFileFolderByParentFolderId(fileFolder.getParentFolderId());
        // 删除当前文件夹的所有文件
        List<MyFile> files = myFileService.getFilesByParentFolderId(fileFolder.getParentFolderId());
        if (files.size() != 0) {
            for (int i = 0; i < files.size(); i++) {
                Integer fileId = files.get(i).getMyFileId();
                boolean deleteResult =
                        FtpUtil.deleteFile("/" + files.get(i).getMyFilePath(),
                                files.get(i).getMyFileName() + files.get(i).getPostfix());
                if (deleteResult) {
                    myFileService.deleteByFileId(fileId);
                    fileStoreService.subSize(fileFolder.getFileStoreId(), Integer.valueOf(files.get(i).getSize()));
                }
            }
        }
        if (folders.size() != 0) {
            for (int i = 0; i < folders.size(); i++) {
                deleteFolder(folders.get(i));
            }
        }

        fileFolderService.deleteFileFolderById(fileFolder.getFileFolderId());
    }
}
