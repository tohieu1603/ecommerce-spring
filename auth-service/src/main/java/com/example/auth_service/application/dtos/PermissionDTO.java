package com.example.auth_service.application.dtos;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PermissionDTO(
    String id,
    String name,
    String resource,
    String action,
    String description,
    Instant createdAt,
    Instant updatedAt
) {}
