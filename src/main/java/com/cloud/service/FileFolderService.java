package com.cloud.service;

import com.cloud.entity.FileFolder;

import java.util.List;

public interface FileFolderService {

    // 根据仓库Id查询仓库根目录下的所有文件夹
    List<FileFolder> getRootFoldersByFileStoreId(Integer fileStoreId);

    // 根据父文件夹获得所有的文件夹
    List<FileFolder> getFileFolderByParentFolderId(Integer parentFolderId);

    // 根据id删除文件夹
    Integer deleteFileFolderById(Integer fileFolderId);

    // 根据fileFolderId查询文件夹
    FileFolder getFileFolderByFileFolderId(Integer fileFolderId);

}
