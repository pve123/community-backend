package io.github.pve123.techpoc.board.application.port;

import io.github.pve123.techpoc.board.application.dto.PostSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 화면에 맞춘 게시글 목록 조회입니다. 엔티티 대신 필요한 칸만 바로 읽어 옵니다. */
public interface PostQueryRepository {

    /** @param authorId 비우면 모든 글, 넣으면 그 회원이 쓴 글만 찾습니다. */
    Page<PostSummaryResponse> findSummaries(Long authorId, Pageable pageable);
}
