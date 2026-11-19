package com.example.demo.collabdoc.controller;

import com.example.demo.collabdoc.dto.ApiResponse;
import com.example.demo.collabdoc.dto.UserUpdateRequest;
import com.example.demo.collabdoc.entity.Role;
import com.example.demo.collabdoc.entity.User;
import com.example.demo.collabdoc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ApiResponse<?> getCurrentUser(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/me")
    public ApiResponse<?> updateCurrentUser(@RequestBody UserUpdateRequest request, 
                                           Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            User updatedUser = userService.updateUser(user.getId(), request);
            return ApiResponse.success("更新成功", updatedUser);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public ApiResponse<?> getUserList() {
        try {
            List<User> users = userService.getAllUsers();
            return ApiResponse.success(users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> assignRole(@PathVariable Long userId, @PathVariable String roleName) {
        try {
            Role.RoleName role = Role.RoleName.valueOf(roleName);
            userService.assignRole(userId, role);
            return ApiResponse.success("角色分配成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> removeRole(@PathVariable Long userId, @PathVariable String roleName) {
        try {
            Role.RoleName role = Role.RoleName.valueOf(roleName);
            userService.removeRole(userId, role);
            return ApiResponse.success("角色移除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
