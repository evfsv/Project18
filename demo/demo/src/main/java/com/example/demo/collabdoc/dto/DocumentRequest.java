package com.example.demo.collabdoc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Set;

@Data
public class DocumentRequest {
    @NotBlank(message = "文档标题不能为空")
    private String title;

    private String content;

    private Long folderId;

    private Set<String> tags;

    private Boolean isTemplate = false;
}
