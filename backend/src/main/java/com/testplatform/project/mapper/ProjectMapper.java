package com.testplatform.project.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.project.entity.Project;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
