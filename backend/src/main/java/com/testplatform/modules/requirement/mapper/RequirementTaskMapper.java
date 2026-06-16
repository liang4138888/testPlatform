package com.testplatform.modules.requirement.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.modules.requirement.entity.RequirementTask;

@Mapper
public interface RequirementTaskMapper extends BaseMapper<RequirementTask> {
}
