package com.testplatform.modules.file.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.modules.file.entity.FileObject;

@Mapper
public interface FileObjectMapper extends BaseMapper<FileObject> {
}
