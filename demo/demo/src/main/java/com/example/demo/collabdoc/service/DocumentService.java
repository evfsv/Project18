package com.example.demo.collabdoc.service;

import com.example.demo.collabdoc.controller.WebSocketController;
import com.example.demo.collabdoc.dto.DocumentRequest;
import com.example.demo.collabdoc.dto.WebSocketMessage;
import com.example.demo.collabdoc.entity.*;
import com.example.demo.collabdoc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private DocumentPermissionRepository permissionRepository;

    @Autowired
    private WebSocketController webSocketController;

    @Transactional
    public Document createDocument(DocumentRequest request, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Document document = new Document();
        document.setTitle(request.getTitle());
        document.setContent(request.getContent());
        document.setOwner(owner);
        document.setIsTemplate(request.getIsTemplate());
        document.setLastEditedBy(userId);

        if (request.getFolderId() != null) {
            Folder folder = folderRepository.findById(request.getFolderId())
                    .orElseThrow(() -> new RuntimeException("文件夹不存在"));
            document.setFolder(folder);
        }

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName)));
                tags.add(tag);
            }
            document.setTags(tags);
        }

        Document savedDocument = documentRepository.save(document);

        // 创建所有者权限
        DocumentPermission permission = new DocumentPermission();
        permission.setDocument(savedDocument);
        permission.setUser(owner);
        permission.setPermission(DocumentPermission.PermissionType.OWNER);
        permission.setGrantedBy(userId);
        permissionRepository.save(permission);

        // 主动加载关联数据，避免懒加载问题
        if (savedDocument.getOwner() != null) {
            savedDocument.getOwner().getUsername();
            // 加载owner的roles
            if (savedDocument.getOwner().getRoles() != null) {
                savedDocument.getOwner().getRoles().size();
            }
        }
        
        // 主动加载tags集合
        if (savedDocument.getTags() != null) {
            savedDocument.getTags().size();
        }
        
        // 主动加载folder
        if (savedDocument.getFolder() != null) {
            savedDocument.getFolder().getName();
        }

        return savedDocument;
    }

    @Transactional
    public Document updateDocument(Long documentId, DocumentRequest request, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        // 检查权限
        if (!hasEditPermission(documentId, userId)) {
            throw new RuntimeException("没有编辑权限");
        }

        document.setTitle(request.getTitle());
        document.setContent(request.getContent());
        document.setLastEditedBy(userId);

        if (request.getFolderId() != null) {
            Folder folder = folderRepository.findById(request.getFolderId())
                    .orElseThrow(() -> new RuntimeException("文件夹不存在"));
            document.setFolder(folder);
        }

        if (request.getTags() != null) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName)));
                tags.add(tag);
            }
            document.setTags(tags);
        }

        Document savedDocument = documentRepository.save(document);
        
        // 主动加载关联数据，避免懒加载问题
        if (savedDocument.getOwner() != null) {
            savedDocument.getOwner().getUsername();
            // 加载owner的roles
            if (savedDocument.getOwner().getRoles() != null) {
                savedDocument.getOwner().getRoles().size();
            }
        }
        
        // 主动加载tags集合
        if (savedDocument.getTags() != null) {
            savedDocument.getTags().size();
        }
        
        // 主动加载folder
        if (savedDocument.getFolder() != null) {
            savedDocument.getFolder().getName();
        }
        
        // 通过 WebSocket 通知其他用户文档已更新
        User currentUser = userRepository.findById(userId).orElse(null);
        WebSocketMessage wsMessage = new WebSocketMessage(
            "DOCUMENT_UPDATE",
            documentId,
            savedDocument,
            currentUser != null ? currentUser.getUsername() : "Unknown",
            userId
        );
        webSocketController.sendToDocument(documentId, wsMessage);
        
        return savedDocument;
    }

    @Transactional(readOnly = true)
    public Document getDocument(Long documentId, Long userId) {
        // 先用普通方法查询文档
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        // 检查权限
        if (!hasViewPermission(documentId, userId)) {
            throw new RuntimeException("没有查看权限");
        }

        // 主动触发owner的加载（解决懒加载问题）
        if (document.getOwner() != null) {
            document.getOwner().getUsername();
            // 加载owner的roles
            if (document.getOwner().getRoles() != null) {
                document.getOwner().getRoles().size();
            }
        }
        
        // 主动加载tags集合
        if (document.getTags() != null) {
            document.getTags().size();
        }
        
        // 主动加载folder
        if (document.getFolder() != null) {
            document.getFolder().getName();
        }

        return document;
    }

    @Transactional(readOnly = true)
    public List<Document> getUserDocuments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        List<Document> documents = documentRepository.findByOwnerIdOrderByUpdatedAtDesc(userId);
        
        System.out.println("getUserDocuments - 用户ID: " + userId + ", 文档数量: " + documents.size());
        
        // 主动加载关联数据，避免懒加载问题
        documents.forEach(doc -> {
            System.out.println("  - 文档ID: " + doc.getId() + ", 标题: [" + doc.getTitle() + "], 标题长度: " + doc.getTitle().length());
            if (doc.getOwner() != null) {
                doc.getOwner().getUsername();
                // 主动加载 owner 的 roles
                if (doc.getOwner().getRoles() != null) {
                    doc.getOwner().getRoles().size();
                }
            }
            if (doc.getTags() != null) {
                doc.getTags().size();
            }
            if (doc.getFolder() != null) {
                doc.getFolder().getName();
            }
        });
        
        return documents;
    }

    @Transactional(readOnly = true)
    public List<Document> getSharedDocuments(Long userId) {
        List<Document> documents = documentRepository.findSharedDocuments(userId);
        // 主动加载owner信息和tags集合，避免懒加载问题
        documents.forEach(doc -> {
            if (doc.getOwner() != null) {
                doc.getOwner().getUsername();
                // 主动加载 owner 的 roles
                if (doc.getOwner().getRoles() != null) {
                    doc.getOwner().getRoles().size();
                }
            }
            // 主动加载tags集合
            if (doc.getTags() != null) {
                doc.getTags().size();
            }
            // 主动加载folder
            if (doc.getFolder() != null) {
                doc.getFolder().getName();
            }
        });
        return documents;
    }

    @Transactional(readOnly = true)
    public List<Document> searchDocuments(String keyword, Long userId) {
        System.out.println("DocumentService.searchDocuments - 关键词: [" + keyword + "], 用户ID: " + userId);
        List<Document> documents = documentRepository.searchByKeywordAndOwner(keyword, userId);
        System.out.println("DocumentService.searchDocuments - 查询结果数量: " + documents.size());
        
        // 主动加载关联数据，避免懒加载问题
        documents.forEach(doc -> {
            System.out.println("  - 文档ID: " + doc.getId() + ", 标题: " + doc.getTitle());
            if (doc.getOwner() != null) {
                doc.getOwner().getUsername();
                // 主动加载 owner 的 roles
                if (doc.getOwner().getRoles() != null) {
                    doc.getOwner().getRoles().size();
                }
            }
            if (doc.getTags() != null) {
                doc.getTags().size();
            }
            if (doc.getFolder() != null) {
                doc.getFolder().getName();
            }
        });
        
        return documents;
    }

    public List<Document> getTemplates() {
        return documentRepository.findByIsTemplate(true);
    }

    @Transactional
    public void deleteDocument(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        if (!document.getOwner().getId().equals(userId)) {
            throw new RuntimeException("只有所有者可以删除文档");
        }

        documentRepository.delete(document);
    }

    @Transactional
    public void shareDocument(Long documentId, Long targetUserId, DocumentPermission.PermissionType permission, Long currentUserId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        if (!document.getOwner().getId().equals(currentUserId)) {
            throw new RuntimeException("只有所有者可以分享文档");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("目标用户不存在"));

        DocumentPermission docPermission = new DocumentPermission();
        docPermission.setDocument(document);
        docPermission.setUser(targetUser);
        docPermission.setPermission(permission);
        docPermission.setGrantedBy(currentUserId);

        permissionRepository.save(docPermission);
    }

    public boolean hasViewPermission(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        if (document.getOwner().getId().equals(userId)) {
            return true;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 查询所有权限记录，只要有一条就说明有权限
        List<DocumentPermission> permissions = permissionRepository.findByDocument(document);
        return permissions.stream().anyMatch(p -> p.getUser().getId().equals(userId));
    }

    public boolean hasEditPermission(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        if (document.getOwner().getId().equals(userId)) {
            return true;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 查询所有权限记录，检查是否有编辑权限
        List<DocumentPermission> permissions = permissionRepository.findByDocument(document);
        return permissions.stream()
                .anyMatch(p -> p.getUser().getId().equals(userId) && 
                         (p.getPermission() == DocumentPermission.PermissionType.EDITOR ||
                          p.getPermission() == DocumentPermission.PermissionType.OWNER));
    }
}
