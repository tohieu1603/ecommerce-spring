package com.example.auth_service.application.dtos;

import java.time.Instant;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoleDTO(
    String id,
    String name,
    String description,
    Set<String> permissions,
    Instant createdAt,
    Instant updatedAt
) {}