package io.github.pve123.techpoc.board.application.port;

import io.github.pve123.techpoc.board.application.dto.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentQueryRepository {

    /** 답글이 아닌 댓글만, 각 댓글의 답글 수와 함께 찾습니다. */
    Page<CommentResponse> findCommentsWithReplyCount(Long postId, Pageable pageable);
}
