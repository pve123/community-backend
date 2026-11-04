package io.github.pve123.techpoc.board.infrastructure;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.pve123.techpoc.board.application.dto.CommentResponse;
import io.github.pve123.techpoc.board.application.port.CommentQueryRepository;
import io.github.pve123.techpoc.board.domain.QComment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static io.github.pve123.techpoc.board.domain.QComment.comment;

@Repository
@RequiredArgsConstructor
class QuerydslCommentQueryRepository implements CommentQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CommentResponse> findCommentsWithReplyCount(Long postId, Pageable pageable) {
        QComment reply = new QComment("reply");

        // 댓글마다 답글 수를 따로 세면 N+1 쿼리가 되므로, 서브쿼리로 한 번에 셉니다.
        List<CommentResponse> content = queryFactory
                .select(Projections.constructor(CommentResponse.class,
                        comment.id,
                        comment.parentId,
                        comment.author.id,
                        comment.author.nickname,
                        comment.content,
                        JPAExpressions.select(reply.count()).from(reply).where(reply.parentId.eq(comment.id)),
                        comment.createdAt))
                .from(comment)
                .where(comment.postId.eq(postId), comment.parentId.isNull())
                .orderBy(comment.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, () -> queryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.postId.eq(postId), comment.parentId.isNull())
                .fetchOne());
    }
}
