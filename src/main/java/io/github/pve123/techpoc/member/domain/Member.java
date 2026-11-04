package io.github.pve123.techpoc.member.domain;

import io.github.pve123.techpoc.common.jpa.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 회원 애그리거트의 루트입니다.
 *
 * <p>비밀번호는 항상 암호화된 값만 받습니다. 원문 비밀번호는 이 클래스에 들어오지 않습니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String nickname;

    private String address;

    private LocalDate birthDate;

    @Column(length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    private MemberRole role;

    private Member(String email, String encodedPassword, String nickname, MemberRole role) {
        this.email = email;
        this.password = encodedPassword;
        this.nickname = nickname;
        this.role = role;
    }

    public static Member join(String email, String encodedPassword, String nickname) {
        return new Member(email, encodedPassword, nickname, MemberRole.USER);
    }

    public static Member admin(String email, String encodedPassword, String nickname) {
        return new Member(email, encodedPassword, nickname, MemberRole.ADMIN);
    }

    public void changeProfile(String nickname, String address, LocalDate birthDate, String phoneNumber) {
        this.nickname = nickname;
        this.address = address;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
