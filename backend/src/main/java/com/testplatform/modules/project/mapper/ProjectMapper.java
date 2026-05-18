package com.testplatform.modules.project.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.modules.project.entity.Project;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
