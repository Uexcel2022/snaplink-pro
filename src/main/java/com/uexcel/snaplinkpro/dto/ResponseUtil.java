package com.uexcel.snaplinkpro.dto;

import com.uexcel.snaplinkpro.exception.ApiResponse;

import java.time.LocalDateTime;

public class ResponseUtil {

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, PaginationMeta pagination) {
        return ApiResponse.<T>builder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(data)
                .pagination(pagination)
                .build();
    }
}
