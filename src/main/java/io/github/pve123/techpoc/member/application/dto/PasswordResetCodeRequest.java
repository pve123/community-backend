package io.github.pve123.techpoc.member.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetCodeRequest(@NotBlank @Email String email) {
}
