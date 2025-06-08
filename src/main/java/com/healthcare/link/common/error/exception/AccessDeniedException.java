package com.healthcare.link.common.error.exception;

import com.healthcare.link.common.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class AccessDeniedException extends BaseException {
    public AccessDeniedException(ErrorCode errorCode) {
        super(errorCode.getMessage(), errorCode);
    }

    public AccessDeniedException() {
        super(ErrorCode.FORBIDDEN);
    }
}
