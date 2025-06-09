package com.healthcare.link.common.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.healthcare.link.common.error.ErrorCode;
import com.healthcare.link.common.error.exception.BadRequestException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

public class ZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {
    private static final DateTimeFormatter[] FORMATTERS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
    };

    @Override
    public ZonedDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getText().trim();
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                TemporalAccessor parsed = formatter.parse(value);
                ZonedDateTime zonedDateTime = parsed.isSupported(ChronoField.OFFSET_SECONDS)
                        ? ZonedDateTime.from(parsed) // 시간대가 있는 경우 -> KST로 변환
                        : LocalDateTime.from(parsed).atZone(ZoneId.systemDefault()); // 시간대가 없는 경우 -> UTC 라고 가정, KST로 변환

                return zonedDateTime.withZoneSameInstant(ZoneOffset.of("+09:00"));

            } catch (DateTimeParseException ignored) {
            }
        }
        throw new BadRequestException(ErrorCode.UNSUPPORTED_DATE_FORMAT);
    }
}
