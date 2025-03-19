package com.cloud.mapper;

import com.cloud.entity.TempFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TempFileMapper {
    /**
     * 插入临时文件
     * @param tempFile
     * @return
     */
    int insert(TempFile tempFile);

    TempFile queryById(@Param("fileId") Integer fileId);

    int deleteById(@Param("fileId")Integer fileId);
}
