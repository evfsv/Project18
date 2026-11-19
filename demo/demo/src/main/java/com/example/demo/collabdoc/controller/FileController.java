package com.example.demo.collabdoc.controller;

import com.example.demo.collabdoc.dto.ApiResponse;
import com.example.demo.collabdoc.dto.UserUpdateRequest;
import com.example.demo.collabdoc.entity.User;
import com.example.demo.collabdoc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private UserService userService;

    @PostMapping("/upload/avatar")
    public ApiResponse<?> uploadAvatar(@RequestParam("file") MultipartFile file,
                                      Authentication authentication) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return ApiResponse.error("文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error("只能上传图片文件");
            }

            // 验证文件大小（最大2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return ApiResponse.error("文件大小不能超过2MB");
            }

            // 创建上传目录
            File uploadDirectory = new File(uploadDir);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = Paths.get(uploadDir, filename);
            Files.write(filePath, file.getBytes());

            // 更新用户头像URL
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            String avatarUrl = "/uploads/" + filename;
            
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setAvatar(avatarUrl);
            userService.updateUser(user.getId(), updateRequest);

            return ApiResponse.success("头像上传成功", avatarUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.error("文件上传失败：" + e.getMessage());
        }
    }
}
