package com.example.auth_service.interfaces.rest.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Schema(description = "Change-password payload for the current user")
public record ChangePasswordRequest(
    @Schema(description="Current password")
    @NotBlank String oldPassword,

    @Schema(description = "Desired new password")
    @NotBlank @Size(min = 8, max = 100) String newPassword
) {
    
}
