package com.testplatform.modules.casesuite.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.modules.casesuite.entity.CaseSuite;

@Mapper
public interface CaseSuiteMapper extends BaseMapper<CaseSuite> {
}
