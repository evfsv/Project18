import request from '../utils/request';

// 获取所有通知
export const getNotifications = () => {
  return request({
    url: '/notifications',
    method: 'get',
  });
};

// 获取未读通知
export const getUnreadNotifications = () => {
  return request({
    url: '/notifications/unread',
    method: 'get',
  });
};

// 获取未读通知数量
export const getUnreadCount = () => {
  return request({
    url: '/notifications/unread/count',
    method: 'get',
  });
};

// 标记单个通知为已读
export const markAsRead = (id) => {
  return request({
    url: `/notifications/${id}/read`,
    method: 'put',
  });
};

// 标记所有通知为已读
export const markAllAsRead = () => {
  return request({
    url: '/notifications/read-all',
    method: 'put',
  });
};
