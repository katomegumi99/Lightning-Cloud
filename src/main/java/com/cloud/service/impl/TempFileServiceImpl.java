package com.cloud.service.impl;

import com.cloud.entity.TempFile;
import com.cloud.service.TempFileService;
import com.cloud.utils.FtpUtil;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author youzairichangdawang
 * @version 1.0
 * @Description TODO
 */
@Service
public class TempFileServiceImpl extends BaseService implements TempFileService {
    @Override
    public boolean insert(TempFile tempFile) {
        if (tempFileMapper.insert(tempFile) == 1) {
            return true;
        }
        return false;

    }

    @Override
    public void deleteById(Integer fileId) {
        try {
            // 4小时后删除临时文件
            TimeUnit.HOURS.sleep(4);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        TempFile tempFile = queryById(fileId);
        if (FtpUtil.deleteFile("/" + tempFile.getFilePath(), tempFile.getFileName())) {
            FtpUtil.deleteFolder("/" + tempFile.getFilePath());
            tempFileMapper.deleteById(fileId);
        }
    }

    @Override
    public TempFile queryById(Integer fileId) {
        return tempFileMapper.queryById(fileId);
    }
}
