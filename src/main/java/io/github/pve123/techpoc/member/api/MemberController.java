package io.github.pve123.techpoc.member.api;

import io.github.pve123.techpoc.member.application.MemberService;
import io.github.pve123.techpoc.member.application.dto.MemberResponse;
import io.github.pve123.techpoc.member.application.dto.PasswordChangeRequest;
import io.github.pve123.techpoc.member.application.dto.ProfileUpdateRequest;
import io.github.pve123.techpoc.member.application.dto.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse signUp(@Valid @RequestBody SignUpRequest request) {
        return memberService.signUp(request);
    }

    @Operation(summary = "내 정보 보기")
    @GetMapping("/me")
    public MemberResponse getMe(@AuthenticationPrincipal Long memberId) {
        return memberService.getMember(memberId);
    }

    @Operation(summary = "내 정보 고치기")
    @PatchMapping("/me")
    public MemberResponse updateMe(@AuthenticationPrincipal Long memberId,
                                   @Valid @RequestBody ProfileUpdateRequest request) {
        return memberService.updateProfile(memberId, request);
    }

    @Operation(summary = "비밀번호 바꾸기", description = "바꾸고 나면 다른 기기의 로그인도 풀립니다.")
    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal Long memberId,
                               @Valid @RequestBody PasswordChangeRequest request) {
        memberService.changePassword(memberId, request);
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@AuthenticationPrincipal Long memberId) {
        memberService.withdraw(memberId);
    }
}
