/**
 * 存储工具类
 * 使用 sessionStorage 实现标签页隔离
 * 每个标签页有独立的登录状态，互不影响
 */

// 使用 sessionStorage 替代 localStorage
// sessionStorage 是标签页隔离的，每个标签页有独立的存储空间
const storage = sessionStorage;

const StorageUtil = {
  // 获取 token
  getToken() {
    return storage.getItem('token');
  },

  // 设置 token
  setToken(token) {
    storage.setItem('token', token);
  },

  // 移除 token
  removeToken() {
    storage.removeItem('token');
  },

  // 获取用户信息
  getUser() {
    const userStr = storage.getItem('user');
    return userStr ? JSON.parse(userStr) : {};
  },

  // 设置用户信息
  setUser(user) {
    storage.setItem('user', JSON.stringify(user));
  },

  // 移除用户信息
  removeUser() {
    storage.removeItem('user');
  },

  // 清空所有存储
  clear() {
    storage.clear();
  }
};

export default StorageUtil;
