import axios from 'axios';
import { message } from 'antd';
import StorageUtil from './storage';

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从 sessionStorage 获取 token
    const token = StorageUtil.getToken();
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data;
    
    // 如果返回的状态码不是200，说明有错误
    if (res.code !== 200) {
      message.error(res.message || '请求失败');
      
      // 401: 未授权，跳转到登录页
      if (res.code === 401) {
        StorageUtil.removeToken();
        StorageUtil.removeUser();
        window.location.href = '/login';
      }
      
      return Promise.reject(new Error(res.message || '请求失败'));
    }
    
    return res;
  },
  (error) => {
    console.error('请求错误：', error);
    
    if (error.response) {
      if (error.response.status === 401) {
        message.error('登录已过期，请重新登录');
        StorageUtil.removeToken();
        StorageUtil.removeUser();
        window.location.href = '/login';
      } else {
        message.error(error.response.data?.message || '请求失败');
      }
    } else {
      message.error('网络错误，请检查网络连接');
    }
    
    return Promise.reject(error);
  }
);

export default request;
