package com.example.training.exception;

import com.example.training.dto.response.ApiErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFoundException(
            EntityNotFoundException ex,
            HttpServletRequest request
    )
    {
        return errorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidWorkloadException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidWorkloadException(
            InvalidWorkloadException ex,
            HttpServletRequest request
    )
    {
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    )
    {
        return errorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request
        );
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    )
    {
        Instant timestamp = Instant.now();

        ApiErrorResponse response = new ApiErrorResponse(
                timestamp,
                status,
                message,
                request.getRequestURI()
        );

        return new ResponseEntity<>(response, status);
    }
}
