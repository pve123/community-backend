package io.github.pve123.techpoc.member.infrastructure;

import io.github.pve123.techpoc.member.application.port.VerificationCodeStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class RedisVerificationCodeStore implements VerificationCodeStore {

    private static final String KEY_PREFIX = "password-reset-code:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String email, String code, Duration validity) {
        redisTemplate.opsForValue().set(KEY_PREFIX + email, code, validity);
    }

    @Override
    public Optional<String> find(String email) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + email));
    }

    @Override
    public void delete(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }
}
