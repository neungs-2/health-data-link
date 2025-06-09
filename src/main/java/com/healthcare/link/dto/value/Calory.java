package com.healthcare.link.dto.value;

import com.healthcare.link.common.error.ErrorCode;
import com.healthcare.link.common.error.exception.BadRequestException;
import com.healthcare.link.enums.MeasurementUnit;
import io.swagger.v3.oas.annotations.media.Schema;

public record Calory(
        @Schema(description = "측정 칼로리", example = "240.0")
        double value,

        @Schema(description = "측정 단위", example = "kcal")
        MeasurementUnit unit
) {
    public Calory {
        if (unit != MeasurementUnit.KCAL) {
            throw new BadRequestException(ErrorCode.MEASUREMENT_UNIT_MISMATCH);
        }
    }
}
