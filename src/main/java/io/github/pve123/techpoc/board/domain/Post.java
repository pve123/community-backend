package io.github.pve123.techpoc.board.domain;

import io.github.pve123.techpoc.common.error.BusinessException;
import io.github.pve123.techpoc.common.error.ErrorCode;
import io.github.pve123.techpoc.common.jpa.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 게시글 애그리거트의 루트입니다. 게시글에 붙은 이미지(PostImage)는 이 클래스를 통해서만 바꿉니다.
 *
 * <p>"글쓴이만 고치고 지울 수 있다"는 규칙도 여기서 지킵니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Author author;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 조회수는 동시에 여러 명이 올릴 수 있어서 엔티티가 아닌 UPDATE 쿼리로만 올립니다. */
    @Column(nullable = false)
    private long viewCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<PostImage> images = new ArrayList<>();

    private Post(Author author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }

    public static Post write(Author author, String title, String content) {
        return new Post(author, title, content);
    }

    public void edit(Long memberId, String title, String content) {
        checkAuthor(memberId);
        this.title = title;
        this.content = content;
    }

    public void checkAuthor(Long memberId) {
        if (!author.isSameMember(memberId)) {
            throw new BusinessException(ErrorCode.NOT_POST_AUTHOR);
        }
    }

    public void addImage(Long memberId, String originalName, String storedPath) {
        checkAuthor(memberId);
        images.add(new PostImage(this, originalName, storedPath));
    }

    /** 이미지를 떼어 내고, 지울 파일을 알 수 있도록 떼어 낸 이미지를 돌려줍니다. */
    public PostImage removeImage(Long memberId, Long imageId) {
        checkAuthor(memberId);
        PostImage image = images.stream()
                .filter(candidate -> candidate.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));
        images.remove(image);
        return image;
    }

    public List<PostImage> getImages() {
        return Collections.unmodifiableList(images);
    }
}
