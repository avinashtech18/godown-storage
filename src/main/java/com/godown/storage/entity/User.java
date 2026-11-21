package com.godown.storage.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    // The registration service will pass in a BCrypt hash.
    @Column(nullable = false, length = 60)
    private String password;

    // Store ADMIN or CUSTOMER by name so reordering the enum is safe.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    public User(String name, String email, String passwordHash, Role role) {
        this.name = name;
        this.email = email;
        this.password = passwordHash;
        this.role = role;
    }
}