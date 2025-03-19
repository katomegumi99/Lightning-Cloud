package com.cloud.controller;

import com.cloud.entity.FileFolder;
import com.cloud.entity.FileStore;
import com.cloud.entity.MyFile;
import com.cloud.entity.TempFile;
import com.cloud.utils.FtpUtil;
import com.cloud.utils.LogUtils;
import com.cloud.utils.QRCodeUtil;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author youzairichangdawang
 * @version 1.0
 * @Description 文件仓库
 */
@Controller
public class FileStoreController extends BaseController {

    private Logger logger = LogUtils.getInstance(FileStoreController.class);

    /**
     * 上传临时文件
     *
     * @param file
     * @param url
     * @return
     */
    @PostMapping("/uploadTempFile")
    public String uploadTempFile(@RequestParam("file") MultipartFile file, String url) {
        session.setAttribute("imgPath", "https://cdn.jsdelivr.net/gh/katomegumi99/cdn@main/images/head_megumi.jpg");
        // 替换文件名中的空格
        String fileName = file.getOriginalFilename().replaceAll(" ", "");
        // 判断文件名是否合法
        if (!checkTarget(fileName)) {
            logger.error("文件名不符合规范，临时文件上传失败！");
            session.setAttribute("msg", "文件名不符合规范，临时文件上传失败！");
            return "redirect:/temp-file";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String createTime = format.format(new Date());
        String path = "temp/" + createTime + "/" + UUID.randomUUID();

        try {
            if (FtpUtil.uploadFile("/" + path, fileName, file.getInputStream())) {
                logger.info("临时文件上传成功" + fileName);
                String size = String.valueOf(file.getSize());
                TempFile tempFile = TempFile.builder().fileName(fileName).filePath(path).size(size).uploadTime(new Date()).build();
                // 将临时文件存入数据库中
                if (tempFileService.insert(tempFile)) {
                    try {
                        // 生成分享链接
                        String id = UUID.randomUUID().toString();
                        String p = request.getSession().getServletContext().getRealPath("/user_img/");
                        Long t = tempFile.getUploadTime().getTime();
                        url = url + "/file/share?t=" +
                                UUID.randomUUID().toString().substring(0, 10) +
                                "&f=" + tempFile.getFileId() + "&p=" + size + "flag=2";
                        File targetFile = new File(p, "");
                        // 检查指定的目标目录是否存在，如果不存在，则尝试创建
                        if (!targetFile.exists()) {
                            targetFile.mkdirs();
                        }
                        File f = new File(p, id + ".jpg");
                        // 检查图片二维码是否已经存在
                        if (!f.exists()) {
                            //文件不存在,开始生成二维码并保存文件
                            OutputStream os = new FileOutputStream(f);
                            QRCodeUtil.encode(url, "/static/img/logo.png", os, true);
                            os.close();
                        }
                        // 异步删除临时文件
                        tempFileService.deleteById(tempFile.getFileId());
                        session.setAttribute("imgPath", "user_img/" + id + ".jpg");
                        session.setAttribute("url", url);
                        session.setAttribute("msg", "上传成功，扫码/访问链接 即可下载！");
                        return "redirect:/temp-file";
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    logger.info("临时文件写入数据库失败" + fileName);
                    session.setAttribute("url", "error");
                    session.setAttribute("msg", "服务器错误！上传临时文件失败！");
                }
            } else {
                //上传失败
                logger.info("临时文件上传失败!" + fileName);
                session.setAttribute("url", "error");
                session.setAttribute("msg", "服务器出错了，上传失败!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "redirect:/temp-file";
    }

    /**
     * 上传文件
     *
     * @param files
     * @return
     */
    @PostMapping("/uploadFile")
    @ResponseBody
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile files) {
        Map<String, Object> map = new HashMap<>();
        // 验证用户权限
        if (fileStoreService.getFileStoreByUserId(loginUser.getUserId()).getPermission() != 0) {
            logger.error("用户权限不足");
            map.put("code", 499);
            // 返回空集合
            return map;
        }

        FileStore store = fileStoreService.getFileStoreByUserId(loginUser.getUserId());
        Integer folderId = Integer.valueOf(request.getHeader("id"));
        String fileName = files.getOriginalFilename().replaceAll(" ", "");

        // 获取当前目录下所有文件，判断要上传的文件是否已经存在
        List<MyFile> myFiles = null;
        if (folderId == 0) {
            // 当前位置是根目录
            myFiles = myFileService.getRootFilesByFileStoreId(loginUser.getFileStoreId());
        } else {
            // 当前在其他目录
            myFiles = myFileService.getFilesByParentFolderId(folderId);
        }
        for (int i = 0; i < myFiles.size(); i++) {
            if ((myFiles.get(i).getMyFileName() + myFiles.get(i).getPostfix()).equals(fileName)) {
                logger.error("同名文件已存在！");
                map.put("code", 501);
                return map;
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String uploadTime = format.format(new Date());
        String path = loginUser.getUserId() + "/" + uploadTime + "/" + folderId;
        if (!checkTarget(fileName)) {
            logger.error("文件名不符合规范，上传失败");
            map.put("code", 502);
            return map;
        }
        // 计算文件大小
        Integer fileSize = Math.toIntExact(files.getSize() / 1024);

        // 检查仓库是否放得下该文件
        if (store.getCurrentSize() + fileSize > store.getMaxSize()) {
            logger.error("仓库存储空间不足！");
            map.put("code", 503);
            return map;
        }

        // 处理文件大小
        String size = String.valueOf(files.getSize() / 1024.0);
        int indexDot = size.lastIndexOf(".");
        size = size.substring(0, indexDot);
        int index = fileName.lastIndexOf(".");
        String tempName = fileName;
        String postfix = "";
        int type = 4;
        if (index != -1) {
            tempName = fileName.substring(index);
            fileName = fileName.substring(0, index);
            // 获取文件类型
            type = getType(tempName.toLowerCase());
            postfix = tempName.toLowerCase();
        }

        try {
            // 提交到FTP服务器
            boolean result = FtpUtil.uploadFile("/" + path, fileName + postfix, files.getInputStream());
            if (result) {
                // 上传成功
                logger.info("文件上传成功!" + files.getOriginalFilename());
                // 将文件写入数据库
                myFileService.addFileByFileStoreId(
                        MyFile.builder().myFileName(fileName).fileStoreId(loginUser.getFileStoreId()).myFilePath(path)
                                .downloadTime(0).uploadTime(new Date()).parentFolderId(folderId).size(Integer.valueOf(size))
                                .type(type).postfix(postfix).build()
                );
                // 更新当前仓库大小
                fileStoreService.addSize(store.getFileStoreId(), Integer.valueOf(size));
                try {
                    Thread.sleep(5000);
                    map.put("code", 200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                // 上传失败
                logger.error("文化上传失败" + files.getOriginalFilename());
                map.put("code", 504);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    /**
     * 文件下载
     *
     * @param fileId
     * @return
     */
    @GetMapping("/downloadFile")
    public String downloadFile(@RequestParam("fId") Integer fileId) {
        // 验证用户是否有下载权限
        if (fileStoreService.getFileStoreByUserId(loginUser.getUserId()).getPermission() != 2) {
            logger.error("用户权限不足！");
            return "redirect:/error401Page";
        }

        // 获取文件相关信息
        MyFile myFile = myFileService.getFileByFileId(fileId);
        String remotePath = myFile.getMyFilePath();
        String fileName = myFile.getMyFileName() + myFile.getPostfix();

        try {
            // 去FTP服务器下载文件
            BufferedOutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setCharacterEncoding("utf-8");
            // 设置返回类型
            response.setContentType("multipart/form-data");
            // 文件转码，防止出现中文乱码
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName, "UTF-8"));
            boolean result = FtpUtil.downloadFile("/" + remotePath, fileName, outputStream);
            if (result) {
                // 更新文件被下载的次数
                myFileService.updateFile(
                        MyFile.builder().myFileId(myFile.getMyFileId()).downloadTime(myFile.getDownloadTime() + 1).build());
                outputStream.flush();
                outputStream.close();
                logger.info("文件下载成功!" + myFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "success";
    }

    /**
     * 删除文件
     *
     * @param fileId
     * @param folderId
     * @return
     */
    @GetMapping("/deleteFile")
    public String deleteFile(@RequestParam("fId") Integer fileId, Integer folderId) {
        // 获取文件信息
        MyFile myFile = myFileService.getFileByFileId(fileId);
        String filePath = myFile.getMyFilePath();
        String fileName = myFile.getMyFileName() + myFile.getPostfix();

        // 从FTP服务器中删除文件
        boolean result = FtpUtil.deleteFile("/" + filePath, fileName);
        if (result) {
            //删除成功,返回空间
            fileStoreService.subSize(myFile.getFileStoreId(), Integer.valueOf(myFile.getSize()));
            //删除文件表对应的数据
            myFileService.deleteByFileId(fileId);
        }
        logger.info("删除文件成功!" + myFile);
        return "redirect:/files?fId=" + folderId;
    }

    /**
     * 删除文件夹并清空文件
     *
     * @param folderId
     * @return
     */
    @GetMapping("/deleteFolder")
    public String deleteFolder(@RequestParam("fId") Integer folderId) {
        FileFolder folder = fileFolderService.getFileFolderByFileFolderId(folderId);

        // 删除
        this.deleteFolder(folder);
        return folder.getParentFolderId() == 0 ? "redirect:/files" : "redirect:/files?fId=" + folder.getParentFolderId();
    }

    /**
     * 添加文件夹
     *
     * @param folder
     * @param map
     * @return
     */
    @PostMapping("/addFolder")
    public String addFolder(FileFolder folder, Map<String, Object> map) {
        // 设置文件夹信息
        folder.setFileStoreId(loginUser.getFileStoreId());
        folder.setTime(new Date());

        // 检查文件夹是否已经存在
        List<FileFolder> fileFolders = null;
        if (folder.getParentFolderId() == 0) {
            // 当前在根目录
            fileFolders = fileFolderService.getRootFoldersByFileStoreId(loginUser.getFileStoreId());
        } else {
            // 当前在其他目录
            fileFolders = fileFolderService.getFileFolderByParentFolderId(folder.getParentFolderId());
        }
        for (int i = 0; i < fileFolders.size(); i++) {
            FileFolder fileFolder = fileFolders.get(i);
            if (fileFolder.getFileFolderName().equals(folder.getFileFolderName())) {
                logger.info("已存在同名文件夹");
                return "redirect:/files?error=1&fId=" + folder.getFileFolderId();
            }
        }
        // 存入数据库
        Integer rows = fileFolderService.addFileFolder(folder);
        logger.info("添加文件夹成功!" + folder);
        return "redirect:/files?fId=" + folder.getParentFolderId();
    }

    /**
     * 重命名文件夹
     *
     * @param folder
     * @param map
     * @return
     */
    @PostMapping("/updateFolder")
    public String uploadFolder(FileFolder folder, Map<String, Object> map) {
        // 获取文件夹信息
        FileFolder fileFolder = fileFolderService.getFileFolderByFileFolderId(folder.getFileFolderId());
        // 更新文件夹名
        fileFolder.setFileFolderName(folder.getFileFolderName());
        // 获取当前目录下的所有文件夹，检查是否重复命名
        List<FileFolder> fileFolders = fileFolderService.getFileFolderByParentFolderId(fileFolder.getParentFolderId());
        for (int i = 0; i < fileFolders.size(); i++) {
            FileFolder folder1 = fileFolders.get(i);
            if (folder1.getFileFolderName().equals(folder.getFileFolderName()) && folder1.getFileFolderId() != (folder.getFileFolderId())) {
                logger.info("同名文件夹已存在！");
                return "redirect:/files?error=2&fId=" + fileFolder.getParentFolderId();
            }
        }

        // 写入数据库
        fileFolderService.updateFileFolderById(fileFolder);
        logger.info("重命名文件夹成功!" + folder);
        return "redirect:/files?fId=" + fileFolder.getParentFolderId();
    }

    /**
     * 重命名文件
     *
     * @param file
     * @param map
     * @return
     */
    @PostMapping("/updateFileName")
    public String updateFileName(MyFile file, Map<String, Object> map) {
        MyFile myFile = myFileService.getFileByFileId(file.getMyFileId());
        if (myFile != null) {
            String oldName = myFile.getMyFileName();
            String newName = file.getMyFileName();
            if (!oldName.equals(newName)) {
                boolean result = FtpUtil.reNameFile(myFile.getMyFilePath() + "/" + oldName + myFile.getPostfix(),
                        myFile.getMyFilePath() + "/" + newName + myFile.getPostfix());
                if (result) {
                    Integer row = myFileService.updateFile(MyFile.builder().myFileId(myFile.getMyFileId()).myFileName(newName).build());
                    if (row == 1) {
                        logger.info("修改文件名成功!原文件名:" + oldName + "  新文件名:" + newName);
                    } else {
                        logger.error("修改文件名失败!原文件名:" + oldName + "  新文件名:" + newName);
                    }
                }
            }
        }
        return "redirect:/files?fId=" + myFile.getParentFolderId();
    }

    /**
     * 获取分享二维码
     *
     * @param id
     * @param url
     * @return
     */
    @GetMapping("getQrCode")
    @ResponseBody
    public Map<String, Object> getQrCode(@RequestParam Integer id, @RequestParam String url) {
        Map<String, Object> map = new HashMap<>();
        map.put("imgPath", "https://cdn.jsdelivr.net/gh/katomegumi99/cdn@main/images/head_megumi.jpg");
        if (id != null) {
            MyFile file = myFileService.getFileByFileId(id);
            if (file != null) {
                try {
                    String path = request.getSession().getServletContext().getRealPath("/user_img/");
                    url = url + "/file/share?t=" + UUID.randomUUID().toString().substring(0, 10) + "&f=" +
                            file.getMyFileId() + "&p=" + file.getUploadTime().getTime() + "" +
                            file.getSize() + "&flag=1";
                    File targetFile = new File(path, "");
                    // 检查指定的目标目录是否存在，如果不存在，则尝试创建
                    if (!targetFile.exists()) {
                        targetFile.mkdirs();
                    }
                    File f = new File(path, id + ".jpg");
                    // 检查图片二维码是否已存在
                    if (!f.exists()) {
                        // 文件不存在,生成二维码并保存文件
                        OutputStream os = new FileOutputStream(f);
                        QRCodeUtil.encode(url, "/static/img/logo.png", os, true);
                        os.close();
                    }
                    map.put("imgPath", "user_img/" + id + ".jpg");
                    map.put("url", url);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return map;
    }

    /**
     * 分享文件
     *
     * @param f
     * @param p
     * @param t
     * @param flag
     * @return
     */
    @GetMapping("/file/share")
    public String shareFile(Integer f, String p, String t, Integer flag) {
        String fileNameTemp = "";
        String remotePath = "";
        String fileName = "";
        Integer times = 0;

        if (flag == null || f == null || p == null || t == null) {
            logger.info("下载分享文件失败，参数错误");
            return "redirect:/error400Page";
        }

        if (flag == 1) {
            // 获取文件信息
            MyFile myFile = myFileService.getFileByFileId(f);

            // 检查文件是否存在
            if (myFile == null) {
                return "redirect:/error404Page";
            }

            // 生成密钥
            String pwd = myFile.getUploadTime().getTime() + "" + myFile.getSize();
            if (!pwd.equals(p)) {
                return "redirect:/error400Page";
            }

            remotePath = myFile.getMyFilePath();
            fileName = myFile.getMyFileName() + myFile.getPostfix();
        } else if (flag == 2) {
            TempFile tempFile = tempFileService.queryById(f);

            if (tempFile == null) {
                return "redirect:/error404Page";
            }

            Long test = tempFile.getUploadTime().getTime();
            String pwd = tempFile.getSize();
            if (!pwd.equals(p)) {
                return "redirect:/error400Page";
            }

            remotePath = tempFile.getFilePath();
            fileName = tempFile.getFileName();
        } else {
            return "redirect:/error400Page";
        }
        fileNameTemp = fileName;

        try {
            // 解决下载文件时的中文文件名乱码问题
            boolean isMSIE = isMSBrowser(request);
            if (isMSIE) {
                // IE浏览器的乱码问题解决
                fileNameTemp = URLEncoder.encode(fileNameTemp, "UTF-8");
            } else {
                // 万能乱码问题解决
                fileNameTemp = new String(fileNameTemp.getBytes("UTF-8"), "ISO-8859-1");
            }
            // 去FTP上拉取
            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setCharacterEncoding("utf-8");
            // 设置返回类型
            response.setContentType("multipart/form-data");
            // 文件名转码一下，不然会出现中文乱码
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileNameTemp);
            if (FtpUtil.downloadFile("/" + remotePath, fileName, outputStream)) {
                myFileService.updateFile(
                        MyFile.builder().myFileId(f).downloadTime(times + 1).build());
                outputStream.flush();
                outputStream.close();
                logger.info("文件下载成功!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "success";
    }


    /**
     * 删除文件夹里面的所有文件和子文件夹
     *
     * @param folder
     */
    public void deleteFolder(FileFolder folder) {
        //获得当前文件夹下的所有子文件夹
        List<FileFolder> folders = fileFolderService.getFileFolderByParentFolderId(folder.getFileFolderId());
        //删除当前文件夹的所有的文件
        List<MyFile> files = myFileService.getFilesByParentFolderId(folder.getFileFolderId());
        if (files.size() != 0) {
            for (int i = 0; i < files.size(); i++) {
                Integer fileId = files.get(i).getMyFileId();
                boolean b = FtpUtil.deleteFile("/" + files.get(i).getMyFilePath(), files.get(i).getMyFileName() + files.get(i).getPostfix());
                if (b) {
                    myFileService.deleteByFileId(fileId);
                    fileStoreService.subSize(folder.getFileStoreId(), Integer.valueOf(files.get(i).getSize()));
                }
            }
        }
        if (folders.size() != 0) {
            for (int i = 0; i < folders.size(); i++) {
                this.deleteFolder(folders.get(i));
            }
        }
        fileFolderService.deleteFileFolderById(folder.getFileFolderId());
    }


    /**
     * 用正则表达式验证文件名是否符合（汉字,字符,数字,下划线,英文句号,横线）
     *
     * @param target
     * @return
     */
    public boolean checkTarget(String target) {
        final String format = "[^\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w-_.]";
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(target);
        return !matcher.find();
    }

    /**
     * 检查浏览器是否为IE
     *
     * @param request
     * @return
     */
    public static boolean isMSBrowser(HttpServletRequest request) {
        String[] IEBrowserSignals = {"MSIE", "Trident", "Edge"};
        String userAgent = request.getHeader("User-Agent");
        for (String signal : IEBrowserSignals) {
            if (userAgent.contains(signal)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据文件名的后缀判断文件类型
     *
     * @param type
     * @return
     */
    public int getType(String type) {
        if (".chm".equals(type) || ".txt".equals(type) || ".xmind".equals(type) || ".xlsx".equals(type) || ".md".equals(type)
                || ".doc".equals(type) || ".docx".equals(type) || ".pptx".equals(type)
                || ".wps".equals(type) || ".word".equals(type) || ".html".equals(type) || ".pdf".equals(type)) {
            return 1;
        } else if (".bmp".equals(type) || ".gif".equals(type) || ".jpg".equals(type) || ".ico".equals(type) || ".vsd".equals(type)
                || ".pic".equals(type) || ".png".equals(type) || ".jepg".equals(type) || ".jpeg".equals(type) || ".webp".equals(type)
                || ".svg".equals(type)) {
            return 2;
        } else if (".avi".equals(type) || ".mov".equals(type) || ".qt".equals(type)
                || ".asf".equals(type) || ".rm".equals(type) || ".navi".equals(type) || ".wav".equals(type)
                || ".mp4".equals(type) || ".mkv".equals(type) || ".webm".equals(type)) {
            return 3;
        } else if (".mp3".equals(type) || ".wma".equals(type)) {
            return 4;
        } else {
            return 5;
        }
    }
}
