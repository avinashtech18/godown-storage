package com.godown.storage.repository;

import com.godown.storage.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data derives the query from the User.email field.
    Optional<User> findByEmail(String email);
}