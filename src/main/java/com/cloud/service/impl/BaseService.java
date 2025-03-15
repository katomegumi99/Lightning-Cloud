package com.cloud.service.impl;

import com.cloud.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author youzairichangdawang
 * @version 1.0
 * @Description Service基类
 */
public class BaseService {

    @Autowired
    protected UserMapper userMapper;
    @Autowired
    protected MyFileMapper myFileMapper;
    @Autowired
    protected FileFolderMapper fileFolderMapper;
    @Autowired
    protected FileStoreMapper fileStoreMapper;
    @Autowired
    protected TempFileMapper tempFileMapper;
}
