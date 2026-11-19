import React, { useState, useEffect } from 'react';
import { Layout, Menu, Badge, Dropdown, Avatar, Space, message } from 'antd';
import { 
  FileTextOutlined, 
  BellOutlined, 
  UserOutlined,
  LogoutOutlined,
  CheckCircleOutlined,
  CrownOutlined
} from '@ant-design/icons';
import { useNavigate, Outlet, useLocation } from 'react-router-dom';
import { logout } from '../services/auth';
import { getUnreadCount, getUnreadNotifications, markAsRead, markAllAsRead } from '../services/notification';
import StorageUtil from '../utils/storage';
import websocketService from '../services/websocket';
import './Layout.css';

const { Header, Content } = Layout;

function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const user = StorageUtil.getUser();
  
  // 检查是否是管理员
  const isAdmin = user.roles?.includes('ROLE_ADMIN') || false;

  // 加载未读通知数量
  const loadUnreadCount = async () => {
    try {
      const res = await getUnreadCount();
      setUnreadCount(res.data || 0);
    } catch (error) {
      console.error('加载未读通知数量失败：', error);
    }
  };

  // 加载未读通知列表
  const loadNotifications = async () => {
    try {
      const res = await getUnreadNotifications();
      setNotifications(res.data || []);
    } catch (error) {
      console.error('加载通知失败：', error);
    }
  };

  useEffect(() => {
    loadUnreadCount();
    loadNotifications();
    
    // 连接 WebSocket（如果还没连接）
    if (!websocketService.isConnected()) {
      websocketService.connect(
        () => {
          console.log('Layout: WebSocket 已连接');
          subscribeToNotifications();
        },
        (error) => {
          console.error('Layout: WebSocket 连接失败:', error);
        }
      );
    } else {
      // 如果已连接，直接订阅
      subscribeToNotifications();
    }
    
    // 每30秒刷新一次（作为备用）
    const interval = setInterval(() => {
      loadUnreadCount();
      loadNotifications();
    }, 30000);
    
    return () => {
      clearInterval(interval);
      // 取消订阅
      if (websocketService.isConnected() && websocketService.stompClient) {
        try {
          const subscription = websocketService.subscriptions[`notifications-${user.id}`];
          if (subscription) {
            subscription.unsubscribe();
            delete websocketService.subscriptions[`notifications-${user.id}`];
          }
        } catch (error) {
          console.error('取消订阅失败:', error);
        }
      }
    };
  }, []);

  // 订阅实时通知
  const subscribeToNotifications = () => {
    const userId = user.id;
    
    if (!websocketService.stompClient || !websocketService.connected) {
      console.error('WebSocket 未连接，无法订阅通知');
      return;
    }
    
    try {
      console.log('准备订阅通知，用户ID:', userId);
      
      // 订阅用户专属的通知队列
      const subscription = websocketService.stompClient.subscribe(
        `/user/${userId}/queue/notifications`,
        (message) => {
          console.log('收到 WebSocket 原始消息:', message);
          const notification = JSON.parse(message.body);
          console.log('解析后的通知:', notification);
          
          // 添加新通知到列表顶部
          setNotifications(prev => {
            console.log('更新通知列表，之前有', prev.length, '条');
            return [notification, ...prev];
          });
          
          // 更新未读数量
          setUnreadCount(prev => {
            console.log('更新未读数量，从', prev, '到', prev + 1);
            return prev + 1;
          });
          
          // 显示提示消息
          message.info(`新通知: ${notification.title}`);
        }
      );
      
      // 保存订阅引用
      websocketService.subscriptions[`notifications-${userId}`] = subscription;
      console.log('✅ 已成功订阅通知队列:', `/user/${userId}/queue/notifications`);
    } catch (error) {
      console.error('❌ 订阅通知失败:', error);
    }
  };

  // 登出
  const handleLogout = async () => {
    try {
      await logout();
      StorageUtil.removeToken();
      StorageUtil.removeUser();
      message.success('登出成功');
      navigate('/login');
    } catch (error) {
      console.error('登出失败：', error);
    }
  };

  // 标记通知为已读并跳转
  const handleNotificationClick = async (notification) => {
    try {
      await markAsRead(notification.id);
      loadUnreadCount();
      loadNotifications();
      
      // 根据通知类型跳转
      if (notification.documentId) {
        navigate(`/document/${notification.documentId}`);
      } else if (notification.taskId) {
        // 任务相关通知，跳转到文档的任务标签页
        navigate(`/document/${notification.documentId}?tab=3`);
      }
    } catch (error) {
      console.error('处理通知失败：', error);
    }
  };

  // 标记通知为已读
  const handleMarkAsRead = async (id) => {
    try {
      await markAsRead(id);
      loadUnreadCount();
      loadNotifications();
    } catch (error) {
      console.error('标记已读失败：', error);
    }
  };

  // 标记所有通知为已读
  const handleMarkAllAsRead = async () => {
    try {
      await markAllAsRead();
      message.success('已全部标记为已读');
      loadUnreadCount();
      loadNotifications();
    } catch (error) {
      console.error('标记全部已读失败：', error);
    }
  };

  // 通知下拉菜单
  const notificationMenu = {
    items: [
      ...notifications.slice(0, 5).map((item, index) => ({
        key: `notification-${index}`,
        label: (
          <div 
            className="notification-item"
            onClick={() => handleNotificationClick(item)}
            style={{ cursor: 'pointer', maxWidth: 300 }}
          >
            <div className="notification-title" style={{ fontWeight: 500, marginBottom: 4 }}>
              {item.title}
            </div>
            <div className="notification-content" style={{ fontSize: 12, color: '#666', marginBottom: 4 }}>
              {item.content}
            </div>
            {item.documentTitle && (
              <div style={{ fontSize: 12, color: '#1890ff', marginBottom: 2 }}>
                📄 文档: {item.documentTitle}
              </div>
            )}
            {item.commentContent && (
              <div style={{ 
                fontSize: 11, 
                color: '#52c41a', 
                marginBottom: 2,
                padding: '4px 8px',
                background: '#f6ffed',
                borderLeft: '2px solid #52c41a',
                borderRadius: 2
              }}>
                💬 {item.commentContent}
              </div>
            )}
            {item.taskTitle && (
              <div style={{ fontSize: 12, color: '#faad14', marginBottom: 2 }}>
                ✓ 任务: {item.taskTitle}
              </div>
            )}
            <div className="notification-time" style={{ fontSize: 11, color: '#999', marginTop: 4 }}>
              {new Date(item.createdAt).toLocaleString()}
            </div>
          </div>
        ),
      })),
      {
        key: 'mark-all',
        label: (
          <div style={{ textAlign: 'center', color: '#1890ff' }}>
            <CheckCircleOutlined /> 全部标记为已读
          </div>
        ),
        onClick: handleMarkAllAsRead,
      },
    ],
  };

  // 用户下拉菜单
  const userMenu = {
    items: [
      {
        key: 'profile',
        icon: <UserOutlined />,
        label: '个人资料',
        onClick: () => navigate('/profile'),
      },
      {
        type: 'divider',
      },
      {
        key: 'logout',
        icon: <LogoutOutlined />,
        label: '退出登录',
        onClick: handleLogout,
      },
    ],
  };

  // 菜单项
  const menuItems = [
    {
      key: '/',
      icon: <FileTextOutlined />,
      label: '我的文档',
      onClick: () => navigate('/'),
    },
  ];

  // 如果是管理员，添加管理员菜单
  if (isAdmin) {
    menuItems.push({
      key: '/admin',
      icon: <CrownOutlined />,
      label: '管理员面板',
      onClick: () => navigate('/admin'),
    });
  }

  return (
    <Layout className="app-layout">
      <Header className="app-header">
        <div className="header-left">
          <div className="logo" onClick={() => navigate('/')}>
            <FileTextOutlined style={{ fontSize: 24, marginRight: 8 }} />
            <span>协作编辑系统</span>
          </div>
          
          <Menu
            theme="dark"
            mode="horizontal"
            selectedKeys={[location.pathname]}
            items={menuItems}
            style={{ flex: 1, minWidth: 0, marginLeft: 40 }}
          />
        </div>
        
        <Space size="large">
          <Dropdown menu={notificationMenu} trigger={['click']}>
            <Badge count={unreadCount} offset={[10, 0]}>
              <BellOutlined style={{ fontSize: 20, cursor: 'pointer', color: 'white' }} />
            </Badge>
          </Dropdown>
          
          <Dropdown menu={userMenu} trigger={['click']}>
            <Space style={{ cursor: 'pointer' }}>
              <Avatar 
                icon={<UserOutlined />}
                src={user.avatar ? `http://localhost:8080${user.avatar}` : null}
              />
              <span style={{ color: 'white' }}>{user.username}</span>
              {isAdmin && <CrownOutlined style={{ color: '#faad14' }} />}
            </Space>
          </Dropdown>
        </Space>
      </Header>
      
      <Content className="app-content">
        <Outlet />
      </Content>
    </Layout>
  );
}

export default AppLayout;
