import { request } from './http';

export interface NotificationItem {
  id: number;
  bizId: number;
  bizType: string;
  title: string;
  content: string;
  readStatus: string;
  createdAt: string;
}

export function listNotifications() {
  return request<NotificationItem[]>('/api/notifications');
}

export function unreadCount() {
  return request<number>('/api/notifications/unread-count');
}

export function markNotificationRead(notificationId: number) {
  return request<void>(`/api/notifications/${notificationId}/read`, { method: 'PUT' });
}

export function markAllNotificationsRead() {
  return request<void>('/api/notifications/read-all', { method: 'PUT' });
}
