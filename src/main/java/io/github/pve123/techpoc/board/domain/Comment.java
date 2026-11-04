package io.github.pve123.techpoc.board.domain;

import io.github.pve123.techpoc.common.error.BusinessException;
import io.github.pve123.techpoc.common.error.ErrorCode;
import io.github.pve123.techpoc.common.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 댓글 애그리거트입니다.
 *
 * <p>게시글에 댓글이 수천 개 달려도 게시글을 불러올 때 함께 불러오지 않도록,
 * Post 안에 넣지 않고 따로 두었습니다. 게시글과 부모 댓글은 ID로만 가리킵니다.
 *
 * <p>답글은 한 단계까지만 달 수 있습니다. parentId가 없으면 댓글, 있으면 답글입니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    private Long parentId;

    @Embedded
    private Author author;

    @Column(nullable = false, length = 500)
    private String content;

    private Comment(Long postId, Long parentId, Author author, String content) {
        this.postId = postId;
        this.parentId = parentId;
        this.author = author;
        this.content = content;
    }

    public static Comment write(Long postId, Author author, String content) {
        return new Comment(postId, null, author, content);
    }

    public static Comment reply(Comment parent, Long postId, Author author, String content) {
        if (parent.isReply()) {
            throw new BusinessException(ErrorCode.REPLY_TO_REPLY);
        }
        if (!parent.postId.equals(postId)) {
            throw new BusinessException(ErrorCode.PARENT_IN_OTHER_POST);
        }
        return new Comment(postId, parent.id, author, content);
    }

    public boolean isReply() {
        return parentId != null;
    }
}
