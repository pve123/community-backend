package io.github.pve123.techpoc.board.application.port;

import io.github.pve123.techpoc.board.domain.Author;

/**
 * 회원 ID로 글쓴이 정보를 찾습니다.
 * 게시판이 회원 컨텍스트의 내부 구조를 몰라도 되도록 이 인터페이스 하나로만 연결합니다.
 */
public interface AuthorFinder {

    Author find(Long memberId);
}
