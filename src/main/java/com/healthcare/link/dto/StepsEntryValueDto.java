package com.healthcare.link.dto;

import java.time.ZonedDateTime;

public record StepsEntryValueDto(
        Integer steps,
        Double distanceValue,
        Double caloriesValue,
        ZonedDateTime periodFrom
) { }
