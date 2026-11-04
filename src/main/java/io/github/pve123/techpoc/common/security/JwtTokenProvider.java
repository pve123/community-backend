package io.github.pve123.techpoc.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * JWT를 만들고 읽습니다.
 *
 * <p>액세스 토큰과 리프레시 토큰은 같은 키로 서명하므로, type 값을 넣어 둘을 구분합니다.
 * 이렇게 하지 않으면 리프레시 토큰을 액세스 토큰 자리에 넣어도 통과해 버립니다.
 */
@Component
public class JwtTokenProvider {

    private static final String TYPE = "type";
    private static final String ROLE = "role";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final SecretKey key;
    private final JwtProperties properties;

    public JwtTokenProvider(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
        this.properties = properties;
    }

    public String createAccessToken(Long memberId, String role) {
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(TYPE, ACCESS)
                .claim(ROLE, role)
                .issuedAt(new Date())
                .expiration(expiresAfter(properties.accessTokenValidity()))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(TYPE, REFRESH)
                // 같은 초에 두 번 발급해도 서로 다른 토큰이 되도록 고유 ID를 넣습니다.
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(expiresAfter(properties.refreshTokenValidity()))
                .signWith(key)
                .compact();
    }

    public Optional<AccessTokenClaims> readAccessToken(String token) {
        return parse(token, ACCESS)
                .map(claims -> new AccessTokenClaims(
                        Long.valueOf(claims.getSubject()),
                        claims.get(ROLE, String.class)));
    }

    public Optional<Long> readRefreshToken(String token) {
        return parse(token, REFRESH).map(claims -> Long.valueOf(claims.getSubject()));
    }

    public Duration refreshTokenValidity() {
        return properties.refreshTokenValidity();
    }

    private Optional<Claims> parse(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token)
                    .getPayload();
            return expectedType.equals(claims.get(TYPE, String.class)) ? Optional.of(claims) : Optional.empty();
        } catch (JwtException | IllegalArgumentException invalidToken) {
            return Optional.empty();
        }
    }

    private static Date expiresAfter(Duration validity) {
        return Date.from(Instant.now().plus(validity));
    }

    public record AccessTokenClaims(Long memberId, String role) {
    }
}
