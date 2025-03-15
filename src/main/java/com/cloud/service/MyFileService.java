package com.cloud.service;

import com.cloud.entity.FileStoreStatistics;
import com.cloud.entity.MyFile;

import java.util.List;

public interface MyFileService {

    // 获取仓库的统计信息
    FileStoreStatistics getCountStatistics(Integer fileStoreId);

    // 根据父文件夹id获得文件
    List<MyFile> getFilesByParentFolderId(Integer parentFolderId);

    // 根据文件id删除文件
    Integer deleteByFileId(Integer myFileId);

    // 获得仓库根目录下的所有文件
    List<MyFile> getRootFilesByFileStoreId(Integer fileStoreId);
}
