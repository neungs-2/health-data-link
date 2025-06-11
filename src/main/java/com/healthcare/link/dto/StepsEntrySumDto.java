package com.healthcare.link.dto;

public record StepsEntrySumDto(
        Integer totalSteps,
        Double totalCalories,
        Double totalDistance
) { }
