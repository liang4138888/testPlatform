package com.testplatform.modules.auth.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.modules.auth.entity.AuthToken;

@Mapper
public interface AuthTokenMapper extends BaseMapper<AuthToken> {
}
