package com.example.demo.collabdoc.controller;

import com.example.demo.collabdoc.dto.WebSocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 文档内容更新
    @MessageMapping("/document/{documentId}/update")
    public void handleDocumentUpdate(@DestinationVariable Long documentId, @Payload WebSocketMessage message) {
        // 广播给所有订阅该文档的用户
        messagingTemplate.convertAndSend("/topic/document/" + documentId, message);
    }

    // 用户加入文档
    @MessageMapping("/document/{documentId}/join")
    public void handleUserJoin(@DestinationVariable Long documentId, @Payload WebSocketMessage message) {
        message.setType("USER_JOIN");
        messagingTemplate.convertAndSend("/topic/document/" + documentId, message);
    }

    // 用户离开文档
    @MessageMapping("/document/{documentId}/leave")
    public void handleUserLeave(@DestinationVariable Long documentId, @Payload WebSocketMessage message) {
        message.setType("USER_LEAVE");
        messagingTemplate.convertAndSend("/topic/document/" + documentId, message);
    }

    // 实时内容更新（用户输入时）
    @MessageMapping("/document/{documentId}/content")
    public void handleContentChange(@DestinationVariable Long documentId, @Payload WebSocketMessage message) {
        message.setType("CONTENT_CHANGE");
        messagingTemplate.convertAndSend("/topic/document/" + documentId, message);
    }

    // 光标位置更新
    @MessageMapping("/document/{documentId}/cursor")
    public void handleCursorMove(@DestinationVariable Long documentId, @Payload WebSocketMessage message) {
        message.setType("CURSOR_MOVE");
        messagingTemplate.convertAndSend("/topic/document/" + documentId, message);
    }

    // 发送消息到特定文档
    public void sendToDocument(Long documentId, WebSocketMessage message) {
        messagingTemplate.convertAndSend("/topic/document/" + documentId, message);
    }
}
