package io.github.pve123.techpoc.board.application.dto;

import io.github.pve123.techpoc.board.domain.Post;
import io.github.pve123.techpoc.board.domain.PostImage;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        Long authorId,
        String authorNickname,
        long viewCount,
        List<ImageResponse> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PostDetailResponse from(Post post) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().id(),
                post.getAuthor().nickname(),
                post.getViewCount(),
                post.getImages().stream().map(ImageResponse::from).toList(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public record ImageResponse(Long id, String originalName) {

        static ImageResponse from(PostImage image) {
            return new ImageResponse(image.getId(), image.getOriginalName());
        }
    }
}
