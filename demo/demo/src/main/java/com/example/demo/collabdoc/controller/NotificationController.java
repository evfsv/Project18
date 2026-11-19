package com.example.demo.collabdoc.controller;

import com.example.demo.collabdoc.dto.ApiResponse;
import com.example.demo.collabdoc.entity.Notification;
import com.example.demo.collabdoc.entity.User;
import com.example.demo.collabdoc.service.NotificationService;
import com.example.demo.collabdoc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ApiResponse<?> getNotifications(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            List<Notification> notifications = notificationService.getUserNotifications(user.getId());
            return ApiResponse.success(notifications);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/unread")
    public ApiResponse<?> getUnreadNotifications(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            List<Notification> notifications = notificationService.getUnreadNotifications(user.getId());
            return ApiResponse.success(notifications);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/unread/count")
    public ApiResponse<?> getUnreadCount(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            Long count = notificationService.getUnreadCount(user.getId());
            return ApiResponse.success(count);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/read")
    public ApiResponse<?> markAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return ApiResponse.success("标记已读成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/read-all")
    public ApiResponse<?> markAllAsRead(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            notificationService.markAllAsRead(user.getId());
            return ApiResponse.success("全部标记已读成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
