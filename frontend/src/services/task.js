import request from '../utils/request';

// 创建任务
export const createTask = (data) => {
  return request({
    url: '/tasks',
    method: 'post',
    data,
  });
};

// 获取文档的所有任务
export const getDocumentTasks = (documentId) => {
  return request({
    url: `/tasks/document/${documentId}`,
    method: 'get',
  });
};

// 获取我的任务
export const getMyTasks = () => {
  return request({
    url: '/tasks/my',
    method: 'get',
  });
};

// 更新任务状态
export const updateTaskStatus = (id, status) => {
  return request({
    url: `/tasks/${id}/status`,
    method: 'put',
    params: { status },
  });
};

// 删除任务
export const deleteTask = (id) => {
  return request({
    url: `/tasks/${id}`,
    method: 'delete',
  });
};
