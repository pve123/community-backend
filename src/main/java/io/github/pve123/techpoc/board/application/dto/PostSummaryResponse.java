package io.github.pve123.techpoc.board.application.dto;

import java.time.LocalDateTime;

/** 목록에 보여 줄 짧은 정보입니다. 본문은 담지 않습니다. */
public record PostSummaryResponse(
        Long id,
        String title,
        Long authorId,
        String authorNickname,
        long viewCount,
        LocalDateTime createdAt
) {
}
