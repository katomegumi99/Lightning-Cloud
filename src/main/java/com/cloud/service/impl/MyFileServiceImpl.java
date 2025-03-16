package com.cloud.service.impl;

import com.cloud.entity.FileStoreStatistics;
import com.cloud.entity.MyFile;
import com.cloud.service.MyFileService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author youzairichangdawang
 * @version 1.0
 * @Description TODO
 */
@Service
public class MyFileServiceImpl extends BaseService implements MyFileService {

    /**
     * 获取仓库的统计信息
     *
     * @param fileStoreId
     * @return
     */
    @Override
    public FileStoreStatistics getCountStatistics(Integer fileStoreId) {
        FileStoreStatistics statistics = myFileMapper.getCountStatistics(fileStoreId);
        return statistics;
    }

    // 根据父文件夹id获得文件
    @Override
    public List<MyFile> getFilesByParentFolderId(Integer parentFolderId) {
        return myFileMapper.getFilesByParentFolderId(parentFolderId);
    }

    /**
     * 根据文件id删除文件
     *
     * @param myFileId
     */
    @Override
    public Integer deleteByFileId(Integer myFileId) {
        return myFileMapper.deleteByFileId(myFileId);
    }

    /**
     * 获得仓库根目录下的所有文件
     *
     * @param fileStoreId
     * @return
     */
    @Override
    public List<MyFile> getRootFilesByFileStoreId(Integer fileStoreId) {
        return myFileMapper.getRootFilesByFileStoreId(fileStoreId);
    }

    /**
     * 根据类型获取文件
     * @param fileStoreId
     * @param type
     * @return
     */
    @Override
    public List<MyFile> getFilesByType(Integer fileStoreId, Integer type) {
        return myFileMapper.getFilesByType(fileStoreId, type);
    }
}
