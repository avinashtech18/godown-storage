package com.godown.storage.dto;

import com.godown.storage.entity.Role;

public record UserResponse(Long id, String name, String email, Role role) {}