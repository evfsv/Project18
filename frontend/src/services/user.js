import request from '../utils/request';

// 获取当前用户信息
export const getCurrentUser = () => {
  return request({
    url: '/users/me',
    method: 'get',
  });
};

// 更新当前用户信息
export const updateCurrentUser = (data) => {
  return request({
    url: '/users/me',
    method: 'put',
    data,
  });
};

// 获取用户详情
export const getUserById = (id) => {
  return request({
    url: `/users/${id}`,
    method: 'get',
  });
};

// 获取所有用户（管理员）
export const getAllUsers = () => {
  return request({
    url: '/users',
    method: 'get',
  });
};

// 获取用户列表（用于分享等功能，所有用户可访问）
export const getUserList = () => {
  return request({
    url: '/users/list',
    method: 'get',
  });
};

// 分配角色（管理员）
export const assignRole = (userId, roleName) => {
  return request({
    url: `/users/${userId}/roles/${roleName}`,
    method: 'post',
  });
};

// 移除角色（管理员）
export const removeRole = (userId, roleName) => {
  return request({
    url: `/users/${userId}/roles/${roleName}`,
    method: 'delete',
  });
};
