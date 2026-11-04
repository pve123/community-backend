package io.github.pve123.techpoc.board.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 글을 쓰거나 고칠 때 받는 값입니다. */
public record PostRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank @Size(max = 10_000) String content
) {
}
