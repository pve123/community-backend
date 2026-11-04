package io.github.pve123.techpoc.board.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * 게시판에서 바라본 "글쓴이"입니다.
 *
 * <p>회원 컨텍스트의 Member를 직접 참조하지 않고, 글을 쓸 때의 ID와 닉네임만 복사해 둡니다.
 * 그래서 회원이 탈퇴해도 글은 남고, 목록을 볼 때 회원 테이블과 조인하지 않아도 됩니다.
 * 대신 닉네임을 바꿔도 예전 글에는 예전 닉네임이 보입니다.
 */
@Embeddable
public record Author(
        @Column(name = "author_id", nullable = false) Long id,
        @Column(name = "author_nickname", nullable = false, length = 20) String nickname
) {

    public boolean isSameMember(Long memberId) {
        return id.equals(memberId);
    }
}
