package io.github.pve123.techpoc.member.application;

import io.github.pve123.techpoc.common.error.BusinessException;
import io.github.pve123.techpoc.common.error.ErrorCode;
import io.github.pve123.techpoc.member.application.port.MemberMailSender;
import io.github.pve123.techpoc.member.application.port.RefreshTokenStore;
import io.github.pve123.techpoc.member.application.port.VerificationCodeStore;
import io.github.pve123.techpoc.member.domain.Member;
import io.github.pve123.techpoc.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * 비밀번호를 잊은 회원에게 임시 비밀번호를 보내 줍니다.
 * <ol>
 *   <li>이메일로 6자리 인증 코드를 보냅니다.</li>
 *   <li>코드가 맞으면 그 회원의 비밀번호만 임시 비밀번호로 바꾸고 메일로 알려 줍니다.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    private static final Duration CODE_VALIDITY = Duration.ofMinutes(5);
    private static final int TEMPORARY_PASSWORD_LENGTH = 12;
    private static final String PASSWORD_CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";

    private final SecureRandom random = new SecureRandom();

    private final MemberRepository memberRepository;
    private final VerificationCodeStore verificationCodeStore;
    private final RefreshTokenStore refreshTokenStore;
    private final MemberMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    /** 가입하지 않은 이메일이어도 똑같이 성공으로 응답해서, 가입 여부를 알려 주지 않습니다. */
    public void sendCode(String email) {
        if (!memberRepository.existsByEmail(email)) {
            return;
        }
        String code = "%06d".formatted(random.nextInt(1_000_000));
        verificationCodeStore.save(email, code, CODE_VALIDITY);
        mailSender.sendVerificationCode(email, code);
    }

    @Transactional
    public void resetPassword(String email, String code) {
        boolean codeMatches = verificationCodeStore.find(email)
                .map(code::equals)
                .orElse(false);
        if (!codeMatches) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }
        verificationCodeStore.delete(email);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE));
        String temporaryPassword = createTemporaryPassword();
        member.changePassword(passwordEncoder.encode(temporaryPassword));
        refreshTokenStore.delete(member.getId());

        mailSender.sendTemporaryPassword(email, temporaryPassword);
    }

    private String createTemporaryPassword() {
        StringBuilder password = new StringBuilder(TEMPORARY_PASSWORD_LENGTH);
        for (int i = 0; i < TEMPORARY_PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARACTERS.charAt(random.nextInt(PASSWORD_CHARACTERS.length())));
        }
        return password.toString();
    }
}
