package com.healthcare.link.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum HealthRecordType {
    STEPS("steps")
    ;

    private final String value;

    HealthRecordType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static HealthRecordType fromString(String value) {
        for (HealthRecordType type : HealthRecordType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown health record type: " + value);
    }
}
