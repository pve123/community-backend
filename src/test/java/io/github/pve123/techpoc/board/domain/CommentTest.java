package io.github.pve123.techpoc.board.domain;

import io.github.pve123.techpoc.common.error.BusinessException;
import io.github.pve123.techpoc.common.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentTest {

    private static final Author AUTHOR = new Author(1L, "철수");
    private static final Long POST_ID = 10L;

    @Test
    void 댓글에는_답글을_달_수_있다() {
        Comment comment = savedComment(1L, POST_ID);

        Comment reply = Comment.reply(comment, POST_ID, AUTHOR, "답글");

        assertThat(reply.isReply()).isTrue();
    }

    @Test
    void 답글에는_답글을_달_수_없다() {
        Comment comment = savedComment(1L, POST_ID);
        Comment reply = Comment.reply(comment, POST_ID, AUTHOR, "답글");
        ReflectionTestUtils.setField(reply, "id", 2L);

        assertThatThrownBy(() -> Comment.reply(reply, POST_ID, AUTHOR, "답글의 답글"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).errorCode())
                .isEqualTo(ErrorCode.REPLY_TO_REPLY);
    }

    @Test
    void 다른_글의_댓글에는_답글을_달_수_없다() {
        Comment commentOnOtherPost = savedComment(1L, 99L);

        assertThatThrownBy(() -> Comment.reply(commentOnOtherPost, POST_ID, AUTHOR, "답글"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).errorCode())
                .isEqualTo(ErrorCode.PARENT_IN_OTHER_POST);
    }

    /** DB에 저장된 댓글처럼 ID가 있는 댓글을 만듭니다. */
    private static Comment savedComment(Long id, Long postId) {
        Comment comment = Comment.write(postId, AUTHOR, "댓글");
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }
}
