package io.github.pve123.techpoc.member.application;

import io.github.pve123.techpoc.common.error.BusinessException;
import io.github.pve123.techpoc.common.error.ErrorCode;
import io.github.pve123.techpoc.common.security.JwtTokenProvider;
import io.github.pve123.techpoc.member.application.dto.LoginRequest;
import io.github.pve123.techpoc.member.application.dto.TokenResponse;
import io.github.pve123.techpoc.member.application.port.RefreshTokenStore;
import io.github.pve123.techpoc.member.domain.Member;
import io.github.pve123.techpoc.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인과 토큰 재발급을 맡습니다.
 *
 * <p>리프레시 토큰은 쓸 때마다 새것으로 바꿉니다(rotation).
 * 저장소에 있는 최신 토큰과 다르면, 이미 쓴 토큰이 다시 들어온 것이므로 거절합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenStore refreshTokenStore;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public TokenResponse login(LoginRequest request) {
        // 이메일이 없을 때와 비밀번호가 틀릴 때 같은 오류를 내서, 가입 여부를 알려 주지 않습니다.
        Member member = memberRepository.findByEmail(request.email())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPassword()))
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));
        return issueTokens(member);
    }

    public TokenResponse reissue(String refreshToken) {
        Long memberId = tokenProvider.readRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        boolean isLatestToken = refreshTokenStore.find(memberId)
                .map(refreshToken::equals)
                .orElse(false);
        if (!isLatestToken) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        return issueTokens(member);
    }

    public void logout(Long memberId) {
        refreshTokenStore.delete(memberId);
    }

    private TokenResponse issueTokens(Member member) {
        String accessToken = tokenProvider.createAccessToken(member.getId(), member.getRole().authority());
        String refreshToken = tokenProvider.createRefreshToken(member.getId());
        refreshTokenStore.save(member.getId(), refreshToken, tokenProvider.refreshTokenValidity());
        return TokenResponse.bearer(accessToken, refreshToken);
    }
}
