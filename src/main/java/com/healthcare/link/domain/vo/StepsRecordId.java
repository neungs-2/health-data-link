package com.healthcare.link.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record StepsRecordId(
        @Column(length = 36)
        String recordkey,

        @Column(name = "user_id")
        Long userId

) implements Serializable { }