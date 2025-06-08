package com.healthcare.link.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MeasurementUnit {
    KM("km"),
    KCAL("kcal")
    ;

    private final String value;

    MeasurementUnit(String value) {
        this.value = value;
    }

    @JsonCreator
    public static MeasurementUnit fromString(String value) {
        for (MeasurementUnit type : MeasurementUnit.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown measurement unit: " + value);
    }
}
