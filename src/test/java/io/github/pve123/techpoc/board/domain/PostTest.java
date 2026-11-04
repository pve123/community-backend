package io.github.pve123.techpoc.board.domain;

import io.github.pve123.techpoc.common.error.BusinessException;
import io.github.pve123.techpoc.common.error.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostTest {

    private static final Author WRITER = new Author(1L, "글쓴이");
    private static final Long OTHER_MEMBER_ID = 2L;

    @Test
    void 글쓴이는_글을_고칠_수_있다() {
        Post post = Post.write(WRITER, "제목", "내용");

        post.edit(WRITER.id(), "바꾼 제목", "바꾼 내용");

        assertThat(post.getTitle()).isEqualTo("바꾼 제목");
    }

    @Test
    void 다른_회원은_글을_고칠_수_없다() {
        Post post = Post.write(WRITER, "제목", "내용");

        assertThatThrownBy(() -> post.edit(OTHER_MEMBER_ID, "바꾼 제목", "바꾼 내용"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).errorCode())
                .isEqualTo(ErrorCode.NOT_POST_AUTHOR);
        assertThat(post.getTitle()).isEqualTo("제목");
    }

    @Test
    void 다른_회원은_이미지를_붙일_수_없다() {
        Post post = Post.write(WRITER, "제목", "내용");

        assertThatThrownBy(() -> post.addImage(OTHER_MEMBER_ID, "cat.png", "2026/01/01/a.png"))
                .isInstanceOf(BusinessException.class);
        assertThat(post.getImages()).isEmpty();
    }

    @Test
    void 이미지_목록은_밖에서_바꿀_수_없다() {
        Post post = Post.write(WRITER, "제목", "내용");

        assertThatThrownBy(() -> post.getImages().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
