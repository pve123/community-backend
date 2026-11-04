package io.github.pve123.techpoc.member.application.port;

import java.time.Duration;
import java.util.Optional;

/** 회원마다 지금 쓸 수 있는 리프레시 토큰 하나를 기억합니다. */
public interface RefreshTokenStore {

    void save(Long memberId, String refreshToken, Duration validity);

    Optional<String> find(Long memberId);

    void delete(Long memberId);
}
