package com.testplatform.modules.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testplatform.modules.notification.dto.NotificationResponse;
import com.testplatform.modules.notification.entity.Notification;
import com.testplatform.modules.notification.mapper.NotificationMapper;

@Service
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Transactional
    public void create(Long receiverId, Long senderId, Long bugId, String title, String content) {
        if (receiverId == null || receiverId.equals(senderId)) {
            return;
        }
        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setSenderId(senderId);
        notification.setBizType("BUG");
        notification.setBizId(bugId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setReadStatus("UNREAD");
        notification.setExternalStatus("PENDING");
        notificationMapper.insert(notification);
    }

    public List<NotificationResponse> list(Long receiverId) {
        return notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, receiverId)
                .orderByDesc(Notification::getCreatedAt))
            .stream()
            .map(NotificationResponse::from)
            .collect(Collectors.toList());
    }

    public Long unreadCount(Long receiverId) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
            .eq(Notification::getReceiverId, receiverId)
            .eq(Notification::getReadStatus, "UNREAD"));
    }

    @Transactional
    public void markRead(Long notificationId, Long receiverId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getReceiverId().equals(receiverId)) {
            return;
        }
        notification.setReadStatus("READ");
        notification.setReadAt(LocalDateTime.now());
        notificationMapper.updateById(notification);
    }

    @Transactional
    public void markAllRead(Long receiverId) {
        List<Notification> notifications = notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
            .eq(Notification::getReceiverId, receiverId)
            .eq(Notification::getReadStatus, "UNREAD"));
        for (Notification notification : notifications) {
            notification.setReadStatus("READ");
            notification.setReadAt(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
    }
}
