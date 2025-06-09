package com.healthcare.link.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE) // 인스턴스화 방지
public final class DateTimeConstant {
    public static final String KST_ZONE_OFFSET = "+09:00";

    public static final String DATETIME_PATTERN_WITH_ZONE = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String DATETIME_PATTERN_WITH_SPACE_ZONE = "yyyy-MM-dd HH:mm:ss Z";
    public static final String DATETIME_PATTERN_WITHOUT_ZONE = "yyyy-MM-dd HH:mm:ss";

    public static final DateTimeFormatter[] SUPPORTED_FORMATTERS = {
            DateTimeFormatter.ofPattern(DATETIME_PATTERN_WITH_ZONE),
            DateTimeFormatter.ofPattern(DATETIME_PATTERN_WITH_SPACE_ZONE),
            DateTimeFormatter.ofPattern(DATETIME_PATTERN_WITHOUT_ZONE),
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
    };
}
