package io.github.pve123.techpoc.member.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProfileUpdateRequest(
        @NotBlank @Size(max = 20) String nickname,
        @Size(max = 255) String address,
        @Past LocalDate birthDate,
        @Size(max = 20) String phoneNumber
) {
}
