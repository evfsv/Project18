import React, { useState, useEffect } from 'react';
import { Table, Button, Input, Space, Modal, Form, message, Popconfirm, Select, Tabs } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined, ShareAltOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getUserDocuments, getSharedDocuments, createDocument, deleteDocument, searchDocuments, shareDocument } from '../services/document';
import { getUserList } from '../services/user';
import moment from 'moment';
import './DocumentList.css';

const { Search } = Input;
const { Option } = Select;

function DocumentList() {
  const navigate = useNavigate();
  const [documents, setDocuments] = useState([]);
  const [sharedDocuments, setSharedDocuments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [shareModalVisible, setShareModalVisible] = useState(false);
  const [selectedDocument, setSelectedDocument] = useState(null);
  const [users, setUsers] = useState([]);
  const [activeTab, setActiveTab] = useState('my');
  const [form] = Form.useForm();
  const [shareForm] = Form.useForm();

  // 加载我的文档列表
  const loadDocuments = async () => {
    setLoading(true);
    try {
      const res = await getUserDocuments();
      setDocuments(res.data || []);
    } catch (error) {
      console.error('加载文档失败：', error);
    } finally {
      setLoading(false);
    }
  };

  // 加载共享文档列表
  const loadSharedDocuments = async () => {
    setLoading(true);
    try {
      const res = await getSharedDocuments();
      setSharedDocuments(res.data || []);
    } catch (error) {
      console.error('加载共享文档失败：', error);
    } finally {
      setLoading(false);
    }
  };

  // 加载用户列表
  const loadUsers = async () => {
    try {
      const res = await getUserList();
      setUsers(res.data || []);
    } catch (error) {
      console.error('加载用户列表失败：', error);
    }
  };

  useEffect(() => {
    loadDocuments();
    loadSharedDocuments();
    loadUsers();
  }, []);

  // 创建文档
  const handleCreate = async (values) => {
    try {
      await createDocument(values);
      message.success('文档创建成功');
      setCreateModalVisible(false);
      form.resetFields();
      loadDocuments();
    } catch (error) {
      console.error('创建文档失败：', error);
    }
  };

  // 删除文档
  const handleDelete = async (id) => {
    try {
      await deleteDocument(id);
      message.success('文档删除成功');
      loadDocuments();
    } catch (error) {
      console.error('删除文档失败：', error);
    }
  };

  // 搜索文档
  const handleSearch = async (value) => {
    if (!value.trim()) {
      loadDocuments();
      return;
    }
    
    setLoading(true);
    try {
      const res = await searchDocuments(value);
      setDocuments(res.data || []);
    } catch (error) {
      console.error('搜索文档失败：', error);
    } finally {
      setLoading(false);
    }
  };

  // 编辑文档
  const handleEdit = (id) => {
    navigate(`/document/${id}`);
  };

  // 打开分享对话框
  const handleOpenShare = (document) => {
    setSelectedDocument(document);
    setShareModalVisible(true);
    shareForm.resetFields();
  };

  // 分享文档
  const handleShare = async (values) => {
    try {
      await shareDocument(selectedDocument.id, values.userId, values.permission);
      message.success('文档分享成功');
      setShareModalVisible(false);
      shareForm.resetFields();
    } catch (error) {
      console.error('分享文档失败：', error);
    }
  };

  const columns = [
    {
      title: '文档标题',
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (
        <a onClick={() => handleEdit(record.id)}>{text}</a>
      ),
    },
    {
      title: '创建者',
      dataIndex: 'owner',
      key: 'owner',
      render: (owner) => owner?.username || '未知',
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      render: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="middle">
          <Button 
            type="link" 
            icon={<EditOutlined />}
            onClick={() => handleEdit(record.id)}
          >
            编辑
          </Button>
          <Button 
            type="link" 
            icon={<ShareAltOutlined />}
            onClick={() => handleOpenShare(record)}
          >
            分享
          </Button>
          <Popconfirm
            title="确定要删除这个文档吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const sharedColumns = [
    {
      title: '文档标题',
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (
        <a onClick={() => handleEdit(record.id)}>{text}</a>
      ),
    },
    {
      title: '所有者',
      dataIndex: 'owner',
      key: 'owner',
      render: (owner) => owner?.username || '未知',
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      render: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space size="middle">
          <Button 
            type="link" 
            icon={<EditOutlined />}
            onClick={() => handleEdit(record.id)}
          >
            查看/编辑
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div className="document-list-container">
      <div className="document-list-header">
        <h2>文档管理</h2>
        <Space>
          <Search
            placeholder="搜索文档"
            allowClear
            onSearch={handleSearch}
            style={{ width: 300 }}
          />
          <Button 
            type="primary" 
            icon={<PlusOutlined />}
            onClick={() => setCreateModalVisible(true)}
          >
            新建文档
          </Button>
        </Space>
      </div>

      <Tabs 
        activeKey={activeTab} 
        onChange={setActiveTab}
        items={[
          {
            key: 'my',
            label: '我的文档',
            children: (
              <Table
                columns={columns}
                dataSource={documents}
                rowKey="id"
                loading={loading}
                pagination={{ pageSize: 10 }}
              />
            ),
          },
          {
            key: 'shared',
            label: '与我共享',
            children: (
              <Table
                columns={sharedColumns}
                dataSource={sharedDocuments}
                rowKey="id"
                loading={loading}
                pagination={{ pageSize: 10 }}
              />
            ),
          },
        ]}
      />

      {/* 创建文档对话框 */}
      <Modal
        title="新建文档"
        open={createModalVisible}
        onCancel={() => {
          setCreateModalVisible(false);
          form.resetFields();
        }}
        footer={null}
      >
        <Form
          form={form}
          onFinish={handleCreate}
          layout="vertical"
        >
          <Form.Item
            name="title"
            label="文档标题"
            rules={[{ required: true, message: '请输入文档标题' }]}
          >
            <Input placeholder="请输入文档标题" />
          </Form.Item>

          <Form.Item
            name="content"
            label="文档内容"
          >
            <Input.TextArea 
              rows={6} 
              placeholder="请输入文档内容"
            />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                创建
              </Button>
              <Button onClick={() => {
                setCreateModalVisible(false);
                form.resetFields();
              }}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 分享文档对话框 */}
      <Modal
        title={`分享文档 - ${selectedDocument?.title}`}
        open={shareModalVisible}
        onCancel={() => {
          setShareModalVisible(false);
          shareForm.resetFields();
        }}
        footer={null}
      >
        <Form
          form={shareForm}
          onFinish={handleShare}
          layout="vertical"
        >
          <Form.Item
            name="userId"
            label="选择用户"
            rules={[{ required: true, message: '请选择要分享的用户' }]}
          >
            <Select
              placeholder="请选择用户"
              showSearch
              filterOption={(input, option) =>
                option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
              }
            >
              {users.map(user => (
                <Option key={user.id} value={user.id}>
                  {user.username} ({user.email})
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="permission"
            label="权限类型"
            rules={[{ required: true, message: '请选择权限类型' }]}
            initialValue="VIEWER"
          >
            <Select placeholder="请选择权限类型">
              <Option value="VIEWER">只读（查看）</Option>
              <Option value="EDITOR">读写（编辑）</Option>
            </Select>
          </Form.Item>

          <Form.Item>
            <div style={{ marginBottom: 16, padding: 12, background: '#f0f2f5', borderRadius: 4 }}>
              <p style={{ margin: 0, fontSize: 12, color: '#666' }}>
                <strong>权限说明：</strong>
              </p>
              <ul style={{ margin: '8px 0 0 0', paddingLeft: 20, fontSize: 12, color: '#666' }}>
                <li><strong>只读</strong>：用户只能查看文档内容，不能编辑</li>
                <li><strong>读写</strong>：用户可以查看和编辑文档内容</li>
              </ul>
            </div>
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<ShareAltOutlined />}>
                分享
              </Button>
              <Button onClick={() => {
                setShareModalVisible(false);
                shareForm.resetFields();
              }}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default DocumentList;
