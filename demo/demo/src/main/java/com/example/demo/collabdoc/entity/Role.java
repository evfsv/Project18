package com.example.demo.collabdoc.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleName name;

    private String description;

    public enum RoleName {
        ROLE_ADMIN,
        ROLE_EDITOR,
        ROLE_VIEWER
    }

    public Role(RoleName name) {
        this.name = name;
    }
}
