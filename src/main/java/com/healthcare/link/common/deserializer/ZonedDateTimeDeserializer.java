package com.healthcare.link.common.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.healthcare.link.common.constant.DateTimeConstant;
import com.healthcare.link.common.error.ErrorCode;
import com.healthcare.link.common.error.exception.BadRequestException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

public class ZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {

    @Override
    public ZonedDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getText().trim();

        // 주어지는 시간 데이터 포멧이 다양하기 때문에 지정된 포멧을 순회하며 파싱
        for (DateTimeFormatter formatter : DateTimeConstant.SUPPORTED_FORMATTERS) {
            try {
                TemporalAccessor parsed = formatter.parse(value);
                ZonedDateTime zonedDateTime = parsed.isSupported(ChronoField.OFFSET_SECONDS)
                        ? ZonedDateTime.from(parsed)
                        : LocalDateTime.from(parsed).atZone(ZoneOffset.UTC); // 시간대가 없는 경우 -> UTC 라고 가정

                return zonedDateTime.withZoneSameInstant(ZoneOffset.of(DateTimeConstant.KST_ZONE_OFFSET)); // KST로 변환

            } catch (DateTimeParseException ignored) {
            }
        }

        // 해당되는 포멧이 없는 경우 예외 발생
        throw new BadRequestException(ErrorCode.UNSUPPORTED_DATE_FORMAT);
    }
}
