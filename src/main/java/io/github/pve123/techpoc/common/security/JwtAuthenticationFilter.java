package io.github.pve123.techpoc.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authorization 헤더의 액세스 토큰을 읽어 "누가 요청했는지"를 기록합니다.
 *
 * <p>컨트롤러에서는 {@code @AuthenticationPrincipal Long memberId}로 회원 ID를 꺼냅니다.
 * 요청 본문에 담긴 회원 ID는 믿지 않습니다.
 *
 * <p>빈으로 등록하면 서블릿 필터로도 한 번 더 걸리므로, SecurityConfig에서 직접 생성합니다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            tokenProvider.readAccessToken(header.substring(BEARER_PREFIX.length()))
                    .ifPresent(claims -> SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    claims.memberId(),
                                    null,
                                    List.of(new SimpleGrantedAuthority(claims.role())))));
        }
        chain.doFilter(request, response);
    }
}
