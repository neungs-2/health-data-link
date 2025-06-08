package com.healthcare.link.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record SourceId(
        @Column(length = 36)
        String recordkey,

        Long userId

) implements Serializable { }
