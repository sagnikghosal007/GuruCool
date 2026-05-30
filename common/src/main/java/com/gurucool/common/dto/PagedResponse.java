package com.gurucool.common.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Builder
public class PagedResponse<T> {
    private final List<T> content;
    private final int page;
    private final int size;
    private final int totalPages;
    private final long totalElements;
    private final boolean last;
    private final boolean first;

    public static <T> PagedResponse<T> from(Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    public static <S, T> PagedResponse<T> from(Page<S> page, Function<S, T> mapper) {
        return PagedResponse.<T>builder()
                .content(page.getContent().stream().map(mapper).collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
