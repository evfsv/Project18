package com.example.demo.collabdoc.service;

import com.example.demo.collabdoc.controller.WebSocketController;
import com.example.demo.collabdoc.dto.TaskRequest;
import com.example.demo.collabdoc.dto.WebSocketMessage;
import com.example.demo.collabdoc.entity.Document;
import com.example.demo.collabdoc.entity.Task;
import com.example.demo.collabdoc.entity.User;
import com.example.demo.collabdoc.repository.DocumentRepository;
import com.example.demo.collabdoc.repository.TaskRepository;
import com.example.demo.collabdoc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private WebSocketController webSocketController;

    @Transactional
    public Task createTask(TaskRequest request, Long creatorId) {
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Task task = new Task();
        task.setDocument(document);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCreatedBy(creator);
        task.setDueDate(request.getDueDate());
        task.setStatus(Task.TaskStatus.TODO);

        if (request.getAssignedToId() != null) {
            User assignedUser = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("被分配用户不存在"));
            task.setAssignedTo(assignedUser);

            // 发送任务分配通知
            notificationService.createTaskAssignedNotification(
                    request.getAssignedToId(), creatorId, task.getId());
        }

        Task savedTask = taskRepository.save(task);
        
        // 主动加载关联数据，避免懒加载问题
        if (savedTask.getCreatedBy() != null) {
            savedTask.getCreatedBy().getUsername();
            // 加载用户的roles
            if (savedTask.getCreatedBy().getRoles() != null) {
                savedTask.getCreatedBy().getRoles().size();
            }
        }
        if (savedTask.getAssignedTo() != null) {
            savedTask.getAssignedTo().getUsername();
            // 加载用户的roles
            if (savedTask.getAssignedTo().getRoles() != null) {
                savedTask.getAssignedTo().getRoles().size();
            }
        }
        if (savedTask.getDocument() != null) {
            savedTask.getDocument().getTitle();
            // 加载文档的tags集合
            if (savedTask.getDocument().getTags() != null) {
                savedTask.getDocument().getTags().size();
            }
            // 加载文档的owner
            if (savedTask.getDocument().getOwner() != null) {
                savedTask.getDocument().getOwner().getUsername();
                if (savedTask.getDocument().getOwner().getRoles() != null) {
                    savedTask.getDocument().getOwner().getRoles().size();
                }
            }
            // 加载文档的folder
            if (savedTask.getDocument().getFolder() != null) {
                savedTask.getDocument().getFolder().getName();
            }
        }
        
        // 通过 WebSocket 通知其他用户有新任务
        WebSocketMessage wsMessage = new WebSocketMessage(
            "TASK_ADD",
            document.getId(),
            savedTask,
            creator.getUsername(),
            creatorId
        );
        webSocketController.sendToDocument(document.getId(), wsMessage);

        return savedTask;
    }

    @Transactional(readOnly = true)
    public List<Task> getDocumentTasks(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        List<Task> tasks = taskRepository.findByDocumentOrderByCreatedAtDesc(document);
        
        // 主动加载关联数据，避免懒加载问题
        tasks.forEach(task -> {
            if (task.getCreatedBy() != null) {
                task.getCreatedBy().getUsername();
                // 加载用户的roles
                if (task.getCreatedBy().getRoles() != null) {
                    task.getCreatedBy().getRoles().size();
                }
            }
            if (task.getAssignedTo() != null) {
                task.getAssignedTo().getUsername();
                // 加载用户的roles
                if (task.getAssignedTo().getRoles() != null) {
                    task.getAssignedTo().getRoles().size();
                }
            }
            if (task.getDocument() != null) {
                task.getDocument().getTitle();
                // 加载文档的tags集合
                if (task.getDocument().getTags() != null) {
                    task.getDocument().getTags().size();
                }
                // 加载文档的owner
                if (task.getDocument().getOwner() != null) {
                    task.getDocument().getOwner().getUsername();
                    if (task.getDocument().getOwner().getRoles() != null) {
                        task.getDocument().getOwner().getRoles().size();
                    }
                }
                // 加载文档的folder
                if (task.getDocument().getFolder() != null) {
                    task.getDocument().getFolder().getName();
                }
            }
        });
        
        return tasks;
    }

    @Transactional(readOnly = true)
    public List<Task> getUserTasks(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        List<Task> tasks = taskRepository.findByAssignedTo(user);
        
        // 主动加载关联数据，避免懒加载问题
        tasks.forEach(task -> {
            if (task.getCreatedBy() != null) {
                task.getCreatedBy().getUsername();
                // 加载用户的roles
                if (task.getCreatedBy().getRoles() != null) {
                    task.getCreatedBy().getRoles().size();
                }
            }
            if (task.getAssignedTo() != null) {
                task.getAssignedTo().getUsername();
                // 加载用户的roles
                if (task.getAssignedTo().getRoles() != null) {
                    task.getAssignedTo().getRoles().size();
                }
            }
            if (task.getDocument() != null) {
                task.getDocument().getTitle();
                // 加载文档的tags集合
                if (task.getDocument().getTags() != null) {
                    task.getDocument().getTags().size();
                }
                // 加载文档的owner
                if (task.getDocument().getOwner() != null) {
                    task.getDocument().getOwner().getUsername();
                    if (task.getDocument().getOwner().getRoles() != null) {
                        task.getDocument().getOwner().getRoles().size();
                    }
                }
                // 加载文档的folder
                if (task.getDocument().getFolder() != null) {
                    task.getDocument().getFolder().getName();
                }
            }
        });
        
        return tasks;
    }

    @Transactional
    public Task updateTaskStatus(Long taskId, Task.TaskStatus status, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        // 权限检查：只有文档所有者或任务创建者可以修改任务状态
        Long documentOwnerId = task.getDocument().getOwner().getId();
        Long taskCreatorId = task.getCreatedBy().getId();
        
        if (!documentOwnerId.equals(userId) && !taskCreatorId.equals(userId)) {
            throw new RuntimeException("您没有权限修改此任务状态");
        }

        task.setStatus(status);

        if (status == Task.TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
            // 发送任务完成通知
            notificationService.createTaskCompletedNotification(
                    task.getCreatedBy().getId(), userId, task.getId());
        }

        Task savedTask = taskRepository.save(task);
        
        // 主动加载关联数据，避免懒加载问题
        if (savedTask.getCreatedBy() != null) {
            savedTask.getCreatedBy().getUsername();
            // 加载用户的roles
            if (savedTask.getCreatedBy().getRoles() != null) {
                savedTask.getCreatedBy().getRoles().size();
            }
        }
        if (savedTask.getAssignedTo() != null) {
            savedTask.getAssignedTo().getUsername();
            // 加载用户的roles
            if (savedTask.getAssignedTo().getRoles() != null) {
                savedTask.getAssignedTo().getRoles().size();
            }
        }
        if (savedTask.getDocument() != null) {
            savedTask.getDocument().getTitle();
            // 加载文档的tags集合
            if (savedTask.getDocument().getTags() != null) {
                savedTask.getDocument().getTags().size();
            }
            // 加载文档的owner
            if (savedTask.getDocument().getOwner() != null) {
                savedTask.getDocument().getOwner().getUsername();
                if (savedTask.getDocument().getOwner().getRoles() != null) {
                    savedTask.getDocument().getOwner().getRoles().size();
                }
            }
            // 加载文档的folder
            if (savedTask.getDocument().getFolder() != null) {
                savedTask.getDocument().getFolder().getName();
            }
        }
        
        // 通过 WebSocket 通知其他用户任务状态已更新
        User currentUser = userRepository.findById(userId).orElse(null);
        WebSocketMessage wsMessage = new WebSocketMessage(
            "TASK_UPDATE",
            savedTask.getDocument().getId(),
            savedTask,
            currentUser != null ? currentUser.getUsername() : "Unknown",
            userId
        );
        webSocketController.sendToDocument(savedTask.getDocument().getId(), wsMessage);
        
        return savedTask;
    }

    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        if (!task.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("只能删除自己创建的任务");
        }

        taskRepository.delete(task);
    }
}
