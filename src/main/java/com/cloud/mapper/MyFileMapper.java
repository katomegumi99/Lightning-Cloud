package com.cloud.mapper;

import com.cloud.entity.FileStoreStatistics;
import com.cloud.entity.MyFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyFileMapper {
    // 获取仓库的统计信息
    FileStoreStatistics getCountStatistics(@Param("fileStoreId") Integer fileStoreId);

    // 根据父文件夹id获得文件
    List<MyFile> getFilesByParentFolderId(@Param("parentFolderId") Integer parentFolderId);

    // 根据文件id删除文件
    Integer deleteByFileId(@Param("myFileId") Integer myFileId);

    // 获得仓库根目录下的所有文件
    List<MyFile> getRootFilesByFileStoreId(@Param("fileStoreId") Integer fileStoreId);

    // 根据类型获取文件
    List<MyFile> getFilesByType(@Param("fileStoreId") Integer fileStoreId, @Param("type") Integer type);
}
