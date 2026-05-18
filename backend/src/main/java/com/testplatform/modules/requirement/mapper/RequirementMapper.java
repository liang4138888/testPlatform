package com.testplatform.modules.requirement.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.modules.requirement.entity.Requirement;

@Mapper
public interface RequirementMapper extends BaseMapper<Requirement> {
}
