package com.healthcare.link.common.error.exception;

import com.healthcare.link.common.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage(), errorCode);
    }

    public ResourceNotFoundException() {
        super(ErrorCode.DATA_NOT_FOUND);
    }
}
