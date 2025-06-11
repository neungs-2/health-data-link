package com.healthcare.link.dto.value;

import com.healthcare.link.common.error.ErrorCode;
import com.healthcare.link.common.error.exception.BadRequestException;
import com.healthcare.link.common.enums.MeasurementUnit;
import io.swagger.v3.oas.annotations.media.Schema;

public record Distance(
        @Schema(description = "측정 거리", example = "5.5")
        double value,

        @Schema(description = "측정 단위", example = "km")
        MeasurementUnit unit
) {
    public Distance {
        if (unit != MeasurementUnit.KM) {
            throw new BadRequestException(ErrorCode.MEASUREMENT_UNIT_MISMATCH);
        }
    }
}
