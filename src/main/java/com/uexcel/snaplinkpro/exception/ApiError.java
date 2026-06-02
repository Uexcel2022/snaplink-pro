package com.uexcel.snaplinkpro.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    private String code;
    private String message;
    private int status;
    private String path;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Map<String, String> errors;
}
