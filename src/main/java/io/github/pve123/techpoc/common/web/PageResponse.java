package io.github.pve123.techpoc.common.web;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 목록 응답의 공통 모양입니다. 클라이언트가 마지막 페이지를 알 수 있도록 전체 개수를 함께 보냅니다.
 * page는 1부터 셉니다.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
