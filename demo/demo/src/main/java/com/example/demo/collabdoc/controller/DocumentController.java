package com.example.demo.collabdoc.controller;

import com.example.demo.collabdoc.dto.ApiResponse;
import com.example.demo.collabdoc.dto.DocumentRequest;
import com.example.demo.collabdoc.entity.Document;
import com.example.demo.collabdoc.entity.DocumentPermission;
import com.example.demo.collabdoc.entity.User;
import com.example.demo.collabdoc.service.DocumentService;
import com.example.demo.collabdoc.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ApiResponse<?> createDocument(@Valid @RequestBody DocumentRequest request, 
                                        Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            Document document = documentService.createDocument(request, user.getId());
            return ApiResponse.success("文档创建成功", document);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateDocument(@PathVariable Long id, 
                                        @Valid @RequestBody DocumentRequest request,
                                        Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            Document document = documentService.updateDocument(id, request, user.getId());
            return ApiResponse.success("文档更新成功", document);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getDocument(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            
            System.out.println("用户 " + username + " (ID: " + user.getId() + ") 正在请求文档 ID: " + id);
            
            Document document = documentService.getDocument(id, user.getId());
            return ApiResponse.success(document);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<?> getUserDocuments(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            List<Document> documents = documentService.getUserDocuments(user.getId());
            return ApiResponse.success(documents);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/shared")
    public ApiResponse<?> getSharedDocuments(Authentication authentication) {
        try {
            String username = authentication.getName();
            System.out.println("用户 " + username + " 正在请求共享文档列表");
            
            User user = userService.findByUsername(username);
            System.out.println("用户 ID: " + user.getId());
            
            List<Document> documents = documentService.getSharedDocuments(user.getId());
            System.out.println("找到 " + documents.size() + " 个共享文档");
            
            return ApiResponse.success(documents);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ApiResponse<?> searchDocuments(@RequestParam String keyword, Authentication authentication) {
        try {
            System.out.println("搜索关键词：" + keyword);
            
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            System.out.println("当前用户：" + username + " (ID: " + user.getId() + ")");
            
            List<Document> documents = documentService.searchDocuments(keyword, user.getId());
            System.out.println("找到 " + documents.size() + " 个文档");
            return ApiResponse.success(documents);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/templates")
    public ApiResponse<?> getTemplates() {
        try {
            List<Document> templates = documentService.getTemplates();
            return ApiResponse.success(templates);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteDocument(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            documentService.deleteDocument(id, user.getId());
            return ApiResponse.success("文档删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/share")
    public ApiResponse<?> shareDocument(@PathVariable Long id,
                                       @RequestParam Long targetUserId,
                                       @RequestParam DocumentPermission.PermissionType permission,
                                       Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            documentService.shareDocument(id, targetUserId, permission, user.getId());
            return ApiResponse.success("文档分享成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
