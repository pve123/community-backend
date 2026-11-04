package io.github.pve123.techpoc.board.application.port;

/** 같은 회원이 같은 글을 여러 번 열어도 조회수가 한 번만 오르도록 기록합니다. */
public interface PostViewHistory {

    /** 처음 보는 글이면 기록하고 true, 이미 봤으면 false를 돌려줍니다. */
    boolean markViewed(Long postId, Long memberId);
}
