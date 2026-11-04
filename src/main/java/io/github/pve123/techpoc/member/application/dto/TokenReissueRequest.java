package io.github.pve123.techpoc.member.application.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest(@NotBlank String refreshToken) {
}
