package io.github.pve123.techpoc.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @param secret Base64로 인코딩한 서명 키. 환경변수 JWT_SECRET으로만 받습니다.
 */
@ConfigurationProperties("app.jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenValidity,
        Duration refreshTokenValidity
) {
}
