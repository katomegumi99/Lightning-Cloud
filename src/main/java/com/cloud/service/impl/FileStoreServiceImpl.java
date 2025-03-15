package com.cloud.service.impl;

import com.cloud.entity.FileStore;
import com.cloud.service.FileStoreService;
import org.springframework.stereotype.Service;

/**
 * @author youzairichangdawang
 * @version 1.0
 * @Description TODO
 */
@Service
public class FileStoreServiceImpl extends BaseService implements FileStoreService {

    // 更新仓库权限
    @Override
    public Integer updatePermission(Integer userId, Integer permission, Integer size) {
        return fileStoreMapper.updatePermission(userId, permission, size);
    }

    // 根据userId查询用户的仓库
    @Override
    public FileStore getFileStoreByUserId(Integer userId) {
        return fileStoreMapper.getFileStoreByUserId(userId);
    }

    /**
     * 修改仓库当前已使用的容量
     *
     * @param fileStoreId
     * @param size
     * @return
     */
    @Override
    public Integer subSize(Integer fileStoreId, Integer size) {
        return fileStoreMapper.subSize(fileStoreId, size);
    }


    @Override
    public Integer deleteById(Integer fileStoreId) {
        return fileStoreMapper.deleteById(fileStoreId);
    }

    @Override
    public Integer addFileStore(FileStore fileStore) {
        return fileStoreMapper.addFileStore(fileStore);
    }

    @Override
    public FileStore getFileStoreById(Integer fileStoreId) {
        return fileStoreMapper.getFileStoreById(fileStoreId);
    }
}
