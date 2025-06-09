package com.healthcare.link.controller;

import com.healthcare.link.common.response.ApiResponse;
import com.healthcare.link.dto.request.StepsRecordRequestDto;
import com.healthcare.link.service.HealthRecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health Record", description = "Health Record API")
@RequestMapping("/api/health-records")
@RequiredArgsConstructor
@Validated
@RestController
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    @PostMapping("/steps")
    public ApiResponse<Boolean> saveSteps(
            @RequestHeader("userId") Long userId,
            @RequestBody @Valid StepsRecordRequestDto request
    ) {
        healthRecordService.saveSteps(request, userId);
        return ApiResponse.success(true);
    }
}
