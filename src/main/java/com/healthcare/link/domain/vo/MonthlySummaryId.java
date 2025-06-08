package com.healthcare.link.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public record MonthlySummaryId(
    Long userId,

    @Column(length = 36)
    String recordkey,

    @Column(length = 7)
    String date // Format: YYYY-MM

) implements Serializable { }