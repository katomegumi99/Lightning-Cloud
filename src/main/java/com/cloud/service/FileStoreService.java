package com.cloud.service;

import com.cloud.entity.FileStore;

public interface FileStoreService {

    // 更新仓库权限
    Integer updatePermission(Integer userId, Integer permission, Integer size);

    // 根据userId查询用户的仓库
    FileStore getFileStoreByUserId(Integer userId);

    // 修改仓库当前已使用的容量
    Integer subSize(Integer fileStoreId, Integer size);

    Integer deleteById(Integer fileStoreId);

    Integer addFileStore(FileStore fileStore);

    FileStore getFileStoreById(Integer fileStoreId);

    int addSize(Integer fileStoreId, Integer size);
}
