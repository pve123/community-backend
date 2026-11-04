package io.github.pve123.techpoc.board.application;

import io.github.pve123.techpoc.board.application.dto.CommentRequest;
import io.github.pve123.techpoc.board.application.dto.CommentResponse;
import io.github.pve123.techpoc.board.application.port.AuthorFinder;
import io.github.pve123.techpoc.board.domain.Author;
import io.github.pve123.techpoc.board.domain.Comment;
import io.github.pve123.techpoc.board.domain.CommentRepository;
import io.github.pve123.techpoc.board.domain.PostRepository;
import io.github.pve123.techpoc.board.application.port.CommentQueryRepository;
import io.github.pve123.techpoc.common.error.BusinessException;
import io.github.pve123.techpoc.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentQueryRepository commentQueryRepository;
    private final PostRepository postRepository;
    private final AuthorFinder authorFinder;

    @Transactional
    public CommentResponse write(Long postId, Long memberId, CommentRequest request) {
        checkPostExists(postId);
        Author author = authorFinder.find(memberId);
        Comment comment = request.parentId() == null
                ? Comment.write(postId, author, request.content())
                : Comment.reply(findComment(request.parentId()), postId, author, request.content());
        return CommentResponse.from(commentRepository.save(comment));
    }

    /** 게시글의 댓글을 답글 수와 함께 보여 줍니다. 답글 본문은 getReplies로 따로 불러옵니다. */
    public Page<CommentResponse> getComments(Long postId, Pageable pageable) {
        checkPostExists(postId);
        return commentQueryRepository.findCommentsWithReplyCount(postId, pageable);
    }

    public Page<CommentResponse> getReplies(Long commentId, Pageable pageable) {
        findComment(commentId);
        return commentRepository.findByParentId(commentId, pageable).map(CommentResponse::from);
    }

    private void checkPostExists(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
    }

    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }
}
