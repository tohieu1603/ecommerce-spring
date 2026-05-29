package com.example.auth_service.interfaces.rest.dtos;

import com.example.auth_service.application.commands.RegisterUserCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description= "New user registration payload")
public record RegisterRequest(
    @Schema(example="Johndoe", minLength= 3, maxLength=32)
    @NotBlank @Size(min=3, max=32) String username,

        @Schema(example = "john@example.com")
        @NotBlank @Email String email,

        @Schema(description = "Raw password, min 8 chars, must contain letter+digit", example = "P@ssw0rd123")
        @NotBlank @Size(min = 8, max = 100) String password,

        @Schema(example = "John") @NotBlank @Size(max = 64) String firstName,
        @Schema(example = "Doe")  @NotBlank @Size(max = 64) String lastName
) {
    public RegisterUserCommand toCommand() {
        return new RegisterUserCommand(username, email, password, firstName, lastName);
    }
}
