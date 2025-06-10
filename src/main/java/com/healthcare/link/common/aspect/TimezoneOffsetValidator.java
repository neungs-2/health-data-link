package com.healthcare.link.common.aspect;

import com.healthcare.link.common.error.ErrorCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class TimezoneOffsetValidator implements ConstraintValidator<TimezoneOffset, String> {

    private static final Pattern TIMEZONE_OFFSET_PATTERN = Pattern.compile("^[+-]\\d{2}:\\d{2}$");

    @Override
    public void initialize(TimezoneOffset constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(ErrorCode.INVALID_TIMEZONE_OFFSET.getMessage())
                .addConstraintViolation();

        // null 이면 Default 값 사용하므로 true 반환
        if (value == null) {
            return true;
        }

        if (!TIMEZONE_OFFSET_PATTERN.matcher(value).matches()) {
            return false;
        }

        try {
            String[] parts = value.substring(1).split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            return hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59;
        } catch (Exception e) {
            return false;
        }
    }
}
