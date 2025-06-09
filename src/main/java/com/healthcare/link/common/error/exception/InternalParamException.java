package com.healthcare.link.common.error.exception;

import com.healthcare.link.common.error.ErrorCode;

public class InternalParamException extends BaseException {
    public InternalParamException(ErrorCode errorCode) {
        super(errorCode.getMessage(), errorCode);
    }

    public InternalParamException(String message) {
        super(message, ErrorCode.INVALID_INTERNAL_PARAMETER);
    }

    public InternalParamException() {
        super(ErrorCode.INVALID_INTERNAL_PARAMETER);
    }
}
