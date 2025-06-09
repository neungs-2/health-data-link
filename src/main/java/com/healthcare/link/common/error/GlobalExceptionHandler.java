package com.healthcare.link.common.error;

import com.healthcare.link.common.error.exception.BaseException;
import com.healthcare.link.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<ErrorResponse> handle(BaseException e) {
        log.error("BusinessException", e);
        return createErrorResponseEntity(e.getErrorCode());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handle(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException", e);
        return createErrorResponseEntity(ErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handle(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException", e);
        return createErrorResponseEntity(ErrorCode.INVALID_PARAMETER, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<ErrorResponse> handle(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException", e);
        return createErrorResponseEntity(ErrorCode.INTEGRITY_CONSTRAINT_VIOLATION_EXCEPTION);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handle(Exception e) {
        e.printStackTrace();
        log.error("Exception", e);
        return createErrorResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> createErrorResponseEntity(ErrorCode errorCode, String message) {
        return new ResponseEntity<>(
                ErrorResponse.of(errorCode, message),
                errorCode.getStatus());
    }

    private ResponseEntity<ErrorResponse> createErrorResponseEntity(ErrorCode errorCode) {
        return new ResponseEntity<>(
                ErrorResponse.of(errorCode),
                errorCode.getStatus());
    }
}
