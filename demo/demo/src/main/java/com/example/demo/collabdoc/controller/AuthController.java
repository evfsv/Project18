package com.example.demo.collabdoc.controller;

import com.example.demo.collabdoc.dto.*;
import com.example.demo.collabdoc.entity.User;
import com.example.demo.collabdoc.security.JwtUtil;
import com.example.demo.collabdoc.service.OperationLogService;
import com.example.demo.collabdoc.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OperationLogService operationLogService;

    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return ApiResponse.success("注册成功", user.getUsername());
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken(authentication);

            User user = userService.findByUsername(request.getUsername());
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // 记录登录日志
            operationLogService.log(user.getId(), user.getUsername(), "LOGIN", "USER",
                    user.getId(), "用户登录", httpRequest.getRemoteAddr());

            JwtResponse response = new JwtResponse(jwt, user.getId(), user.getUsername(), 
                    user.getEmail(), roles);

            return ApiResponse.success("登录成功", response);
        } catch (Exception e) {
            return ApiResponse.error("用户名或密码错误");
        }
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            String username = authentication.getName();
            User user = userService.findByUsername(username);
            
            // 记录登出日志
            operationLogService.log(user.getId(), user.getUsername(), "LOGOUT", "USER",
                    user.getId(), "用户登出", request.getRemoteAddr());
        }
        
        SecurityContextHolder.clearContext();
        return ApiResponse.success("登出成功");
    }
}
