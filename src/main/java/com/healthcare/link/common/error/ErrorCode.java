package com.healthcare.link.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "E001", "잘못된 요청입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "E002", "잘못된 HTTP 메서드를 호출했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "E003", "접근 권한이 없는 유저입니다."),
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "E004", "데이터를 찾을 수 없습니다."),
    NOT_EXIST_USER(HttpStatus.NOT_FOUND, "E005", "존재하지 않는 유저입니다."),
    UNSUPPORTED_DATE_FORMAT(HttpStatus.BAD_REQUEST, "E006", "지원하지 않는 날짜 형식입니다."),
    INVALID_INTERNAL_PARAMETER(HttpStatus.INTERNAL_SERVER_ERROR, "E998", "내부 파라미터가 잘못 전달되었습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E999", "서버 내부 오류");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
