package com.example.demo.collabdoc.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
}
