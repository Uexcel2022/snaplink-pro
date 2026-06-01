package com.uexcel.snaplinkpro.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    private String code;
    private String message;
    private int status;
    private String path;
}
