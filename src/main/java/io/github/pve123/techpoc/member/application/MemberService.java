package io.github.pve123.techpoc.member.application;

import io.github.pve123.techpoc.common.error.BusinessException;
import io.github.pve123.techpoc.common.error.ErrorCode;
import io.github.pve123.techpoc.member.application.dto.MemberResponse;
import io.github.pve123.techpoc.member.application.dto.PasswordChangeRequest;
import io.github.pve123.techpoc.member.application.dto.ProfileUpdateRequest;
import io.github.pve123.techpoc.member.application.dto.SignUpRequest;
import io.github.pve123.techpoc.member.application.port.RefreshTokenStore;
import io.github.pve123.techpoc.member.domain.Member;
import io.github.pve123.techpoc.member.domain.MemberRepository;
import io.github.pve123.techpoc.member.domain.MemberRole;
import io.github.pve123.techpoc.member.domain.WithdrawnMember;
import io.github.pve123.techpoc.member.domain.WithdrawnMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final WithdrawnMemberRepository withdrawnMemberRepository;
    private final RefreshTokenStore refreshTokenStore;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_USED);
        }
        Member member = Member.join(request.email(), passwordEncoder.encode(request.password()), request.nickname());
        return MemberResponse.from(memberRepository.save(member));
    }

    public MemberResponse getMember(Long memberId) {
        return MemberResponse.from(findMember(memberId));
    }

    /** 관리자 화면용 목록입니다. 관리자 계정은 빼고 일반 회원만 보여 줍니다. */
    public Page<MemberResponse> getMembers(Pageable pageable) {
        return memberRepository.findByRole(MemberRole.USER, pageable).map(MemberResponse::from);
    }

    @Transactional
    public MemberResponse updateProfile(Long memberId, ProfileUpdateRequest request) {
        Member member = findMember(memberId);
        member.changeProfile(request.nickname(), request.address(), request.birthDate(), request.phoneNumber());
        return MemberResponse.from(member);
    }

    @Transactional
    public void changePassword(Long memberId, PasswordChangeRequest request) {
        Member member = findMember(memberId);
        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.WRONG_PASSWORD);
        }
        member.changePassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenStore.delete(memberId);
    }

    /**
     * 회원 정보를 지우고 탈퇴 기록만 남깁니다.
     * 이 회원이 쓴 글과 댓글은 지우지 않습니다. 글에는 작성 당시 닉네임이 남아 있습니다.
     */
    @Transactional
    public void withdraw(Long memberId) {
        Member member = findMember(memberId);
        memberRepository.delete(member);
        withdrawnMemberRepository.save(new WithdrawnMember(memberId, LocalDateTime.now()));
        refreshTokenStore.delete(memberId);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
