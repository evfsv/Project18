package com.example.demo.collabdoc.service;

import com.example.demo.collabdoc.entity.Notification;
import com.example.demo.collabdoc.entity.User;
import com.example.demo.collabdoc.repository.NotificationRepository;
import com.example.demo.collabdoc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Notification createNotification(Long userId, Notification.NotificationType type, 
                                          String title, String content, Long relatedId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification createNotificationWithDetails(Long userId, Notification.NotificationType type, 
                                                     String title, String content, 
                                                     Long documentId, Long commentId, Long taskId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setDocumentId(documentId);
        notification.setCommentId(commentId);
        notification.setTaskId(taskId);
        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification createNotificationWithFullDetails(Long userId, Notification.NotificationType type, 
                                                         String title, String content, 
                                                         Long documentId, String documentTitle,
                                                         Long commentId, String commentContent,
                                                         Long taskId, String taskTitle) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setDocumentId(documentId);
        notification.setDocumentTitle(documentTitle);
        notification.setCommentId(commentId);
        notification.setCommentContent(commentContent);
        notification.setTaskId(taskId);
        notification.setTaskTitle(taskTitle);
        notification.setIsRead(false);

        Notification savedNotification = notificationRepository.save(notification);
        
        System.out.println("=== 准备发送 WebSocket 通知 ===");
        System.out.println("接收者用户ID: " + userId);
        System.out.println("通知标题: " + title);
        System.out.println("通知内容: " + content);
        System.out.println("目标路径: /user/" + userId + "/queue/notifications");
        
        try {
            // 通过 WebSocket 发送实时通知
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                savedNotification
            );
            System.out.println("✅ WebSocket 通知发送成功");
        } catch (Exception e) {
            System.err.println("❌ WebSocket 通知发送失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return savedNotification;
    }

    public void createCommentNotification(Long userId, Long commenterId, Long documentId) {
        createNotificationWithDetails(userId, Notification.NotificationType.COMMENT_ADDED,
                "新评论", "您的文档收到了新评论", documentId, null, null);
    }

    public void createCommentNotificationWithCommentId(Long userId, Long commenterId, Long documentId, Long commentId) {
        createNotificationWithDetails(userId, Notification.NotificationType.COMMENT_ADDED,
                "新评论", "您的文档收到了新评论", documentId, commentId, null);
    }

    public void createCommentNotificationFull(Long userId, Long commenterId, 
                                             Long documentId, String documentTitle,
                                             Long commentId, String commentContent) {
        createNotificationWithFullDetails(userId, Notification.NotificationType.COMMENT_ADDED,
                "新评论", "您的文档收到了新评论", 
                documentId, documentTitle, commentId, commentContent, null, null);
    }

    public void createCommentReplyNotification(Long userId, Long replierId, Long documentId) {
        createNotificationWithDetails(userId, Notification.NotificationType.COMMENT_REPLY,
                "评论回复", "有人回复了您的评论", documentId, null, null);
    }

    public void createCommentReplyNotificationWithCommentId(Long userId, Long replierId, Long documentId, Long commentId) {
        createNotificationWithDetails(userId, Notification.NotificationType.COMMENT_REPLY,
                "评论回复", "有人回复了您的评论", documentId, commentId, null);
    }

    public void createCommentReplyNotificationFull(Long userId, Long replierId, 
                                                  Long documentId, String documentTitle,
                                                  Long commentId, String commentContent) {
        createNotificationWithFullDetails(userId, Notification.NotificationType.COMMENT_REPLY,
                "评论回复", "有人回复了您的评论", 
                documentId, documentTitle, commentId, commentContent, null, null);
    }

    public void createMentionNotification(Long userId, Long mentionerId, Long documentId) {
        createNotificationWithDetails(userId, Notification.NotificationType.MENTION,
                "提及通知", "有人在评论中@了您", documentId, null, null);
    }

    public void createTaskAssignedNotification(Long userId, Long assignerId, Long taskId) {
        createNotificationWithDetails(userId, Notification.NotificationType.TASK_ASSIGNED,
                "任务分配", "您被分配了一个新任务", null, null, taskId);
    }

    public void createTaskCompletedNotification(Long userId, Long completerId, Long taskId) {
        createNotificationWithDetails(userId, Notification.NotificationType.TASK_COMPLETED,
                "任务完成", "您创建的任务已完成", null, null, taskId);
    }

    public void createDocumentSharedNotification(Long userId, Long sharerId, Long documentId) {
        createNotificationWithDetails(userId, Notification.NotificationType.DOCUMENT_SHARED,
                "文档分享", "有人与您分享了文档", documentId, null, null);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        
        // 主动加载关联数据，避免懒加载问题
        notifications.forEach(notification -> {
            if (notification.getUser() != null) {
                notification.getUser().getUsername();
                if (notification.getUser().getRoles() != null) {
                    notification.getUser().getRoles().size();
                }
            }
        });
        
        return notifications;
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        List<Notification> notifications = notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
        
        // 主动加载关联数据，避免懒加载问题
        notifications.forEach(notification -> {
            if (notification.getUser() != null) {
                notification.getUser().getUsername();
                if (notification.getUser().getRoles() != null) {
                    notification.getUser().getRoles().size();
                }
            }
        });
        
        return notifications;
    }

    public Long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return notificationRepository.countByUserAndIsRead(user, false);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        List<Notification> notifications = notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }
}
