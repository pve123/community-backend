package io.github.pve123.techpoc.board.infrastructure;

import io.github.pve123.techpoc.board.application.port.PostViewHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 조회 기록을 Redis에 하루 동안 남깁니다. 하루가 지나면 같은 회원이 다시 봐도 조회수가 오릅니다.
 *
 * <p>SET NX(없을 때만 저장)는 한 번에 실행되므로, 같은 회원이 동시에 두 번 열어도 한 번만 true가 나옵니다.
 */
@Component
@RequiredArgsConstructor
class RedisPostViewHistory implements PostViewHistory {

    private static final Duration REMEMBER_FOR = Duration.ofDays(1);

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean markViewed(Long postId, Long memberId) {
        String key = "post-view:%d:%d".formatted(postId, memberId);
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "1", REMEMBER_FOR));
    }
}
