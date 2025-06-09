package com.healthcare.link.dto.param;

public record StepsEntrySumDto(
        Integer totalSteps,
        Double totalCalories,
        Double totalDistance
) { }
