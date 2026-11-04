package io.github.pve123.techpoc.board.infrastructure;

import io.github.pve123.techpoc.board.application.port.AuthorFinder;
import io.github.pve123.techpoc.board.domain.Author;
import io.github.pve123.techpoc.common.error.BusinessException;
import io.github.pve123.techpoc.common.error.ErrorCode;
import io.github.pve123.techpoc.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 게시판과 회원 컨텍스트를 잇는 유일한 연결 지점입니다.
 * 회원 쪽 구조가 바뀌어도 이 클래스만 고치면 됩니다.
 */
@Component
@RequiredArgsConstructor
class MemberAuthorFinder implements AuthorFinder {

    private final MemberRepository memberRepository;

    @Override
    public Author find(Long memberId) {
        return memberRepository.findById(memberId)
                .map(member -> new Author(member.getId(), member.getNickname()))
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
