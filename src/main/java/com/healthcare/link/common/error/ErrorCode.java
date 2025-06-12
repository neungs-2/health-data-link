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
    MEASUREMENT_UNIT_MISMATCH(HttpStatus.BAD_REQUEST, "E007", "올바르지 않은 측정 단위입니다."),
    INVALID_TIMEZONE_OFFSET(HttpStatus.BAD_REQUEST, "E009", "유효하지 않은 타임존 오프셋입니다. 올바른 형식은 +HH:MM 또는 -HH:MM 입니다. (HH <= 18)"),
    INVALID_STEPS(HttpStatus.BAD_REQUEST, "E008", "유효하지 않은 걸음 수입니다."),
    INTEGRITY_CONSTRAINT_VIOLATION_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "E997", "DB 무결성 제약 조건 위반입니다."),
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
