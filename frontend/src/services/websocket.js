import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

class WebSocketService {
  constructor() {
    this.stompClient = null;
    this.subscriptions = {};
    this.connected = false;
  }

  connect(onConnected, onError) {
    const socket = new SockJS('http://localhost:8080/ws');
    this.stompClient = Stomp.over(socket);
    
    // 禁用调试日志
    this.stompClient.debug = () => {};

    this.stompClient.connect(
      {},
      (frame) => {
        console.log('WebSocket 连接成功:', frame);
        this.connected = true;
        if (onConnected) onConnected();
      },
      (error) => {
        console.error('WebSocket 连接失败:', error);
        this.connected = false;
        if (onError) onError(error);
      }
    );
  }

  disconnect() {
    if (this.stompClient && this.connected) {
      this.stompClient.disconnect(() => {
        console.log('WebSocket 断开连接');
        this.connected = false;
      });
    }
  }

  // 订阅文档更新
  subscribeToDocument(documentId, callback) {
    if (!this.stompClient || !this.connected) {
      console.error('WebSocket 未连接');
      return null;
    }

    const subscription = this.stompClient.subscribe(
      `/topic/document/${documentId}`,
      (message) => {
        const data = JSON.parse(message.body);
        callback(data);
      }
    );

    this.subscriptions[documentId] = subscription;
    return subscription;
  }

  // 取消订阅文档
  unsubscribeFromDocument(documentId) {
    if (this.subscriptions[documentId]) {
      this.subscriptions[documentId].unsubscribe();
      delete this.subscriptions[documentId];
    }
  }

  // 发送用户加入文档的消息
  sendUserJoin(documentId, username, userId) {
    if (this.stompClient && this.connected) {
      this.stompClient.send(
        `/app/document/${documentId}/join`,
        {},
        JSON.stringify({
          type: 'USER_JOIN',
          documentId,
          username,
          userId
        })
      );
    }
  }

  // 发送用户离开文档的消息
  sendUserLeave(documentId, username, userId) {
    if (this.stompClient && this.connected) {
      this.stompClient.send(
        `/app/document/${documentId}/leave`,
        {},
        JSON.stringify({
          type: 'USER_LEAVE',
          documentId,
          username,
          userId
        })
      );
    }
  }

  // 发送实时内容变化
  sendContentChange(documentId, content, username, userId) {
    if (this.stompClient && this.connected) {
      this.stompClient.send(
        `/app/document/${documentId}/content`,
        {},
        JSON.stringify({
          type: 'CONTENT_CHANGE',
          documentId,
          data: { content },
          username,
          userId
        })
      );
    }
  }

  // 发送光标位置
  sendCursorPosition(documentId, position, username, userId) {
    if (this.stompClient && this.connected) {
      this.stompClient.send(
        `/app/document/${documentId}/cursor`,
        {},
        JSON.stringify({
          type: 'CURSOR_MOVE',
          documentId,
          data: { position },
          username,
          userId
        })
      );
    }
  }

  isConnected() {
    return this.connected;
  }
}

// 创建单例
const websocketService = new WebSocketService();
export default websocketService;
