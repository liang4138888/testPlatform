package com.testplatform.modules.user.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.modules.user.entity.SystemPermission;

@Mapper
public interface SystemPermissionMapper extends BaseMapper<SystemPermission> {
}
