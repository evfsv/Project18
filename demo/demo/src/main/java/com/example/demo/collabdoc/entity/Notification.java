package com.example.demo.collabdoc.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "related_id")
    private Long relatedId;  // 关联的文档、评论、任务等的ID

    @Column(name = "document_id")
    private Long documentId;  // 关联的文档ID

    @Column(name = "document_title")
    private String documentTitle;  // 文档标题

    @Column(name = "comment_id")
    private Long commentId;  // 关联的评论ID（如果是评论相关通知）

    @Column(name = "comment_content")
    private String commentContent;  // 评论内容

    @Column(name = "task_id")
    private Long taskId;  // 关联的任务ID（如果是任务相关通知）

    @Column(name = "task_title")
    private String taskTitle;  // 任务标题

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum NotificationType {
        DOCUMENT_SHARED,   // 文档被分享
        DOCUMENT_EDITED,   // 文档被编辑
        COMMENT_ADDED,     // 新评论
        COMMENT_REPLY,     // 评论回复
        TASK_ASSIGNED,     // 任务分配
        TASK_COMPLETED,    // 任务完成
        MENTION,           // @提及
        SYSTEM             // 系统通知
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
