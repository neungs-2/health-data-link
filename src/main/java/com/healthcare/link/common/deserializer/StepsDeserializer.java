package com.healthcare.link.common.deserializer;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class StepsDeserializer extends JsonDeserializer<Integer> {
    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getText();
        try {
            if (value == null || value.trim().isEmpty()) {
                return 0;
            }

            // 문자열이 숫자 형태인지 확인
            double numericValue;
            try {
                numericValue = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return 0;
            }

            // 반올림 처리
            return (int) Math.round(numericValue);
        } catch (Exception e) {
            return 0;
        }
    }
}
