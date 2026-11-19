package com.example.demo.collabdoc.controller;

import com.example.demo.collabdoc.dto.ApiResponse;
import com.example.demo.collabdoc.dto.CommentRequest;
import com.example.demo.collabdoc.entity.Comment;
import com.example.demo.collabdoc.entity.User;
import com.example.demo.collabdoc.service.CommentService;
import com.example.demo.collabdoc.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ApiResponse<?> addComment(@Valid @RequestBody CommentRequest request, 
                                    Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            Comment comment = commentService.addComment(request, user.getId());
            return ApiResponse.success("评论添加成功", comment);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/document/{documentId}")
    public ApiResponse<?> getDocumentComments(@PathVariable Long documentId) {
        try {
            List<Comment> comments = commentService.getDocumentComments(documentId);
            return ApiResponse.success(comments);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteComment(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            commentService.deleteComment(id, user.getId());
            return ApiResponse.success("评论删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
