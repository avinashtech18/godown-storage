package com.godown.storage.service;

import com.godown.storage.dto.RegisterRequest;
import com.godown.storage.dto.UserResponse;
import com.godown.storage.entity.Role;
import com.godown.storage.entity.User;
import com.godown.storage.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;

@Service
public class RegistrationService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        // BCrypt limits passwords to 72 bytes, and some characters use multiple bytes.
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes");
        }

        String email = request.email().toLowerCase(Locale.ROOT);
        String hash = passwordEncoder.encode(request.password());
        User user = new User(request.name().strip(), email, hash, Role.CUSTOMER);

        try {
            // The database's unique constraint handles simultaneous registrations.
            User saved = users.saveAndFlush(user);
            return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole());
        } catch (DataIntegrityViolationException exception) {
            // MySQL error 1062 means a duplicate unique key.
            if (exception.getMostSpecificCause() instanceof SQLException sql
                    && sql.getErrorCode() == 1062) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
            }
            throw exception;
        }
    }
}