import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Modal, Select, message, Tag, Card } from 'antd';
import { UserOutlined, CrownOutlined, EditOutlined } from '@ant-design/icons';
import { getAllUsers, assignRole, removeRole } from '../services/user';
import './AdminPanel.css';

const { Option } = Select;

function AdminPanel() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [roleModalVisible, setRoleModalVisible] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [selectedRoles, setSelectedRoles] = useState([]);

  // 加载所有用户
  const loadUsers = async () => {
    setLoading(true);
    try {
      const res = await getAllUsers();
      setUsers(res.data || []);
    } catch (error) {
      console.error('加载用户失败：', error);
      message.error('加载用户失败，请确认您有管理员权限');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  // 打开角色管理弹窗
  const handleManageRoles = (user) => {
    setSelectedUser(user);
    const roleNames = user.roles?.map(role => role.name) || [];
    setSelectedRoles(roleNames);
    setRoleModalVisible(true);
  };

  // 保存角色变更
  const handleSaveRoles = async () => {
    if (!selectedUser) return;

    try {
      const currentRoles = selectedUser.roles?.map(role => role.name) || [];
      
      // 找出需要添加的角色
      const rolesToAdd = selectedRoles.filter(role => !currentRoles.includes(role));
      
      // 找出需要移除的角色
      const rolesToRemove = currentRoles.filter(role => !selectedRoles.includes(role));

      // 添加新角色
      for (const role of rolesToAdd) {
        await assignRole(selectedUser.id, role);
      }

      // 移除旧角色
      for (const role of rolesToRemove) {
        await removeRole(selectedUser.id, role);
      }

      message.success('角色更新成功');
      setRoleModalVisible(false);
      loadUsers();
    } catch (error) {
      console.error('更新角色失败：', error);
      message.error('更新角色失败');
    }
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

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      render: (text) => (
        <Space>
          <UserOutlined />
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: '电话',
      dataIndex: 'phone',
      key: 'phone',
      render: (text) => text || '-',
    },
    {
      title: '角色',
      dataIndex: 'roles',
      key: 'roles',
      render: (roles) => (
        <Space>
          {roles && roles.length > 0 ? (
            roles.map((role) => (
              <Tag key={role.id} color={getRoleColor(role.name)}>
                {getRoleDisplayName(role.name)}
              </Tag>
            ))
          ) : (
            <Tag>无角色</Tag>
          )}
        </Space>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (text) => new Date(text).toLocaleString(),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Button
          type="primary"
          icon={<EditOutlined />}
          onClick={() => handleManageRoles(record)}
        >
          管理角色
        </Button>
      ),
    },
  ];

  return (
    <div className="admin-panel-container">
      <Card
        title={
          <Space>
            <CrownOutlined style={{ fontSize: 24, color: '#faad14' }} />
            <span style={{ fontSize: 20 }}>管理员控制面板</span>
          </Space>
        }
        extra={
          <Button type="primary" onClick={loadUsers}>
            刷新
          </Button>
        }
      >
        <Table
          columns={columns}
          dataSource={users}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </Card>

      <Modal
        title={`管理用户角色 - ${selectedUser?.username}`}
        open={roleModalVisible}
        onOk={handleSaveRoles}
        onCancel={() => {
          setRoleModalVisible(false);
          setSelectedUser(null);
        }}
        okText="保存"
        cancelText="取消"
      >
        <div style={{ marginBottom: 16 }}>
          <p>当前用户：<strong>{selectedUser?.username}</strong></p>
          <p>邮箱：{selectedUser?.email}</p>
        </div>

        <div>
          <p style={{ marginBottom: 8 }}>选择角色：</p>
          <Select
            mode="multiple"
            style={{ width: '100%' }}
            placeholder="请选择角色"
            value={selectedRoles}
            onChange={setSelectedRoles}
          >
            <Option value="ROLE_ADMIN">
              <Tag color="red">管理员</Tag>
              <span style={{ marginLeft: 8 }}>拥有所有权限</span>
            </Option>
            <Option value="ROLE_EDITOR">
              <Tag color="blue">编辑者</Tag>
              <span style={{ marginLeft: 8 }}>可以编辑文档</span>
            </Option>
            <Option value="ROLE_VIEWER">
              <Tag color="green">查看者</Tag>
              <span style={{ marginLeft: 8 }}>只能查看文档</span>
            </Option>
          </Select>
        </div>

        <div style={{ marginTop: 16, padding: 12, background: '#f0f2f5', borderRadius: 4 }}>
          <p style={{ margin: 0, fontSize: 12, color: '#666' }}>
            <strong>提示：</strong>
          </p>
          <ul style={{ margin: '8px 0 0 0', paddingLeft: 20, fontSize: 12, color: '#666' }}>
            <li>管理员可以管理所有用户和角色</li>
            <li>编辑者可以创建和编辑文档</li>
            <li>查看者只能查看文档，不能编辑</li>
            <li>一个用户可以拥有多个角色</li>
          </ul>
        </div>
      </Modal>
    </div>
  );
}

export default AdminPanel;
