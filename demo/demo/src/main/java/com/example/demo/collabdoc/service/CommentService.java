package com.example.demo.collabdoc.service;

import com.example.demo.collabdoc.controller.WebSocketController;
import com.example.demo.collabdoc.dto.CommentRequest;
import com.example.demo.collabdoc.dto.WebSocketMessage;
import com.example.demo.collabdoc.entity.Comment;
import com.example.demo.collabdoc.entity.Document;
import com.example.demo.collabdoc.entity.User;
import com.example.demo.collabdoc.repository.CommentRepository;
import com.example.demo.collabdoc.repository.DocumentRepository;
import com.example.demo.collabdoc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private WebSocketController webSocketController;

    @Transactional
    public Comment addComment(CommentRequest request, Long userId) {
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Comment comment = new Comment();
        comment.setDocument(document);
        comment.setUser(user);
        comment.setContent(request.getContent());
        comment.setPositionStart(request.getPositionStart());
        comment.setPositionEnd(request.getPositionEnd());
        comment.setMentionedUserId(request.getMentionedUserId());

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("父评论不存在"));
            comment.setParent(parent);
        }

        Comment savedComment = commentRepository.save(comment);

        // 发送通知（包含文档标题和评论内容）
        String documentTitle = document.getTitle();
        String commentContentText = savedComment.getContent();
        // 截取评论内容前50个字符
        if (commentContentText.length() > 50) {
            commentContentText = commentContentText.substring(0, 50) + "...";
        }

        if (request.getMentionedUserId() != null) {
            notificationService.createMentionNotification(request.getMentionedUserId(), userId, document.getId());
        }

        if (request.getParentId() != null) {
            Comment parent = comment.getParent();
            // 回复通知，包含文档标题和评论内容
            notificationService.createCommentReplyNotificationFull(
                parent.getUser().getId(), userId, 
                document.getId(), documentTitle,
                savedComment.getId(), commentContentText);
        } else {
            // 新评论通知，包含文档标题和评论内容
            notificationService.createCommentNotificationFull(
                document.getOwner().getId(), userId, 
                document.getId(), documentTitle,
                savedComment.getId(), commentContentText);
        }

        // 主动加载关联数据，避免懒加载问题
        if (savedComment.getUser() != null) {
            savedComment.getUser().getUsername();
            // 加载用户的roles
            if (savedComment.getUser().getRoles() != null) {
                savedComment.getUser().getRoles().size();
            }
        }
        if (savedComment.getDocument() != null) {
            savedComment.getDocument().getTitle();
            // 加载文档的tags集合
            if (savedComment.getDocument().getTags() != null) {
                savedComment.getDocument().getTags().size();
            }
            // 加载文档的owner
            if (savedComment.getDocument().getOwner() != null) {
                savedComment.getDocument().getOwner().getUsername();
                if (savedComment.getDocument().getOwner().getRoles() != null) {
                    savedComment.getDocument().getOwner().getRoles().size();
                }
            }
            // 加载文档的folder
            if (savedComment.getDocument().getFolder() != null) {
                savedComment.getDocument().getFolder().getName();
            }
        }
        if (savedComment.getParent() != null) {
            savedComment.getParent().getContent();
        }
        
        // 通过 WebSocket 通知其他用户有新评论
        WebSocketMessage wsMessage = new WebSocketMessage(
            "COMMENT_ADD",
            document.getId(),
            savedComment,
            user.getUsername(),
            userId
        );
        webSocketController.sendToDocument(document.getId(), wsMessage);

        return savedComment;
    }

    @Transactional(readOnly = true)
    public List<Comment> getDocumentComments(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        List<Comment> comments = commentRepository.findByDocumentOrderByCreatedAtDesc(document);
        
        // 主动加载关联数据，避免懒加载问题
        comments.forEach(comment -> {
            if (comment.getUser() != null) {
                comment.getUser().getUsername();
                // 加载用户的roles
                if (comment.getUser().getRoles() != null) {
                    comment.getUser().getRoles().size();
                }
            }
            if (comment.getDocument() != null) {
                comment.getDocument().getTitle();
                // 加载文档的tags集合
                if (comment.getDocument().getTags() != null) {
                    comment.getDocument().getTags().size();
                }
                // 加载文档的owner
                if (comment.getDocument().getOwner() != null) {
                    comment.getDocument().getOwner().getUsername();
                    if (comment.getDocument().getOwner().getRoles() != null) {
                        comment.getDocument().getOwner().getRoles().size();
                    }
                }
                // 加载文档的folder
                if (comment.getDocument().getFolder() != null) {
                    comment.getDocument().getFolder().getName();
                }
            }
            if (comment.getParent() != null) {
                comment.getParent().getContent();
            }
        });
        
        return comments;
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("只能删除自己的评论");
        }

        commentRepository.delete(comment);
    }
}
