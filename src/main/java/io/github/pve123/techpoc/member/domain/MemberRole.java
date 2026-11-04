package io.github.pve123.techpoc.member.domain;

public enum MemberRole {
    USER,
    ADMIN;

    /** 스프링 시큐리티가 기대하는 "ROLE_" 접두어를 붙인 이름입니다. */
    public String authority() {
        return "ROLE_" + name();
    }
}
