package com.healthcare.link.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.healthcare.link.common.deserializer.StepsDeserializer;
import com.healthcare.link.common.deserializer.UtcLocalDateTimeDeserializer;
import com.healthcare.link.dto.value.Calory;
import com.healthcare.link.dto.value.Distance;
import com.healthcare.link.enums.HealthRecordType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.List;

public record StepsRecordRequestDto(
        @Schema(description = "기록 ID", example = "7b012e6e-ba2b-49c7-bc2e-473b7b58e72e")
        @NotEmpty(message = "기록 ID는 필수값입니다.")
        String recordkey,

        @Schema(description = "걷기 기록 데이터")
        StepsHealthRecordDataDto data,

        @Schema(description = "기록 유형", example = "steps")
        HealthRecordType type,

        @Schema(description = "최근 업데이트일", example = "2024-12-15 12:40:00 +0000")
        @JsonDeserialize(using = UtcLocalDateTimeDeserializer.class)
        LocalDateTime lastUpdate
) {
    public record StepsHealthRecordDataDto(
            @Schema(description = "메모", example = "")
            String memo,

            @ArraySchema(schema = @Schema(description = "걷기 기록", implementation = StepsEntryDto.class))
            List<StepsEntryDto> entries,

            @Schema(description = "메타 데이터", example = "")
            SourceDto source

    ) {
        record SourceDto(
                @Schema(description = "디바이스 정보")
                ProductDataDto product,

                @Schema(description = "연동 APP 이름", example = "HealthApp")
                String name,

                @Schema(description = "모드", example = "10")
                Integer mode,

                @Schema(description = "타입")
                String type
        ) {
            record ProductDataDto(
                    @Schema(description = "단말기명", example = "iPhone")
                    String name,

                    @Schema(description = "단말기 제조사", example = "Apple inc,")
                    String vender
            ) { }
        }
    }

    record StepsEntryDto(
            @Schema(description = "step 수", example = "620.5034116508583")
            @JsonDeserialize(using = StepsDeserializer.class)
            Integer steps,

            @Schema(description = "기록 기간")
            PeriodDto period,

            @Schema(description = "측정 거리")
            Distance distance,

            @Schema(description = "측정 칼로리")
            Calory calories
    ) {

        record PeriodDto(
                @Schema(description = "측정 시작 시점", example = "2024-11-14T21:20:00+0000")
                @JsonDeserialize(using = UtcLocalDateTimeDeserializer.class)
                LocalDateTime from,

                @Schema(description = "측정 종료 시점", example = "2024-11-14T21:30:00+0000")
                @JsonDeserialize(using = UtcLocalDateTimeDeserializer.class)
                LocalDateTime to
        ) { }
    }
}
