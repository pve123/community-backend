package io.github.pve123.techpoc.board.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param parentId 답글이면 부모 댓글 ID, 그냥 댓글이면 비워 둡니다.
 */
public record CommentRequest(
        @NotBlank @Size(max = 500) String content,
        Long parentId
) {
}
