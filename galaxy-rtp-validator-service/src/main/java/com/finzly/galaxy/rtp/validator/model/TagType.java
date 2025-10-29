package com.finzly.galaxy.rtp.validator.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TagType {
    MANDATORY("M"),
    OPTIONAL("O"),
    CONDITIONAL("C");

    private final String code;

    TagType(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public static TagType fromCode(String code) {
        for (TagType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown tag type code: " + code);
    }
}

