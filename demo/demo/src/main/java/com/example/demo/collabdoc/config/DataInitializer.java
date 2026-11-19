package com.example.demo.collabdoc.config;

import com.example.demo.collabdoc.entity.Role;
import com.example.demo.collabdoc.entity.User;
import com.example.demo.collabdoc.repository.RoleRepository;
import com.example.demo.collabdoc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 初始化角色数据
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(null, Role.RoleName.ROLE_ADMIN, "管理员"));
            roleRepository.save(new Role(null, Role.RoleName.ROLE_EDITOR, "编辑者"));
            roleRepository.save(new Role(null, Role.RoleName.ROLE_VIEWER, "查看者"));
            System.out.println("角色数据初始化完成");
        }

        // 初始化默认管理员账户
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@collabdoc.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            // 分配管理员角色
            Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("管理员角色不存在"));
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);

            userRepository.save(admin);
            System.out.println("默认管理员账户创建完成");
            System.out.println("用户名: admin");
            System.out.println("密码: admin123");
        }
    }
}
