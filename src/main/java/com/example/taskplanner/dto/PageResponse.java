package com.example.taskplanner.dto;

import java.util.List;


public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static <T> PageResponse<T> of(List<T> all, int page, int size) {
        int total = all.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int from = (int) Math.min((long) page * size, total);
        int to = Math.min(from + size, total);

        return new PageResponse<>(
                List.copyOf(all.subList(from, to)),
                page,
                size,
                total,
                totalPages,
                page == 0,
                page >= totalPages - 1
        );
    }
}
