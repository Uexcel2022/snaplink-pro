package com.uexcel.snaplinkpro.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> validationErrors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        validationErrors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        ApiError error = ApiError.builder()
                .code(HttpStatus.BAD_REQUEST.name())
                .message("Validation failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .errors(validationErrors)
                .build();

        ApiResponse<ApiError> response = ApiResponse.<ApiError>builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .data(error)
                .build();

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler({InternalAuthenticationServiceException.class,
            BadCredentialsException.class})
    public ResponseEntity<ApiResponse<ApiError>> handleInvalidCredentials(
            InternalAuthenticationServiceException ex,
            HttpServletRequest request
    ) {
        ApiError error = ApiError.builder()
                .code("UNAUTHORIZED")
                .message("Invalid credentials")
                .status(HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI())
                .build();

        ApiResponse<ApiError> response = ApiResponse.<ApiError>builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .data(error)
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ApiError>> handleInternalServerError(
            Exception ex,
            HttpServletRequest request
    ) {
        ApiError error = ApiError.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("Something went wrong. Please try again later.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(request.getRequestURI())
                .build();

        ApiResponse<ApiError> response = ApiResponse.<ApiError>builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .data(error)
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

}
