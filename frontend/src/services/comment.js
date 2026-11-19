import request from '../utils/request';

// 添加评论
export const addComment = (data) => {
  return request({
    url: '/comments',
    method: 'post',
    data,
  });
};

// 获取文档的所有评论
export const getDocumentComments = (documentId) => {
  return request({
    url: `/comments/document/${documentId}`,
    method: 'get',
  });
};

// 删除评论
export const deleteComment = (id) => {
  return request({
    url: `/comments/${id}`,
    method: 'delete',
  });
};
