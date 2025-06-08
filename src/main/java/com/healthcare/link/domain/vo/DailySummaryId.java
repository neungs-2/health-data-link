package com.healthcare.link.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
public record DailySummaryId(
    Long userId,

    @Column(length = 36)
    String recordkey,

    LocalDate date
) implements Serializable { }