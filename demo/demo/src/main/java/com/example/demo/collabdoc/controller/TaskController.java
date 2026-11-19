package com.example.demo.collabdoc.controller;

import com.example.demo.collabdoc.dto.ApiResponse;
import com.example.demo.collabdoc.dto.TaskRequest;
import com.example.demo.collabdoc.entity.Task;
import com.example.demo.collabdoc.entity.User;
import com.example.demo.collabdoc.service.TaskService;
import com.example.demo.collabdoc.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ApiResponse<?> createTask(@Valid @RequestBody TaskRequest request, 
                                    Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            Task task = taskService.createTask(request, user.getId());
            return ApiResponse.success("任务创建成功", task);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/document/{documentId}")
    public ApiResponse<?> getDocumentTasks(@PathVariable Long documentId) {
        try {
            List<Task> tasks = taskService.getDocumentTasks(documentId);
            return ApiResponse.success(tasks);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/my")
    public ApiResponse<?> getMyTasks(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            List<Task> tasks = taskService.getUserTasks(user.getId());
            return ApiResponse.success(tasks);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ApiResponse<?> updateTaskStatus(@PathVariable Long id, 
                                          @RequestParam Task.TaskStatus status,
                                          Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            Task task = taskService.updateTaskStatus(id, status, user.getId());
            return ApiResponse.success("任务状态更新成功", task);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteTask(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            taskService.deleteTask(id, user.getId());
            return ApiResponse.success("任务删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
