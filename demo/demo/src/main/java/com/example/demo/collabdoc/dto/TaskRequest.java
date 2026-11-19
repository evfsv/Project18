package com.example.demo.collabdoc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskRequest {
    @NotNull(message = "文档ID不能为空")
    private Long documentId;

    @NotBlank(message = "任务标题不能为空")
    private String title;

    private String description;

    private Long assignedToId;

    private LocalDateTime dueDate;
}
