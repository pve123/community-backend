package io.github.pve123.techpoc.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 탈퇴 기록입니다. 개인정보는 남기지 않고 "누가 언제 탈퇴했는지"만 남깁니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawnMember {

    @Id
    private Long memberId;

    @Column(nullable = false)
    private LocalDateTime withdrawnAt;

    public WithdrawnMember(Long memberId, LocalDateTime withdrawnAt) {
        this.memberId = memberId;
        this.withdrawnAt = withdrawnAt;
    }
}
