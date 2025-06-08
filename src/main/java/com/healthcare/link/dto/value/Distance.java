package com.healthcare.link.dto.value;

import com.healthcare.link.enums.MeasurementUnit;
import io.swagger.v3.oas.annotations.media.Schema;

public record Distance(
        @Schema(description = "측정 거리", example = "5.5")
        double value,

        @Schema(description = "측정 단위", example = "km")
        MeasurementUnit unit
) {
    public Distance {
        if (unit != MeasurementUnit.KM) {
            throw new IllegalArgumentException("단위를 km로 지정해주세요.");
        }
    }
}
