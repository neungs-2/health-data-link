package com.healthcare.link.common.error.exception;

import com.healthcare.link.common.error.ErrorCode;

public class BadRequestException extends BaseException {
    public BadRequestException(ErrorCode errorCode) {
        super(errorCode.getMessage(), errorCode);
    }

    public BadRequestException() {
        super(ErrorCode.INVALID_PARAMETER);
    }
}
