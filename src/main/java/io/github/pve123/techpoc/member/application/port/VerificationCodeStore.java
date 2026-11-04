package io.github.pve123.techpoc.member.application.port;

import java.time.Duration;
import java.util.Optional;

/** 비밀번호 재설정용 인증 코드를 잠깐 보관합니다. */
public interface VerificationCodeStore {

    void save(String email, String code, Duration validity);

    Optional<String> find(String email);

    void delete(String email);
}
