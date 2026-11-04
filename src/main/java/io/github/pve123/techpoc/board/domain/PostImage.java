package io.github.pve123.techpoc.board.domain;

import io.github.pve123.techpoc.common.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시글에 붙은 이미지입니다. Post 애그리거트에 속하므로 직접 만들거나 저장하지 않습니다.
 *
 * <p>storedPath는 서버 안의 위치라서 응답으로 내보내지 않습니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false, length = 500)
    private String storedPath;

    PostImage(Post post, String originalName, String storedPath) {
        this.post = post;
        this.originalName = originalName;
        this.storedPath = storedPath;
    }
}
