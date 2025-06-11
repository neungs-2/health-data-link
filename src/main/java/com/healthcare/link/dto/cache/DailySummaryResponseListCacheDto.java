package com.healthcare.link.dto.cache;

import com.healthcare.link.dto.response.DailySummaryResponseDto;

import java.io.Serializable;
import java.util.List;

public record DailySummaryResponseListCacheDto(
        List<DailySummaryResponseDto> item
) implements Serializable { }
