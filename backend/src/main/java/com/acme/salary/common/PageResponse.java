package com.acme.salary.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Stable JSON shape for paginated results. We don't serialise Spring's Page directly
 * because its JSON structure is an implementation detail that can change between versions.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
