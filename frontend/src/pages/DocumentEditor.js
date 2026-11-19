import React, { useState, useEffect } from 'react';
import { Input, Button, Space, message, Spin, Tabs, List, Form, Modal, Select, DatePicker, Tag } from 'antd';
import { SaveOutlined, ArrowLeftOutlined, ShareAltOutlined, UserOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getDocument, updateDocument, shareDocument } from '../services/document';
import { getDocumentComments, addComment, deleteComment } from '../services/comment';
import { getDocumentTasks, createTask, updateTaskStatus, deleteTask } from '../services/task';
import { getUserList } from '../services/user';
import websocketService from '../services/websocket';
import StorageUtil from '../utils/storage';
import moment from 'moment';
import './DocumentEditor.css';

const { TextArea } = Input;
const { TabPane } = Tabs;
const { Option } = Select;

function DocumentEditor() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [document, setDocument] = useState(null);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  
  // WebSocket 相关
  const [onlineUsers, setOnlineUsers] = useState([]);
  const [userCursors, setUserCursors] = useState({}); // 存储其他用户的光标位置
  const currentUser = StorageUtil.getUser();
  const contentChangeTimeoutRef = React.useRef(null);
  const isUpdatingFromRemote = React.useRef(false); // 标记是否正在从远程更新
  const textAreaRef = React.useRef(null); // TextArea 引用
  
  // 评论相关
  const [comments, setComments] = useState([]);
  const [commentContent, setCommentContent] = useState('');
  const [replyingTo, setReplyingTo] = useState(null); // 正在回复的评论ID
  const [replyContent, setReplyContent] = useState(''); // 回复内容
  const [replyingToUser, setReplyingToUser] = useState(null); // 正在回复的用户信息
  const [replyingToContent, setReplyingToContent] = useState(''); // 正在回复的评论内容
  
  // 任务相关
  const [tasks, setTasks] = useState([]);
  const [taskModalVisible, setTaskModalVisible] = useState(false);
  const [taskForm] = Form.useForm();

  // 分享相关
  const [shareModalVisible, setShareModalVisible] = useState(false);
  const [users, setUsers] = useState([]);
  const [shareForm] = Form.useForm();

  // 加载文档
  const loadDocument = async () => {
    setLoading(true);
    try {
      const res = await getDocument(id);
      console.log('文档加载成功：', res);
      setDocument(res.data);
      setTitle(res.data.title);
      setContent(res.data.content || '');
    } catch (error) {
      console.error('加载文档失败：', error);
      console.error('错误详情：', error.response);
      message.error('加载文档失败：' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  // 加载评论
  const loadComments = async () => {
    try {
      const res = await getDocumentComments(id);
      const allComments = res.data || [];
      
      // 构建树形结构：将评论分为顶级评论和回复
      const topLevelComments = allComments.filter(comment => !comment.parent);
      const replies = allComments.filter(comment => comment.parent);
      
      // 为每个顶级评论添加其回复
      const commentsWithReplies = topLevelComments.map(comment => ({
        ...comment,
        replies: replies.filter(reply => reply.parent.id === comment.id)
      }));
      
      setComments(commentsWithReplies);
    } catch (error) {
      console.error('加载评论失败：', error);
    }
  };

  // 加载任务
  const loadTasks = async () => {
    try {
      const res = await getDocumentTasks(id);
      setTasks(res.data || []);
    } catch (error) {
      console.error('加载任务失败：', error);
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
    loadDocument();
    loadComments();
    loadTasks();
    loadUsers();
    
    // 连接 WebSocket
    if (!websocketService.isConnected()) {
      websocketService.connect(
        () => {
          console.log('WebSocket 已连接');
          // 订阅当前文档
          subscribeToDocument();
          // 通知其他用户我加入了
          websocketService.sendUserJoin(id, currentUser.username, currentUser.id);
        },
        (error) => {
          console.error('WebSocket 连接失败:', error);
        }
      );
    } else {
      // 如果已连接，直接订阅
      subscribeToDocument();
      websocketService.sendUserJoin(id, currentUser.username, currentUser.id);
    }
    
    // 组件卸载时清理
    return () => {
      websocketService.sendUserLeave(id, currentUser.username, currentUser.id);
      websocketService.unsubscribeFromDocument(id);
    };
  }, [id]);
  
  // 订阅文档更新
  const subscribeToDocument = () => {
    websocketService.subscribeToDocument(id, (wsMessage) => {
      console.log('收到 WebSocket 消息:', wsMessage);
      
      // 忽略自己发送的消息
      if (wsMessage.userId === currentUser.id) {
        return;
      }
      
      switch (wsMessage.type) {
        case 'CONTENT_CHANGE':
          // 实时内容更新
          if (wsMessage.data && wsMessage.data.content !== undefined) {
            isUpdatingFromRemote.current = true;
            setContent(wsMessage.data.content);
            // 短暂延迟后重置标记
            setTimeout(() => {
              isUpdatingFromRemote.current = false;
            }, 100);
          }
          break;

        case 'CURSOR_MOVE':
          // 光标位置更新
          if (wsMessage.data && wsMessage.data.position !== undefined) {
            setUserCursors(prev => ({
              ...prev,
              [wsMessage.userId]: {
                username: wsMessage.username,
                position: wsMessage.data.position
              }
            }));
          }
          break;

        case 'DOCUMENT_UPDATE':
          // 文档保存更新
          message.info(`${wsMessage.username} 保存了文档`);
          loadDocument();
          break;
          
        case 'COMMENT_ADD':
          // 新评论
          message.info(`${wsMessage.username} 添加了评论`);
          loadComments();
          break;
          
        case 'TASK_ADD':
          // 新任务
          message.info(`${wsMessage.username} 创建了任务`);
          loadTasks();
          break;
          
        case 'TASK_UPDATE':
          // 任务更新
          message.info(`${wsMessage.username} 更新了任务状态`);
          loadTasks();
          break;
          
        case 'USER_JOIN':
          // 用户加入
          setOnlineUsers(prev => {
            if (!prev.find(u => u.id === wsMessage.userId)) {
              return [...prev, { id: wsMessage.userId, username: wsMessage.username }];
            }
            return prev;
          });
          message.success(`${wsMessage.username} 加入了文档`);
          break;
          
        case 'USER_LEAVE':
          // 用户离开
          setOnlineUsers(prev => prev.filter(u => u.id !== wsMessage.userId));
          setUserCursors(prev => {
            const newCursors = { ...prev };
            delete newCursors[wsMessage.userId];
            return newCursors;
          });
          message.info(`${wsMessage.username} 离开了文档`);
          break;
          
        default:
          break;
      }
    });
  };

  // 保存文档
  const handleSave = async () => {
    setSaving(true);
    try {
      await updateDocument(id, { title, content });
      message.success('保存成功');
      loadDocument();
    } catch (error) {
      console.error('保存失败：', error);
    } finally {
      setSaving(false);
    }
  };

  // 处理内容变化（带防抖）
  const handleContentChange = (e) => {
    const newContent = e.target.value;
    setContent(newContent);

    // 如果是从远程更新的，不发送 WebSocket 消息
    if (isUpdatingFromRemote.current) {
      return;
    }

    // 清除之前的定时器
    if (contentChangeTimeoutRef.current) {
      clearTimeout(contentChangeTimeoutRef.current);
    }

    // 设置新的定时器，500ms 后发送更新
    contentChangeTimeoutRef.current = setTimeout(() => {
      websocketService.sendContentChange(
        id,
        newContent,
        currentUser.username,
        currentUser.id
      );
    }, 500);
  };

  // 处理光标位置变化
  const handleCursorChange = (e) => {
    const position = e.target.selectionStart;
    const textBeforeCursor = content.substring(0, position);
    const lines = textBeforeCursor.split('\n');
    const line = lines.length - 1;
    const column = lines[lines.length - 1].length;
    
    websocketService.sendCursorPosition(
      id,
      { position, line, column },
      currentUser.username,
      currentUser.id
    );
  };

  // 计算光标的像素位置
  const calculateCursorPixelPosition = (cursorInfo) => {
    if (!cursorInfo || !cursorInfo.position) {
      return { top: 0, left: 0 };
    }

    const { line, column } = cursorInfo.position;
    
    // 每行高度约 22px（根据 TextArea 的行高）
    const lineHeight = 22;
    
    // 检查是否在浏览器环境中
    if (typeof document === 'undefined') {
      // 如果不在浏览器环境，使用简单计算
      const charWidth = 8;
      const top = line * lineHeight + 11;
      const left = column * charWidth - 29;
      return { top, left };
    }
    
    try {
      // 使用 Canvas 测量实际文本宽度（更精确）
      const canvas = document.createElement('canvas');
      const context = canvas.getContext('2d');
      
      if (!context) {
        // 如果无法获取 context，使用简单计算
        const charWidth = 8;
        const top = line * lineHeight + 11;
        const left = column * charWidth - 29;
        return { top, left };
      }
      
      context.font = '14px monospace'; // 与 TextArea 的字体一致
      
      // 获取当前行的文本
      const lines = content.split('\n');
      const currentLineText = lines[line] || '';
      const textBeforeCursor = currentLineText.substring(0, column);
      
      // 测量文本宽度
      const textWidth = context.measureText(textBeforeCursor).width;
      
      // 计算位置，加上 TextArea 的内边距
      const top = line * lineHeight + 11;
      const left = textWidth - 29;
      
      return { top, left };
    } catch (error) {
      console.error('计算光标位置出错:', error);
      // 出错时使用简单计算
      const charWidth = 8;
      const top = line * lineHeight + 11;
      const left = column * charWidth - 29;
      return { top, left };
    }
  };

  // 添加评论
  const handleAddComment = async () => {
    if (!commentContent.trim()) {
      message.warning('请输入评论内容');
      return;
    }

    try {
      const res = await addComment({
        documentId: id,
        content: commentContent,
      });
      message.success('评论添加成功');
      setCommentContent('');
      // 立即将新评论添加到列表顶部
      if (res.data) {
        setComments([res.data, ...comments]);
      } else {
        // 如果返回的数据格式不对，重新加载评论列表
        loadComments();
      }
    } catch (error) {
      console.error('添加评论失败：', error);
      message.error('添加评论失败');
    }
  };

  // 回复评论
  const handleReplyComment = async (parentId) => {
    if (!replyContent.trim()) {
      message.warning('请输入回复内容');
      return;
    }

    try {
      // 构建回复内容，包含引用信息
      const quotedContent = replyingToContent 
        ? `@${replyingToUser} 「${replyingToContent.substring(0, 50)}${replyingToContent.length > 50 ? '...' : ''}」\n${replyContent}`
        : replyContent;

      const res = await addComment({
        documentId: id,
        content: quotedContent,
        parentId: parentId,
      });
      message.success('回复成功');
      setReplyContent('');
      setReplyingTo(null);
      setReplyingToUser(null);
      setReplyingToContent('');
      // 重新加载评论列表以显示回复
      loadComments();
    } catch (error) {
      console.error('回复评论失败：', error);
      message.error('回复评论失败');
    }
  };

  // 取消回复
  const handleCancelReply = () => {
    setReplyingTo(null);
    setReplyContent('');
    setReplyingToUser(null);
    setReplyingToContent('');
  };

  // 开始回复（主评论）
  const handleStartReply = (comment) => {
    setReplyingTo(comment.id);
    setReplyingToUser(comment.user?.username || '未知用户');
    setReplyingToContent(comment.content);
    setReplyContent('');
  };

  // 开始回复（回复评论）
  const handleStartReplyToReply = (reply, parentId) => {
    setReplyingTo(reply.id);
    setReplyingToUser(reply.user?.username || '未知用户');
    setReplyingToContent(reply.content);
    setReplyContent('');
  };

  // 删除评论
  const handleDeleteComment = async (commentId) => {
    try {
      await deleteComment(commentId);
      message.success('评论删除成功');
      loadComments();
    } catch (error) {
      console.error('删除评论失败：', error);
    }
  };

  // 创建任务
  const handleCreateTask = async (values) => {
    try {
      // 格式化日期
      const taskData = {
        ...values,
        documentId: id,
      };
      
      // 如果有截止日期，转换为 ISO 8601 格式（LocalDateTime可以解析的格式）
      if (values.dueDate) {
        // 设置时间为当天的 23:59:59
        const dueDateTime = values.dueDate.hour(23).minute(59).second(59);
        taskData.dueDate = dueDateTime.format('YYYY-MM-DDTHH:mm:ss');
      }
      
      const res = await createTask(taskData);
      message.success('任务创建成功');
      setTaskModalVisible(false);
      taskForm.resetFields();
      // 立即将新任务添加到列表顶部
      if (res.data) {
        setTasks([res.data, ...tasks]);
      } else {
        // 如果返回的数据格式不对，重新加载任务列表
        loadTasks();
      }
    } catch (error) {
      console.error('创建任务失败：', error);
      message.error('创建任务失败');
    }
  };

  // 更新任务状态
  const handleUpdateTaskStatus = async (taskId, status) => {
    try {
      const res = await updateTaskStatus(taskId, status);
      message.success('任务状态更新成功');
      
      // 立即更新任务列表中的状态，而不是重新加载
      if (res.data) {
        setTasks(tasks.map(task => 
          task.id === taskId ? res.data : task
        ));
      } else {
        // 如果返回数据格式不对，重新加载
        loadTasks();
      }
    } catch (error) {
      console.error('更新任务状态失败：', error);
      message.error('更新任务状态失败');
    }
  };

  // 删除任务
  const handleDeleteTask = async (taskId) => {
    try {
      await deleteTask(taskId);
      message.success('任务删除成功');
      loadTasks();
    } catch (error) {
      console.error('删除任务失败：', error);
    }
  };

  // 分享文档
  const handleShare = async (values) => {
    try {
      await shareDocument(id, values.userId, values.permission);
      message.success('文档分享成功');
      setShareModalVisible(false);
      shareForm.resetFields();
    } catch (error) {
      console.error('分享文档失败：', error);
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div className="document-editor-container">
      <div className="editor-header">
        <Space>
          <Button 
            icon={<ArrowLeftOutlined />}
            onClick={() => navigate('/')}
          >
            返回
          </Button>
          <Input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="文档标题"
            style={{ width: 400, fontSize: 18, fontWeight: 'bold' }}
            bordered={false}
          />
          {/* 在线用户显示 */}
          {onlineUsers.length > 0 && (
            <Space style={{ marginLeft: 16 }}>
              <UserOutlined style={{ color: '#52c41a' }} />
              <span style={{ fontSize: 12, color: '#666' }}>
                在线: {onlineUsers.length + 1}
              </span>
              {onlineUsers.map(user => (
                <Tag key={user.id} color="green" style={{ margin: 0 }}>
                  {user.username}
                </Tag>
              ))}
              <Tag color="blue" style={{ margin: 0 }}>
                {currentUser.username} (我)
              </Tag>
            </Space>
          )}
        </Space>
        <Space>
          <Button 
            icon={<ShareAltOutlined />}
            onClick={() => setShareModalVisible(true)}
          >
            分享
          </Button>
          <Button 
            type="primary" 
            icon={<SaveOutlined />}
            onClick={handleSave}
            loading={saving}
          >
            保存
          </Button>
        </Space>
      </div>

      <div className="editor-content">
        <Tabs defaultActiveKey="1">
          <TabPane tab="编辑" key="1">
            <div style={{ position: 'relative' }}>
              {/* 显示其他用户的光标位置 */}
              {Object.entries(userCursors).map(([userId, cursorInfo]) => {
                const { top, left } = calculateCursorPixelPosition(cursorInfo);
                return (
                  <div
                    key={userId}
                    style={{
                      position: 'absolute',
                      top: `${top}px`,
                      left: `${left}px`,
                      width: '2px',
                      height: '20px',
                      backgroundColor: '#1890ff',
                      zIndex: 1000,
                      pointerEvents: 'none',
                      animation: 'blink 1s infinite'
                    }}
                  >
                    <div
                      style={{
                        position: 'absolute',
                        top: '-22px',
                        left: '0',
                        backgroundColor: '#1890ff',
                        color: 'white',
                        padding: '2px 6px',
                        borderRadius: '3px',
                        fontSize: '12px',
                        whiteSpace: 'nowrap'
                      }}
                    >
                      {cursorInfo.username}
                    </div>
                  </div>
                );
              })}
              
              <TextArea
                ref={textAreaRef}
                value={content}
                onChange={handleContentChange}
                onSelect={handleCursorChange}
                onClick={handleCursorChange}
                onKeyUp={handleCursorChange}
                placeholder="开始编辑文档..."
                autoSize={{ minRows: 20 }}
                style={{ fontSize: 14, lineHeight: '22px', fontFamily: 'monospace' }}
              />
            </div>
          </TabPane>

          <TabPane tab="评论" key="2">
            <div className="comment-section">
              <div className="comment-input">
                <TextArea
                  value={commentContent}
                  onChange={(e) => setCommentContent(e.target.value)}
                  placeholder="添加评论..."
                  rows={3}
                />
                <Button 
                  type="primary" 
                  onClick={handleAddComment}
                  style={{ marginTop: 8 }}
                >
                  发表评论
                </Button>
              </div>

              <List
                className="comment-list"
                dataSource={comments}
                renderItem={(item) => (
                  <div key={item.id} style={{ marginBottom: 16 }}>
                    {/* 主评论 */}
                    <List.Item
                      actions={[
                        <a onClick={() => handleStartReply(item)}>回复</a>,
                        <a onClick={() => handleDeleteComment(item.id)}>删除</a>
                      ]}
                    >
                      <div style={{ width: '100%' }}>
                        <List.Item.Meta
                          title={item.user?.username || '未知用户'}
                          description={
                            <>
                              <div>{item.content}</div>
                              <div style={{ fontSize: 12, color: '#999', marginTop: 4 }}>
                                {moment(item.createdAt).format('YYYY-MM-DD HH:mm:ss')}
                              </div>
                            </>
                          }
                        />
                        
                        {/* 回复输入框 */}
                        {replyingTo === item.id && (
                          <div style={{ marginTop: 12, paddingLeft: 24 }}>
                            <div style={{ 
                              marginBottom: 8, 
                              padding: '6px 10px',
                              background: '#e6f7ff',
                              borderLeft: '3px solid #1890ff',
                              fontSize: 12,
                              color: '#666'
                            }}>
                              回复 @{replyingToUser}: {replyingToContent.substring(0, 50)}
                              {replyingToContent.length > 50 ? '...' : ''}
                            </div>
                            <TextArea
                              value={replyContent}
                              onChange={(e) => setReplyContent(e.target.value)}
                              placeholder={`回复 ${replyingToUser}...`}
                              rows={2}
                              autoFocus
                            />
                            <Space style={{ marginTop: 8 }}>
                              <Button 
                                type="primary" 
                                size="small"
                                onClick={() => handleReplyComment(item.id)}
                              >
                                发送回复
                              </Button>
                              <Button 
                                size="small"
                                onClick={handleCancelReply}
                              >
                                取消
                              </Button>
                            </Space>
                          </div>
                        )}
                      </div>
                    </List.Item>

                    {/* 回复列表 */}
                    {item.replies && item.replies.length > 0 && (
                      <div style={{ 
                        marginLeft: 48, 
                        marginTop: 8,
                        paddingLeft: 16,
                        borderLeft: '2px solid #f0f0f0'
                      }}>
                        {item.replies.map(reply => (
                          <div 
                            key={reply.id} 
                            style={{ 
                              marginBottom: 12,
                              padding: '8px 12px',
                              background: '#fafafa',
                              borderRadius: 4
                            }}
                          >
                            <div style={{ 
                              display: 'flex', 
                              justifyContent: 'space-between',
                              alignItems: 'flex-start'
                            }}>
                              <div style={{ flex: 1 }}>
                                <div style={{ 
                                  fontWeight: 500, 
                                  marginBottom: 4,
                                  color: '#1890ff'
                                }}>
                                  {reply.user?.username || '未知用户'}
                                </div>
                                <div style={{ marginBottom: 4 }}>
                                  {/* 解析并显示引用内容 */}
                                  {reply.content.includes('@') && reply.content.includes('「') ? (
                                    <>
                                      <div style={{
                                        padding: '4px 8px',
                                        background: '#f0f0f0',
                                        borderLeft: '2px solid #d9d9d9',
                                        marginBottom: 8,
                                        fontSize: 12,
                                        color: '#666'
                                      }}>
                                        {reply.content.split('\n')[0]}
                                      </div>
                                      <div>
                                        {reply.content.split('\n').slice(1).join('\n')}
                                      </div>
                                    </>
                                  ) : (
                                    reply.content
                                  )}
                                </div>
                                <div style={{ fontSize: 12, color: '#999' }}>
                                  {moment(reply.createdAt).format('YYYY-MM-DD HH:mm:ss')}
                                </div>
                              </div>
                              <Space style={{ marginLeft: 12, fontSize: 12 }}>
                                <a onClick={() => handleStartReplyToReply(reply, item.id)}>回复</a>
                                <a onClick={() => handleDeleteComment(reply.id)}>删除</a>
                              </Space>
                            </div>
                            
                            {/* 回复的回复输入框 */}
                            {replyingTo === reply.id && (
                              <div style={{ marginTop: 12 }}>
                                <div style={{ 
                                  marginBottom: 8, 
                                  padding: '6px 10px',
                                  background: '#e6f7ff',
                                  borderLeft: '3px solid #1890ff',
                                  fontSize: 12,
                                  color: '#666'
                                }}>
                                  回复 @{replyingToUser}: {replyingToContent.substring(0, 50)}
                                  {replyingToContent.length > 50 ? '...' : ''}
                                </div>
                                <TextArea
                                  value={replyContent}
                                  onChange={(e) => setReplyContent(e.target.value)}
                                  placeholder={`回复 ${replyingToUser}...`}
                                  rows={2}
                                  autoFocus
                                />
                                <Space style={{ marginTop: 8 }}>
                                  <Button 
                                    type="primary" 
                                    size="small"
                                    onClick={() => handleReplyComment(item.id)}
                                  >
                                    发送回复
                                  </Button>
                                  <Button 
                                    size="small"
                                    onClick={handleCancelReply}
                                  >
                                    取消
                                  </Button>
                                </Space>
                              </div>
                            )}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}
              />
            </div>
          </TabPane>

          <TabPane tab="任务" key="3">
            <div className="task-section">
              <Button 
                type="primary" 
                onClick={() => setTaskModalVisible(true)}
                style={{ marginBottom: 16 }}
              >
                新建任务
              </Button>

              <List
                dataSource={tasks}
                renderItem={(item) => {
                  // 检查当前用户是否有权限修改任务状态
                  // 只有文档所有者或任务创建者可以修改
                  const currentUserId = StorageUtil.getUser().id;
                  const isDocumentOwner = document?.owner?.id === currentUserId;
                  const isTaskCreator = item.createdBy?.id === currentUserId;
                  const canEditTask = isDocumentOwner || isTaskCreator;
                  
                  return (
                    <List.Item
                      actions={[
                        <Select
                          value={item.status}
                          onChange={(value) => handleUpdateTaskStatus(item.id, value)}
                          style={{ width: 120 }}
                          disabled={!canEditTask}
                        >
                          <Option value="TODO">待办</Option>
                          <Option value="IN_PROGRESS">进行中</Option>
                          <Option value="COMPLETED">已完成</Option>
                          <Option value="CANCELLED">已取消</Option>
                        </Select>,
                        canEditTask ? (
                          <a onClick={() => handleDeleteTask(item.id)}>删除</a>
                        ) : (
                          <span style={{ color: '#ccc' }}>删除</span>
                        )
                      ]}
                    >
                      <List.Item.Meta
                        title={
                          <span>
                            {item.title}
                            {!canEditTask && (
                              <span style={{ 
                                marginLeft: 8, 
                                fontSize: 12, 
                                color: '#999',
                                background: '#f0f0f0',
                                padding: '2px 6px',
                                borderRadius: 3
                              }}>
                                只读
                              </span>
                            )}
                          </span>
                        }
                        description={
                          <>
                            <div>{item.description}</div>
                            <div style={{ fontSize: 12, color: '#999', marginTop: 4 }}>
                              创建者: {item.createdBy?.username || '未知'}
                            </div>
                            <div style={{ fontSize: 12, color: '#999', marginTop: 4 }}>
                              截止日期: {item.dueDate ? moment(item.dueDate).format('YYYY-MM-DD') : '无'}
                            </div>
                          </>
                        }
                      />
                    </List.Item>
                  );
                }}
              />
            </div>
          </TabPane>
        </Tabs>
      </div>

      <Modal
        title="新建任务"
        open={taskModalVisible}
        onCancel={() => {
          setTaskModalVisible(false);
          taskForm.resetFields();
        }}
        footer={null}
      >
        <Form
          form={taskForm}
          onFinish={handleCreateTask}
          layout="vertical"
        >
          <Form.Item
            name="title"
            label="任务标题"
            rules={[{ required: true, message: '请输入任务标题' }]}
          >
            <Input placeholder="请输入任务标题" />
          </Form.Item>

          <Form.Item
            name="description"
            label="任务描述"
          >
            <TextArea rows={4} placeholder="请输入任务描述" />
          </Form.Item>

          <Form.Item
            name="dueDate"
            label="截止日期"
          >
            <DatePicker 
              style={{ width: '100%' }}
              placeholder="选择截止日期"
              format="YYYY-MM-DD"
              disabledDate={(current) => {
                // 禁用今天之前的日期
                return current && current < moment().startOf('day');
              }}
            />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                创建
              </Button>
              <Button onClick={() => {
                setTaskModalVisible(false);
                taskForm.resetFields();
              }}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* 分享文档对话框 */}
      <Modal
        title={`分享文档 - ${title}`}
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

export default DocumentEditor;
