package com.testplatform.modules.notification.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testplatform.modules.notification.entity.Notification;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
