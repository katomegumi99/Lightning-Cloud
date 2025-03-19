package com.cloud.service;

import com.cloud.entity.TempFile;

public interface TempFileService {

    boolean insert(TempFile tempFile);

    void deleteById(Integer fileId);

    TempFile queryById(Integer fileId);
}
