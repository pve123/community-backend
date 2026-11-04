package io.github.pve123.techpoc.member.application.dto;

import io.github.pve123.techpoc.member.domain.Member;
import io.github.pve123.techpoc.member.domain.MemberRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 회원 정보 응답입니다. 비밀번호는 담지 않습니다. */
public record MemberResponse(
        Long id,
        String email,
        String nickname,
        String address,
        LocalDate birthDate,
        String phoneNumber,
        MemberRole role,
        LocalDateTime createdAt
) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getAddress(),
                member.getBirthDate(),
                member.getPhoneNumber(),
                member.getRole(),
                member.getCreatedAt()
        );
    }
}
