package com.testplatform.modules.bug.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.modules.bug.entity.BugComment;

@Mapper
public interface BugCommentMapper extends BaseMapper<BugComment> {
}
