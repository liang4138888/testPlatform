package com.testplatform.modules.bug.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.modules.bug.entity.BugAttachment;

@Mapper
public interface BugAttachmentMapper extends BaseMapper<BugAttachment> {
}
