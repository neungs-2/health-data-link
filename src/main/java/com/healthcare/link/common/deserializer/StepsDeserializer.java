package com.healthcare.link.common.deserializer;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.healthcare.link.common.error.ErrorCode;
import com.healthcare.link.common.error.exception.BadRequestException;

import java.io.IOException;

public class StepsDeserializer extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getText();
        try {
            // 입력값 null 또는 빈 문자열 검증
            if (value == null || value.trim().isEmpty()) {
                throw new BadRequestException(ErrorCode.INVALID_STEPS);
            }

            // 문자열이 숫자 형태인지 검증 후 반올림 처리
            try {
                double numericValue = Double.parseDouble(value);
                return (int) Math.round(numericValue); // 반올림
            } catch (NumberFormatException e) {
                throw new BadRequestException(ErrorCode.INVALID_STEPS);
            }

        } catch (Exception e) {
            return 0;
        }
    }
}
