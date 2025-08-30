package com.finzly.galaxy.rtp.validator;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Enhanced validation error with ISO 20022 error codes
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationError {
    
    private String code;
    private String message;
    private String location;
    private String severity;
    private String category;
    
    public ValidationError(String code, String message) {
        this.code = code;
        this.message = message;
        this.severity = "ERROR";
        this.category = getCategoryFromCode(code);
    }
    
    public ValidationError(String code, String message, String location) {
        this.code = code;
        this.message = message;
        this.location = location;
        this.severity = "ERROR";
        this.category = getCategoryFromCode(code);
    }
    
    /**
     * Create validation error from message and auto-map to error code
     */
    public static ValidationError fromMessage(String message) {
        String code = ISO20022ErrorCodes.mapErrorToCode(message);
        return new ValidationError(code, message);
    }
    
    /**
     * Create validation error from message and location
     */
    public static ValidationError fromMessage(String message, String location) {
        String code = ISO20022ErrorCodes.mapErrorToCode(message);
        return new ValidationError(code, message, location);
    }
    
    /**
     * Get category from error code
     */
    private String getCategoryFromCode(String code) {
        if (code == null) return "UNKNOWN";
        
        if (code.startsWith("SCHEMA_")) {
            return ISO20022ErrorCodes.SCHEMA_VALIDATION;
        } else if (code.startsWith("BUSINESS_")) {
            return ISO20022ErrorCodes.BUSINESS_RULE;
        } else if (code.startsWith("FORMAT_")) {
            return ISO20022ErrorCodes.FORMAT_VALIDATION;
        } else if (code.startsWith("MANDATORY_")) {
            return ISO20022ErrorCodes.MANDATORY_FIELD;
        } else if (code.startsWith("INTEGRITY_")) {
            return ISO20022ErrorCodes.DATA_INTEGRITY;
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Get standardized error description
     */
    public String getStandardDescription() {
        return ISO20022ErrorCodes.getErrorDescription(this.code);
    }
    
    /**
     * Check if this is a schema validation error
     */
    public boolean isSchemaError() {
        return ISO20022ErrorCodes.SCHEMA_VALIDATION.equals(this.category);
    }
    
    /**
     * Check if this is a business rule error
     */
    public boolean isBusinessError() {
        return ISO20022ErrorCodes.BUSINESS_RULE.equals(this.category);
    }
    
    /**
     * Check if this is a format validation error
     */
    public boolean isFormatError() {
        return ISO20022ErrorCodes.FORMAT_VALIDATION.equals(this.category);
    }
    
    /**
     * Check if this is a mandatory field error
     */
    public boolean isMandatoryError() {
        return ISO20022ErrorCodes.MANDATORY_FIELD.equals(this.category);
    }
}
