package com.healthcare.link.dto.cache;

import com.healthcare.link.dto.response.MonthlySummaryResponseDto;

import java.io.Serializable;
import java.util.List;

public record MonthlySummaryResponseListCacheDto(
        List<MonthlySummaryResponseDto> item
) implements Serializable { }
