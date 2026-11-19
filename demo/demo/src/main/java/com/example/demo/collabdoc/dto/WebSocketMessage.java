package com.example.demo.collabdoc.dto;

public class WebSocketMessage {
    private String type; // DOCUMENT_UPDATE, COMMENT_ADD, TASK_UPDATE, USER_JOIN, USER_LEAVE
    private Long documentId;
    private Object data;
    private String username;
    private Long userId;

    public WebSocketMessage() {
    }

    public WebSocketMessage(String type, Long documentId, Object data, String username, Long userId) {
        this.type = type;
        this.documentId = documentId;
        this.data = data;
        this.username = username;
        this.userId = userId;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
