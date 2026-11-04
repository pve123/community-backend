package io.github.pve123.techpoc.board.infrastructure;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.pve123.techpoc.board.application.dto.PostSummaryResponse;
import io.github.pve123.techpoc.board.application.port.PostQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static io.github.pve123.techpoc.board.domain.QPost.post;

@Repository
@RequiredArgsConstructor
class QuerydslPostQueryRepository implements PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostSummaryResponse> findSummaries(Long authorId, Pageable pageable) {
        List<PostSummaryResponse> content = queryFactory
                .select(Projections.constructor(PostSummaryResponse.class,
                        post.id,
                        post.title,
                        post.author.id,
                        post.author.nickname,
                        post.viewCount,
                        post.createdAt))
                .from(post)
                .where(writtenBy(authorId))
                .orderBy(post.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 마지막 페이지처럼 개수를 셀 필요가 없으면 count 쿼리를 건너뜁니다.
        return PageableExecutionUtils.getPage(content, pageable, () -> queryFactory
                .select(post.count())
                .from(post)
                .where(writtenBy(authorId))
                .fetchOne());
    }

    private BooleanExpression writtenBy(Long authorId) {
        return authorId == null ? null : post.author.id.eq(authorId);
    }
}
