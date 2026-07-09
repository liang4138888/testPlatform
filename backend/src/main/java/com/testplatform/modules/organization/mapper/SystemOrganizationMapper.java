package com.testplatform.modules.organization.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.modules.organization.entity.SystemOrganization;

@Mapper
public interface SystemOrganizationMapper extends BaseMapper<SystemOrganization> {
}
