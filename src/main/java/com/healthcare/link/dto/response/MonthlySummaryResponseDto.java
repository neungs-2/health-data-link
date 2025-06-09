package com.healthcare.link.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MonthlySummaryResponseDto(
        @Schema(description = "월", example = "2024-01")
        String date,

        @Schema(description = "걸음 수", example = "300000")
        Integer steps,

        @Schema(description = "칼로리", example = "15000.5")
        Double calories,

        @Schema(description = "거리", example = "225.5")
        Double distance,

        @Schema(description = "기록 ID", example = "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e")
        String recordkey,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "타임존", example = "+0900")
        String timezone
) {}
