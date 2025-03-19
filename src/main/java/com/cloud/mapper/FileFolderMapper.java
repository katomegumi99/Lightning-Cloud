package com.cloud.mapper;

import com.cloud.entity.FileFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileFolderMapper {

    // 根据父文件夹获得所有的文件夹
    List<FileFolder> getFileFolderByParentFolderId(@Param("parentFolderId") Integer parentFolderId);

    //根据id删除文件夹
    Integer deleteFileFolderById(@Param("fileFolderId") Integer fileFolderId);

    List<FileFolder> getRootFoldersByFileStoreId(@Param("fileStoreId") Integer fileStoreId);

//    根据文件夹id获取文件夹
    FileFolder getFileFolderById(@Param("fileFolderId") Integer fileFolderId);

    Integer addFileFolder(FileFolder folder);

    Integer updateFileFolderById(FileFolder fileFolder);
}
