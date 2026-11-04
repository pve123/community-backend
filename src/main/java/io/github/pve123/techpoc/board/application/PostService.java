package io.github.pve123.techpoc.board.application;

import io.github.pve123.techpoc.board.application.dto.PostDetailResponse;
import io.github.pve123.techpoc.board.application.dto.PostRequest;
import io.github.pve123.techpoc.board.application.dto.PostSummaryResponse;
import io.github.pve123.techpoc.board.application.port.AuthorFinder;
import io.github.pve123.techpoc.board.application.port.ImageStorage;
import io.github.pve123.techpoc.board.application.port.ImageStorage.StoredImage;
import io.github.pve123.techpoc.board.application.port.PostViewHistory;
import io.github.pve123.techpoc.board.domain.Post;
import io.github.pve123.techpoc.board.domain.PostImage;
import io.github.pve123.techpoc.board.domain.PostRepository;
import io.github.pve123.techpoc.board.application.port.PostQueryRepository;
import io.github.pve123.techpoc.common.error.BusinessException;
import io.github.pve123.techpoc.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostQueryRepository postQueryRepository;
    private final AuthorFinder authorFinder;
    private final PostViewHistory postViewHistory;
    private final ImageStorage imageStorage;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PostDetailResponse write(Long memberId, PostRequest request) {
        Post post = Post.write(authorFinder.find(memberId), request.title(), request.content());
        return PostDetailResponse.from(postRepository.save(post));
    }

    public Page<PostSummaryResponse> getPosts(Pageable pageable) {
        return postQueryRepository.findSummaries(null, pageable);
    }

    public Page<PostSummaryResponse> getPostsByAuthor(Long authorId, Pageable pageable) {
        return postQueryRepository.findSummaries(authorId, pageable);
    }

    /** 글을 보여 주면서, 이 회원이 처음 보는 글이면 조회수를 1 올립니다. */
    @Transactional
    public PostDetailResponse getPost(Long postId, Long viewerId) {
        if (!postRepository.existsById(postId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        if (postViewHistory.markViewed(postId, viewerId)) {
            postRepository.increaseViewCount(postId);
        }
        return PostDetailResponse.from(findPostWithImages(postId));
    }

    @Transactional
    public PostDetailResponse edit(Long postId, Long memberId, PostRequest request) {
        Post post = findPostWithImages(postId);
        post.edit(memberId, request.title(), request.content());
        return PostDetailResponse.from(post);
    }

    @Transactional
    public void delete(Long postId, Long memberId) {
        Post post = findPostWithImages(postId);
        post.checkAuthor(memberId);
        post.getImages().forEach(image -> eventPublisher.publishEvent(new ImageFileEvents.Discarded(image.getStoredPath())));
        postRepository.delete(post);
    }

    @Transactional
    public PostDetailResponse addImages(Long postId, Long memberId, List<MultipartFile> files) {
        Post post = findPostWithImages(postId);
        post.checkAuthor(memberId);
        for (MultipartFile file : files) {
            StoredImage stored = imageStorage.store(file);
            eventPublisher.publishEvent(new ImageFileEvents.Stored(stored.storedPath()));
            post.addImage(memberId, stored.originalName(), stored.storedPath());
        }
        postRepository.flush();
        return PostDetailResponse.from(post);
    }

    @Transactional
    public void deleteImage(Long postId, Long imageId, Long memberId) {
        PostImage removed = findPostWithImages(postId).removeImage(memberId, imageId);
        eventPublisher.publishEvent(new ImageFileEvents.Discarded(removed.getStoredPath()));
    }

    private Post findPostWithImages(Long postId) {
        return postRepository.findWithImagesById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }
}
