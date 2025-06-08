package com.healthcare.link.common.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

public class UtcLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    private static final DateTimeFormatter[] FORMATTERS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
    };

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getText().trim();
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                TemporalAccessor parsed = formatter.parse(value);
                if (parsed.isSupported(ChronoField.OFFSET_SECONDS)) {
                    // 시간대가 있는 경우 -> UTC로 변환
                    return ZonedDateTime.from(parsed).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
                } else {
                    // 시간대가 없는 경우 -> UTC 라고 가정
                    return LocalDateTime.of(LocalDate.parse(value, formatter), LocalTime.MIDNIGHT);
                }

            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("지원하지 않는 데이터 형식: " + value);
    }
}
