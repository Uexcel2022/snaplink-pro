package com.uexcel.snaplinkpro.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleBase(
            BaseException ex,
            HttpServletRequest request) {

        HttpStatus status = ex.getStatus();

        ApiError error = ApiError.builder()
                .code(status.name())
                .message(ex.getMessage())
                .status(status.value())
                .path(request.getRequestURI())
                .build();

        ApiResponse<ApiError> response = ApiResponse.<ApiError>builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .data(error)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
