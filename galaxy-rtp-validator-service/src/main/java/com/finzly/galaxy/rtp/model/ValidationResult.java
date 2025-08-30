package com.finzly.galaxy.rtp.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ValidationResult {
    private boolean isValid;
    private String messageType;
    private List<String> schemaErrors;
    private List<String> businessRuleErrors;
    private List<String> warnings;
    private List<ValidationError> validationErrors;
}
