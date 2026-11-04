package io.github.pve123.techpoc.board.application.dto;

import io.github.pve123.techpoc.board.domain.Comment;

import java.time.LocalDateTime;

/**
 * @param replyCount 이 댓글에 달린 답글 수. 답글이면 항상 0입니다.
 */
public record CommentResponse(
        Long id,
        Long parentId,
        Long authorId,
        String authorNickname,
        String content,
        long replyCount,
        LocalDateTime createdAt
) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getParentId(),
                comment.getAuthor().id(),
                comment.getAuthor().nickname(),
                comment.getContent(),
                0,
                comment.getCreatedAt()
        );
    }
}
