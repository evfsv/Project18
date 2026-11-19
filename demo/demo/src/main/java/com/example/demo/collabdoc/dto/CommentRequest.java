package com.example.demo.collabdoc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {
    @NotNull(message = "文档ID不能为空")
    private Long documentId;

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Integer positionStart;

    private Integer positionEnd;

    private Long parentId;

    private Long mentionedUserId;
}
