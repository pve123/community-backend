package io.github.pve123.techpoc.member.api;

import io.github.pve123.techpoc.member.application.AuthService;
import io.github.pve123.techpoc.member.application.PasswordResetService;
import io.github.pve123.techpoc.member.application.dto.LoginRequest;
import io.github.pve123.techpoc.member.application.dto.PasswordResetCodeRequest;
import io.github.pve123.techpoc.member.application.dto.PasswordResetRequest;
import io.github.pve123.techpoc.member.application.dto.TokenReissueRequest;
import io.github.pve123.techpoc.member.application.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰은 한 번만 쓸 수 있고, 쓸 때마다 새것을 줍니다.")
    @PostMapping("/reissue")
    public TokenResponse reissue(@Valid @RequestBody TokenReissueRequest request) {
        return authService.reissue(request.refreshToken());
    }

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 지웁니다. 액세스 토큰은 만료될 때까지 유효합니다.")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Long memberId) {
        authService.logout(memberId);
    }

    @Operation(summary = "비밀번호 재설정 코드 받기")
    @PostMapping("/password-reset/code")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendPasswordResetCode(@Valid @RequestBody PasswordResetCodeRequest request) {
        passwordResetService.sendCode(request.email());
    }

    @Operation(summary = "비밀번호 재설정", description = "코드가 맞으면 임시 비밀번호를 메일로 보냅니다.")
    @PostMapping("/password-reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.resetPassword(request.email(), request.code());
    }
}
