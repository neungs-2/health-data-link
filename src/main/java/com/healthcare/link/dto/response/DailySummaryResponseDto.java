package com.healthcare.link.dto.response;

import com.healthcare.link.domain.entity.DailySummary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record DailySummaryResponseDto(
        @Schema(description = "날짜", example = "2024-01-01")
        LocalDate daily,

        @Schema(description = "걸음 수", example = "10000")
        Integer steps,

        @Schema(description = "칼로리", example = "500.5")
        Double calories,

        @Schema(description = "거리", example = "7.5")
        Double distance,

        @Schema(description = "기록 ID", example = "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e")
        String recordkey,

        @Schema(description = "사용자 ID", example = "1")
        Long userId,

        @Schema(description = "타임존", example = "+0900")
        String timezone
) { }