package com.example.demo.collabdoc.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_permissions")
public class DocumentPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionType permission;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    @Column(name = "granted_by")
    private Long grantedBy;

    public enum PermissionType {
        OWNER,   // 所有者
        EDITOR,  // 可编辑
        VIEWER   // 只读
    }

    @PrePersist
    protected void onCreate() {
        grantedAt = LocalDateTime.now();
    }
}
