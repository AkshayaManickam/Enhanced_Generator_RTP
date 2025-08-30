package com.finzly.galaxy.rtp.validator;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced result of XML validation against XSD schema with ISO 20022 error codes
 */
@Data
public class ValidationResult {
    
    private boolean valid;
    private List<String> errors;
    private List<ValidationError> validationErrors;
    private String messageType;
    private List<String> schemaErrors;
    private List<String> businessRuleErrors;
    private List<String> warnings;
    
    public ValidationResult() {
        this.errors = new ArrayList<>();
        this.validationErrors = new ArrayList<>();
        this.schemaErrors = new ArrayList<>();
        this.businessRuleErrors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }
    
    public ValidationResult(boolean valid) {
        this.valid = valid;
        this.errors = new ArrayList<>();
        this.validationErrors = new ArrayList<>();
        this.schemaErrors = new ArrayList<>();
        this.businessRuleErrors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }
    
    public void addError(String error) {
        this.errors.add(error);
        this.validationErrors.add(ValidationError.fromMessage(error));
    }
    
    public void addError(ValidationError error) {
        this.validationErrors.add(error);
        this.errors.add(error.getMessage());
    }
    
    public void addSchemaError(String error) {
        this.schemaErrors.add(error);
        ValidationError validationError = ValidationError.fromMessage(error);
        validationError.setCategory(ISO20022ErrorCodes.SCHEMA_VALIDATION);
        this.validationErrors.add(validationError);
    }
    
    public void addBusinessRuleError(String error) {
        this.businessRuleErrors.add(error);
        ValidationError validationError = ValidationError.fromMessage(error);
        validationError.setCategory(ISO20022ErrorCodes.BUSINESS_RULE);
        this.validationErrors.add(validationError);
    }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
    
    public void setErrors(List<String> errors) {
        this.errors = errors;
        this.validationErrors = errors.stream()
                .map(ValidationError::fromMessage)
                .collect(Collectors.toList());
    }
    
    public void setValidationErrors(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
        this.errors = validationErrors.stream()
                .map(ValidationError::getMessage)
                .collect(Collectors.toList());
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }
    
    public List<ValidationError> getSchemaValidationErrors() {
        return validationErrors.stream()
                .filter(ValidationError::isSchemaError)
                .collect(Collectors.toList());
    }
    
    public List<ValidationError> getBusinessRuleErrors() {
        return validationErrors.stream()
                .filter(ValidationError::isBusinessError)
                .collect(Collectors.toList());
    }
    
    public List<ValidationError> getFormatErrors() {
        return validationErrors.stream()
                .filter(ValidationError::isFormatError)
                .collect(Collectors.toList());
    }
    
    public List<ValidationError> getMandatoryErrors() {
        return validationErrors.stream()
                .filter(ValidationError::isMandatoryError)
                .collect(Collectors.toList());
    }
    
    public String getErrorSummary() {
        if (errors.isEmpty()) {
            return "No validation errors";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Validation failed with ").append(errors.size()).append(" error(s):\n");
        for (int i = 0; i < validationErrors.size(); i++) {
            ValidationError error = validationErrors.get(i);
            summary.append(i + 1).append(". [").append(error.getCode()).append("] ").append(error.getMessage()).append("\n");
        }
        return summary.toString();
    }
    
    public String getDetailedErrorSummary() {
        if (validationErrors.isEmpty()) {
            return "No validation errors";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Validation failed with ").append(validationErrors.size()).append(" error(s):\n\n");
        
        // Group errors by category
        summary.append("Schema Validation Errors:\n");
        getSchemaValidationErrors().forEach(error -> 
            summary.append("  [").append(error.getCode()).append("] ").append(error.getMessage()).append("\n"));
        
        summary.append("\nBusiness Rule Errors:\n");
        getBusinessRuleErrors().forEach(error -> 
            summary.append("  [").append(error.getCode()).append("] ").append(error.getMessage()).append("\n"));
        
        summary.append("\nFormat Validation Errors:\n");
        getFormatErrors().forEach(error -> 
            summary.append("  [").append(error.getCode()).append("] ").append(error.getMessage()).append("\n"));
        
        summary.append("\nMandatory Field Errors:\n");
        getMandatoryErrors().forEach(error -> 
            summary.append("  [").append(error.getCode()).append("] ").append(error.getMessage()).append("\n"));
        
        return summary.toString();
    }
}
