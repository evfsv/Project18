import React, { useState, useEffect } from 'react';
import { Card, Form, Input, Button, message, Descriptions, Tag, Space, Avatar, Upload } from 'antd';
import { UserOutlined, MailOutlined, PhoneOutlined, SaveOutlined, CameraOutlined } from '@ant-design/icons';
import { getCurrentUser, updateCurrentUser } from '../services/user';
import StorageUtil from '../utils/storage';
import moment from 'moment';
import './Profile.css';

function Profile() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(false);
  const [editing, setEditing] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [form] = Form.useForm();

  // 加载用户信息
  const loadUserInfo = async () => {
    setLoading(true);
    try {
      const res = await getCurrentUser();
      setUser(res.data);
      form.setFieldsValue({
        email: res.data.email,
        phone: res.data.phone || '',
      });
    } catch (error) {
      console.error('加载用户信息失败：', error);
      message.error('加载用户信息失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUserInfo();
  }, []);

  // 更新用户信息
  const handleUpdate = async (values) => {
    setLoading(true);
    try {
      await updateCurrentUser(values);
      message.success('个人资料更新成功');
      setEditing(false);
      loadUserInfo();
    } catch (error) {
      console.error('更新失败：', error);
      message.error('更新失败');
    } finally {
      setLoading(false);
    }
  };

  // 上传头像
  const handleAvatarUpload = async (file) => {
    setUploading(true);
    const formData = new FormData();
    formData.append('file', file);

    try {
      const token = StorageUtil.getToken();
      const response = await fetch('http://localhost:8080/api/files/upload/avatar', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
        body: formData,
      });

      const result = await response.json();
      
      if (result.code === 200) {
        message.success('头像上传成功');
        loadUserInfo();
        
        // 更新 sessionStorage 中的用户信息
        const userInfo = StorageUtil.getUser();
        userInfo.avatar = result.data;
        StorageUtil.setUser(userInfo);
      } else {
        message.error(result.message || '头像上传失败');
      }
    } catch (error) {
      console.error('上传失败：', error);
      message.error('头像上传失败');
    } finally {
      setUploading(false);
    }

    return false; // 阻止默认上传行为
  };

  // 角色标签颜色
  const getRoleColor = (roleName) => {
    switch (roleName) {
      case 'ROLE_ADMIN':
        return 'red';
      case 'ROLE_EDITOR':
        return 'blue';
      case 'ROLE_VIEWER':
        return 'green';
      default:
        return 'default';
    }
  };

  // 角色显示名称
  const getRoleDisplayName = (roleName) => {
    switch (roleName) {
      case 'ROLE_ADMIN':
        return '管理员';
      case 'ROLE_EDITOR':
        return '编辑者';
      case 'ROLE_VIEWER':
        return '查看者';
      default:
        return roleName;
    }
  };

  if (!user) {
    return (
      <div className="profile-container">
        <Card loading={true} />
      </div>
    );
  }

  // 获取头像URL
  const avatarUrl = user.avatar 
    ? `http://localhost:8080${user.avatar}` 
    : null;

  return (
    <div className="profile-container">
      <Card
        title={
          <Space>
            <div className="avatar-upload-wrapper">
              <Avatar 
                size={64} 
                icon={<UserOutlined />}
                src={avatarUrl}
              />
              <Upload
                showUploadList={false}
                beforeUpload={handleAvatarUpload}
                accept="image/*"
              >
                <Button
                  className="avatar-upload-button"
                  shape="circle"
                  icon={<CameraOutlined />}
                  loading={uploading}
                  size="small"
                />
              </Upload>
            </div>
            <span style={{ fontSize: 20 }}>个人资料</span>
          </Space>
        }
        extra={
          !editing && (
            <Button type="primary" onClick={() => setEditing(true)}>
              编辑资料
            </Button>
          )
        }
      >
        {!editing ? (
          <Descriptions bordered column={1}>
            <Descriptions.Item label="用户名">
              <Space>
                <UserOutlined />
                {user.username}
              </Space>
            </Descriptions.Item>
            <Descriptions.Item label="邮箱">
              <Space>
                <MailOutlined />
                {user.email}
              </Space>
            </Descriptions.Item>
            <Descriptions.Item label="电话">
              <Space>
                <PhoneOutlined />
                {user.phone || '未设置'}
              </Space>
            </Descriptions.Item>
            <Descriptions.Item label="角色">
              <Space>
                {user.roles && user.roles.length > 0 ? (
                  user.roles.map((role) => (
                    <Tag key={role.id} color={getRoleColor(role.name)}>
                      {getRoleDisplayName(role.name)}
                    </Tag>
                  ))
                ) : (
                  <Tag>无角色</Tag>
                )}
              </Space>
            </Descriptions.Item>
            <Descriptions.Item label="注册时间">
              {moment(user.createdAt).format('YYYY-MM-DD HH:mm:ss')}
            </Descriptions.Item>
            <Descriptions.Item label="最后更新">
              {moment(user.updatedAt).format('YYYY-MM-DD HH:mm:ss')}
            </Descriptions.Item>
          </Descriptions>
        ) : (
          <Form
            form={form}
            layout="vertical"
            onFinish={handleUpdate}
            style={{ maxWidth: 600 }}
          >
            <Form.Item label="用户名">
              <Input
                value={user.username}
                disabled
                prefix={<UserOutlined />}
                placeholder="用户名不可修改"
              />
            </Form.Item>

            <Form.Item
              name="email"
              label="邮箱"
              rules={[
                { required: true, message: '请输入邮箱' },
                { type: 'email', message: '请输入有效的邮箱地址' }
              ]}
            >
              <Input
                prefix={<MailOutlined />}
                placeholder="请输入邮箱"
              />
            </Form.Item>

            <Form.Item
              name="phone"
              label="电话"
              rules={[
                { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码' }
              ]}
            >
              <Input
                prefix={<PhoneOutlined />}
                placeholder="请输入电话号码（可选）"
              />
            </Form.Item>

            <Form.Item>
              <Space>
                <Button
                  type="primary"
                  htmlType="submit"
                  icon={<SaveOutlined />}
                  loading={loading}
                >
                  保存
                </Button>
                <Button
                  onClick={() => {
                    setEditing(false);
                    form.setFieldsValue({
                      email: user.email,
                      phone: user.phone || '',
                    });
                  }}
                >
                  取消
                </Button>
              </Space>
            </Form.Item>
          </Form>
        )}
      </Card>

      <Card
        title="账户安全"
        style={{ marginTop: 24 }}
      >
        <Descriptions bordered column={1}>
          <Descriptions.Item label="密码">
            <Space>
              <span>••••••••</span>
              <Button type="link" size="small">
                修改密码
              </Button>
            </Space>
          </Descriptions.Item>
          <Descriptions.Item label="账户状态">
            <Tag color="green">正常</Tag>
          </Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  );
}

export default Profile;
