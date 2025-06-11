package com.healthcare.link.controller;

import com.healthcare.link.common.constant.DateTimeConstant;
import com.healthcare.link.common.response.ApiResponse;
import com.healthcare.link.common.aspect.TimezoneOffset;
import com.healthcare.link.dto.request.StepsRecordRequestDto;
import com.healthcare.link.dto.response.DailySummaryResponseDto;
import com.healthcare.link.dto.response.MonthlySummaryResponseDto;
import com.healthcare.link.service.HealthRecordService;
import com.healthcare.link.service.cache.HealthRecordCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Health Record", description = "Health Record API")
@RequestMapping("/api/health-records")
@RequiredArgsConstructor
@Validated
@RestController
public class HealthRecordController {

    private final HealthRecordService healthRecordService;
    private final HealthRecordCacheService healthRecordCacheService;

    @PostMapping("/steps")
    @Operation(summary = "걸음 수 저장", description = "걸음 수 데이터를 저장합니다.")
    public ApiResponse<Boolean> saveSteps(
            @RequestHeader("userId") Long userId,
            @RequestBody @Valid StepsRecordRequestDto request
    ) {
        // 외부 시스템 데이터 연동 -> Retry 방지를 위해 멱등성 키 적용
        healthRecordCacheService.saveStepsWithIdempotentCheck(request, userId);
        return ApiResponse.success();
    }

    @GetMapping("/daily-summaries/{recordkey}")
    @Operation(summary = "일별 요약 조회", description = "recordkey와 userId로 일별 요약 데이터를 조회합니다. 타임존이 다르면 재계산합니다.")
    public ApiResponse<List<DailySummaryResponseDto>> getDailySummaries(
            @RequestHeader("userId") Long userId,
            @PathVariable String recordkey,
            @TimezoneOffset @RequestParam(defaultValue = DateTimeConstant.KST_ZONE_OFFSET) String timezone
    ) {
        List<DailySummaryResponseDto> dailySummaries = healthRecordCacheService.getDailySummariesWithCache(recordkey, timezone, userId);
        return ApiResponse.success(dailySummaries);
    }

    @GetMapping("/monthly-summaries/{recordkey}")
    @Operation(summary = "월별 요약 조회", description = "recordkey와 userId로 월별 요약 데이터를 조회합니다. 타임존이 다르면 재계산합니다.")
    public ApiResponse<List<MonthlySummaryResponseDto>> getMonthlySummaries(
            @RequestHeader("userId") Long userId,
            @PathVariable String recordkey,
            @TimezoneOffset @RequestParam(defaultValue = DateTimeConstant.KST_ZONE_OFFSET) String timezone
    ) {
        List<MonthlySummaryResponseDto> monthlySummaries = healthRecordCacheService.getMonthlySummariesWithCache(recordkey, timezone, userId);
        return ApiResponse.success(monthlySummaries);
    }
}
