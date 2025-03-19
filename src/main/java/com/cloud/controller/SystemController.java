package com.cloud.controller;

import com.cloud.entity.FileFolder;
import com.cloud.entity.FileStoreStatistics;
import com.cloud.entity.MyFile;
import com.cloud.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author youzairichangdawang
 * @version 1.0
 * @Description TODO
 */
@Controller
public class SystemController extends BaseController {

    Logger logger = LogUtils.getInstance(SystemController.class);

    /**
     * 我的网盘页面
     *
     * @param fileFolderId 文件夹id
     * @param fileName
     * @param error
     * @param map          返回给前端的数据
     * @return
     */
    @GetMapping("/files")
    public String fileStorePage(Integer fileFolderId, String fileName, Integer error, Map<String, Object> map) {

        //判断是否包含错误信息
        if (error != null) {
            if (error == 1) {
                map.put("error", "添加失败！已存在同名文件夹");
            }
            if (error == 2) {
                map.put("error", "重命名失败！文件夹已存在");
            }
        }

        //包含的子文件夹
        List<FileFolder> folders = null;

        //包含的文件
        List<MyFile> files = null;

        //当前文件夹信息
        FileFolder nowFolder = null;

        // 当前文件夹的相对路径
        List<FileFolder> location = new ArrayList<>();
        if (fileFolderId == null || fileFolderId <= 0) {

            // 代表目前是根目录
            fileFolderId = 0;

            // 根据仓库Id获得仓库根目录下的所有文件夹
            folders = fileFolderService.getRootFoldersByFileStoreId(loginUser.getFileStoreId());

            // 获得仓库根目录下的所有文件
            files = myFileService.getRootFilesByFileStoreId(loginUser.getFileStoreId());

            // 构建当前文件夹信息
            nowFolder = FileFolder.builder().fileFolderId(fileFolderId).build();
            location.add(nowFolder);
        } else {

            // 当前为具体目录，情况1：访问的文件夹不是当前用户所创建的文件夹
            FileFolder folder = fileFolderService.getFileFolderByFileFolderId(fileFolderId);
            if (folder.getFileStoreId() - loginUser.getFileStoreId() != 0) {
                return "redirect:/error401Page";
            }

            // 当前为具体目录，情况2：访问的文件夹是当前用户所创建的文件夹
            folders = fileFolderService.getFileFolderByParentFolderId(fileFolderId);
            files = myFileService.getFilesByParentFolderId(fileFolderId);
            nowFolder = fileFolderService.getFileFolderByFileFolderId(fileFolderId);

            // 遍历当前目录
            FileFolder temp = nowFolder;
            while (temp.getParentFolderId() != 0) {
                temp = fileFolderService.getFileFolderByFileFolderId(temp.getParentFolderId());
                location.add(temp);
            }
        }
        // 形成最终目录
        Collections.reverse(location);

        // 获取统计信息
        FileStoreStatistics statistics = myFileService.getCountStatistics(loginUser.getFileStoreId());
        map.put("statistics", statistics);
        map.put("permission", fileStoreService.getFileStoreByUserId(loginUser.getUserId()).getPermission());
        map.put("folders", folders);
        map.put("files", files);
        map.put("nowFolder", nowFolder);
        map.put("location", location);
        logger.info("网盘页面域中的数据:" + map);
        return "u-admin/files";
    }

    /**
     * 登录后进入主页
     *
     * @param map
     * @return
     */
    @GetMapping("/index")
    public String index(Map<String, Object> map) {

        // 获得统计信息
        FileStoreStatistics statistics = myFileService.getCountStatistics(loginUser.getFileStoreId());
        statistics.setFileStore(fileStoreService.getFileStoreById(loginUser.getFileStoreId()));
        map.put("statistics", statistics);
        return "u-admin/index";
    }

    /**
     * 所有文档页面
     *
     * @param map
     * @return
     */
    @GetMapping("/doc-files")
    public String docFilePage(Map<String, Object> map) {
        List<MyFile> files = myFileService.getFilesByType(loginUser.getFileStoreId(), 1);
        // 获取统计信息
        FileStoreStatistics statistics = myFileService.getCountStatistics(loginUser.getFileStoreId());
        map.put("statistics", statistics);
        map.put("files", files);
        map.put("permission", fileStoreService.getFileStoreByUserId(loginUser.getUserId()).getPermission());
        return "u-admin/doc-files";
    }

    /**
     * 文件上传页面
     *
     * @param folderId
     * @param fileName
     * @param map
     * @return
     */
    @GetMapping("/upload")
    public String uploadPage(Integer folderId, String fileName, Map<String, Object> map) {

        // 包含的子文件夹集合
        List<FileFolder> folders = null;

        // 当前文件夹信息
        FileFolder nowFolder = null;

        //当前文件夹的相对路径
        List<FileFolder> location = new ArrayList<>();
        if (folderId == null || folderId <= 0) {
            // 当前目录为根目录
            folderId = 0;
            // 子目录
            folders = fileFolderService.getRootFoldersByFileStoreId(loginUser.getFileStoreId());
            nowFolder = FileFolder.builder().fileFolderId(folderId).build();
            location.add(nowFolder);
        } else {
            // 当前为具体目录
            folders = fileFolderService.getFileFolderByParentFolderId(folderId);
            nowFolder = fileFolderService.getFileFolderByFileFolderId(folderId);

            // 遍历当前目录
            FileFolder temp = nowFolder;
            while (temp.getParentFolderId() != 0) {
                temp = fileFolderService.getFileFolderByFileFolderId(temp.getParentFolderId());
                location.add(temp);
            }
        }
        Collections.reverse(location);

        // 封装统计信息
        FileStoreStatistics statistics = myFileService.getCountStatistics(loginUser.getFileStoreId());
        map.put("statistics", statistics);
        map.put("folders", folders);
        map.put("nowFolder", nowFolder);
        map.put("location", location);
        logger.info("网盘页面域中的数据:" + map);
        return "u-admin/upload";
    }

    /**
     * 图像文件页面
     *
     * @param map
     * @return
     */
    @GetMapping("/image-files")
    public String imageFilePage(Map<String, Object> map) {
        List<MyFile> files = myFileService.getFilesByType(loginUser.getFileStoreId(), 2);

        // 封装统计信息
        FileStoreStatistics statistics = myFileService.getCountStatistics(loginUser.getFileStoreId());
        map.put("statistics", statistics);
        map.put("files", files);
        map.put("permission", fileStoreService.getFileStoreByUserId(loginUser.getUserId()).getPermission());
        return "u-admin/image-files";
    }

    /**
     * 视频文件页面
     *
     * @param map
     * @return
     */
    @GetMapping("/video-files")
    public String videoFilePage(Map<String, Object> map) {
        // 获取文件
        List<MyFile> files = myFileService.getFilesByType(loginUser.getFileStoreId(), 3);

        // 封装统计信息
        FileStoreStatistics statistics = myFileService.getCountStatistics(loginUser.getFileStoreId());
        map.put("statistics", statistics);
        map.put("files", files);
        map.put("permission", fileStoreService.getFileStoreByUserId(loginUser.getUserId()).getPermission());
        return "u-admin/video-files";
    }

    /**
     * 音频文件页面
     *
     * @param map
     * @return
     */
    @GetMapping("/music-files")
    public String musicFilePage(Map<String, Object> map) {
        // 获取文件
        List<MyFile> files = myFileService.getFilesByType(loginUser.getFileStoreId(), 4);

        // 封装统计信息
        FileStoreStatistics statistics = myFileService.getCountStatistics(loginUser.getFileStoreId());
        map.put("statistics", statistics);
        map.put("files", files);
        map.put("permission", fileStoreService.getFileStoreByUserId(loginUser.getUserId()).getPermission());
        return "u-admin/music-files";
    }

    /**
     * 其他文件页面
     *
     * @param map
     * @return
     */
    @GetMapping("/other-files")
    public String otherFilePage(Map<String, Object> map) {
        // 获取文件
        List<MyFile> files = myFileService.getFilesByType(loginUser.getFileStoreId(), 5);

        // 封装仓库统计数据
        FileStoreStatistics statistics = myFileService.getCountStatistics(loginUser.getFileStoreId());
        map.put("statistics", statistics);
        map.put("files", files);
        map.put("permission", fileStoreService.getFileStoreByUserId(loginUser.getUserId()).getPermission());
        return "u-admin/other-files";
    }

    /**
     * 帮助页面
     *
     * @param map
     * @return
     */
    @GetMapping("/help")
    public String helpPage(Map<String, Object> map) {
        //获得统计信息
        FileStoreStatistics statistics = myFileService.getCountStatistics(loginUser.getFileStoreId());
        map.put("statistics", statistics);
        return "u-admin/help";
    }
}
