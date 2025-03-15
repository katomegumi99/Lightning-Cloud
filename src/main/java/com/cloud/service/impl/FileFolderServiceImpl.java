package com.cloud.service.impl;

import com.cloud.entity.FileFolder;
import com.cloud.mapper.FileFolderMapper;
import com.cloud.service.FileFolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author youzairichangdawang
 * @version 1.0
 * @Description TODO
 */
@Service
public class FileFolderServiceImpl extends BaseService implements FileFolderService {


    /**
     * 根据仓库Id查询仓库根目录下的所有文件夹
     * @param fileStoreId
     * @return
     */
    @Override
    public List<FileFolder> getRootFoldersByFileStoreId(Integer fileStoreId) {
        return fileFolderMapper.getRootFoldersByFileStoreId(fileStoreId);
    }


    /**
     * 根据父文件夹获得所有的文件夹
     * @param parentFolderId
     * @return
     */
    @Override
    public List<FileFolder> getFileFolderByParentFolderId(Integer parentFolderId) {
        return fileFolderMapper.getFileFolderByParentFolderId(parentFolderId);
    }

    /**
     * 根据id删除文件夹
     * @param fileFolderId
     * @return
     */
    @Override
    public Integer deleteFileFolderById(Integer fileFolderId) {
        return fileFolderMapper.deleteFileFolderById(fileFolderId);
    }

    /**
     * 根据文件夹id获取文件夹
     * @param fileFolderId
     * @return
     */
    @Override
    public FileFolder getFileFolderByFileFolderId(Integer fileFolderId) {
        return fileFolderMapper.getFileFolderById(fileFolderId);
    }
}
