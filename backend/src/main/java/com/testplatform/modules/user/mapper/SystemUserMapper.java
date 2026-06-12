package com.testplatform.modules.user.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.modules.user.entity.SystemUser;

@Mapper
public interface SystemUserMapper extends BaseMapper<SystemUser> {
}
