package com.example.demo.collabdoc.service;

import com.example.demo.collabdoc.dto.RegisterRequest;
import com.example.demo.collabdoc.dto.UserUpdateRequest;
import com.example.demo.collabdoc.entity.Role;
import com.example.demo.collabdoc.entity.User;
import com.example.demo.collabdoc.repository.RoleRepository;
import com.example.demo.collabdoc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被使用");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setEnabled(true);

        // 默认分配 VIEWER 角色
        Role viewerRole = roleRepository.findByName(Role.RoleName.ROLE_VIEWER)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        Set<Role> roles = new HashSet<>();
        roles.add(viewerRole);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        // 强制加载 roles 集合
        user.getRoles().size();
        return user;
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        // 强制加载 roles 集合
        user.getRoles().size();
        return user;
    }

    @Transactional
    public User updateUser(Long userId, UserUpdateRequest request) {
        User user = findById(userId);

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        // 强制加载所有用户的 roles 集合
        users.forEach(user -> user.getRoles().size());
        return users;
    }

    @Transactional
    public void assignRole(Long userId, Role.RoleName roleName) {
        User user = findById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        
        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Transactional
    public void removeRole(Long userId, Role.RoleName roleName) {
        User user = findById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        
        user.getRoles().remove(role);
        userRepository.save(user);
    }
}
