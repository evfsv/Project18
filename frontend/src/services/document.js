import request from '../utils/request';

// 创建文档
export const createDocument = (data) => {
  return request({
    url: '/documents',
    method: 'post',
    data,
  });
};

// 更新文档
export const updateDocument = (id, data) => {
  return request({
    url: `/documents/${id}`,
    method: 'put',
    data,
  });
};

// 获取文档详情
export const getDocument = (id) => {
  return request({
    url: `/documents/${id}`,
    method: 'get',
  });
};

// 获取用户的所有文档
export const getUserDocuments = () => {
  return request({
    url: '/documents',
    method: 'get',
  });
};

// 获取与我共享的文档
export const getSharedDocuments = () => {
  return request({
    url: '/documents/shared',
    method: 'get',
  });
};

// 搜索文档
export const searchDocuments = (keyword) => {
  return request({
    url: '/documents/search',
    method: 'get',
    params: { keyword },
  });
};

// 获取文档模板
export const getTemplates = () => {
  return request({
    url: '/documents/templates',
    method: 'get',
  });
};

// 删除文档
export const deleteDocument = (id) => {
  return request({
    url: `/documents/${id}`,
    method: 'delete',
  });
};

// 分享文档
export const shareDocument = (id, targetUserId, permission) => {
  return request({
    url: `/documents/${id}/share`,
    method: 'post',
    params: { targetUserId, permission },
  });
};
