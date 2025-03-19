package com.cloud.mapper;

import com.cloud.entity.FileFolder;
import com.cloud.entity.FileStore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileStoreMapper {

    // 更新权限和容量
    Integer updatePermission(@Param("userId") Integer userId, @Param("permission") Integer permission, @Param("size") Integer size);

    // 根据userId查询用户的仓库
    FileStore getFileStoreByUserId(@Param("userId") Integer userId);

    // 修改仓库当前已使用的容量
    Integer subSize(@Param("fileStoreId") Integer fileStoreId, @Param("size") Integer size);

    Integer deleteById(@Param("fileStoreId") Integer fileStoreId);

    Integer addFileStore(FileStore fileStore);

    FileStore getFileStoreById(@Param("fileStoreId") Integer fileStoreId);

    Integer addSize(@Param("fileStoreId") Integer fileStoreId, @Param("size") Integer size);
}
