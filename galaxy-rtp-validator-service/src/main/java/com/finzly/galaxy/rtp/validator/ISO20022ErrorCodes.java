package com.finzly.galaxy.rtp.validator;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * ISO 20022 Standard Error Codes for PACS.008 validation
 * Based on ISO 20022 standards and common validation scenarios
 */
public class ISO20022ErrorCodes {
    
    // Error code categories
    public static final String SCHEMA_VALIDATION = "SCHEMA";
    public static final String BUSINESS_RULE = "BUSINESS";
    public static final String FORMAT_VALIDATION = "FORMAT";
    public static final String MANDATORY_FIELD = "MANDATORY";
    public static final String DATA_INTEGRITY = "INTEGRITY";
    
    // Schema validation error codes
    public static final String SCHEMA_001 = "SCHEMA_001"; // XML Schema validation failed
    public static final String SCHEMA_002 = "SCHEMA_002"; // Element not allowed
    public static final String SCHEMA_003 = "SCHEMA_003"; // Missing required element
    public static final String SCHEMA_004 = "SCHEMA_004"; // Invalid element structure
    public static final String SCHEMA_005 = "SCHEMA_005"; // Namespace validation failed
    
    // Business rule error codes
    public static final String BUSINESS_001 = "BUSINESS_001"; // Invalid IBAN format
    public static final String BUSINESS_002 = "BUSINESS_002"; // Invalid BIC format
    public static final String BUSINESS_003 = "BUSINESS_003"; // Invalid currency code
    public static final String BUSINESS_004 = "BUSINESS_004"; // Invalid amount format
    public static final String BUSINESS_005 = "BUSINESS_005"; // Invalid date format
    public static final String BUSINESS_006 = "BUSINESS_006"; // Invalid instruction ID format
    public static final String BUSINESS_007 = "BUSINESS_007"; // Invalid remittance ID format
    public static final String BUSINESS_008 = "BUSINESS_008"; // Invalid settlement method
    public static final String BUSINESS_009 = "BUSINESS_009"; // Invalid charge bearer
    public static final String BUSINESS_010 = "BUSINESS_010"; // Invalid purpose code
    public static final String BUSINESS_011 = "BUSINESS_011"; // Invalid Message ID format
    
    // Mandatory field error codes
    public static final String MANDATORY_001 = "MANDATORY_001"; // Missing mandatory field
    public static final String MANDATORY_002 = "MANDATORY_002"; // Missing conditional mandatory field
    public static final String MANDATORY_003 = "MANDATORY_003"; // Empty mandatory field
    
    // Format validation error codes
    public static final String FORMAT_001 = "FORMAT_001"; // Invalid field length
    public static final String FORMAT_002 = "FORMAT_002"; // Invalid character set
    public static final String FORMAT_003 = "FORMAT_003"; // Invalid field pattern
    public static final String FORMAT_004 = "FORMAT_004"; // Invalid field value
    
    // Data integrity error codes
    public static final String INTEGRITY_001 = "INTEGRITY_001"; // Data consistency error
    public static final String INTEGRITY_002 = "INTEGRITY_002"; // Cross-field validation failed
    public static final String INTEGRITY_003 = "INTEGRITY_003"; // Business logic violation
    
    private static final Map<String, String> errorDescriptions = new HashMap<>();
    private static final Map<Pattern, String> errorPatterns = new HashMap<>();
    
    static {
        // Initialize error descriptions
        errorDescriptions.put(SCHEMA_001, "XML Schema validation failed");
        errorDescriptions.put(SCHEMA_002, "Element not allowed in current context");
        errorDescriptions.put(SCHEMA_003, "Missing required element");
        errorDescriptions.put(SCHEMA_004, "Invalid element structure");
        errorDescriptions.put(SCHEMA_005, "Namespace validation failed");
        
        errorDescriptions.put(BUSINESS_001, "Invalid IBAN format - must be 15-34 alphanumeric characters");
        errorDescriptions.put(BUSINESS_002, "Invalid BIC format - must be 8 or 11 alphanumeric characters");
        errorDescriptions.put(BUSINESS_003, "Invalid currency code - must be 3 alphabetic characters");
        errorDescriptions.put(BUSINESS_004, "Invalid amount format - must be a valid decimal number");
        errorDescriptions.put(BUSINESS_005, "Invalid date format - must be in ISO format");
        errorDescriptions.put(BUSINESS_006, "Invalid instruction ID format - contains invalid characters");
        errorDescriptions.put(BUSINESS_007, "Invalid remittance ID format - contains invalid characters");
        errorDescriptions.put(BUSINESS_008, "Invalid settlement method");
        errorDescriptions.put(BUSINESS_009, "Invalid charge bearer");
        errorDescriptions.put(BUSINESS_010, "Invalid purpose code");
        errorDescriptions.put(BUSINESS_011, "Invalid Message ID format - contains invalid characters or sequences");
        
        errorDescriptions.put(MANDATORY_001, "Missing mandatory field");
        errorDescriptions.put(MANDATORY_002, "Missing conditional mandatory field");
        errorDescriptions.put(MANDATORY_003, "Empty mandatory field");
        
        errorDescriptions.put(FORMAT_001, "Invalid field length");
        errorDescriptions.put(FORMAT_002, "Invalid character set");
        errorDescriptions.put(FORMAT_003, "Invalid field pattern");
        errorDescriptions.put(FORMAT_004, "Invalid field value");
        
        errorDescriptions.put(INTEGRITY_001, "Data consistency error");
        errorDescriptions.put(INTEGRITY_002, "Cross-field validation failed");
        errorDescriptions.put(INTEGRITY_003, "Business logic violation");
        
        // Initialize error patterns for automatic mapping
        errorPatterns.put(Pattern.compile(".*element.*must be terminated.*", Pattern.CASE_INSENSITIVE), SCHEMA_001);
        errorPatterns.put(Pattern.compile(".*element.*not allowed.*", Pattern.CASE_INSENSITIVE), SCHEMA_002);
        errorPatterns.put(Pattern.compile(".*missing.*mandatory.*", Pattern.CASE_INSENSITIVE), MANDATORY_001);
        errorPatterns.put(Pattern.compile(".*invalid.*iban.*", Pattern.CASE_INSENSITIVE), BUSINESS_001);
        errorPatterns.put(Pattern.compile(".*invalid.*bic.*", Pattern.CASE_INSENSITIVE), BUSINESS_002);
        errorPatterns.put(Pattern.compile(".*invalid.*currency.*", Pattern.CASE_INSENSITIVE), BUSINESS_003);
        errorPatterns.put(Pattern.compile(".*invalid.*amount.*", Pattern.CASE_INSENSITIVE), BUSINESS_004);
        errorPatterns.put(Pattern.compile(".*invalid.*date.*", Pattern.CASE_INSENSITIVE), BUSINESS_005);
        errorPatterns.put(Pattern.compile(".*invalid.*instruction.*id.*", Pattern.CASE_INSENSITIVE), BUSINESS_006);
        errorPatterns.put(Pattern.compile(".*invalid.*remittance.*id.*", Pattern.CASE_INSENSITIVE), BUSINESS_007);
        errorPatterns.put(Pattern.compile(".*invalid.*message.*id.*", Pattern.CASE_INSENSITIVE), BUSINESS_011);
        errorPatterns.put(Pattern.compile(".*exceeds.*maxLength.*", Pattern.CASE_INSENSITIVE), FORMAT_001);
        errorPatterns.put(Pattern.compile(".*contains.*invalid.*characters.*", Pattern.CASE_INSENSITIVE), FORMAT_002);
    }
    
    /**
     * Get error description for a given error code
     */
    public static String getErrorDescription(String errorCode) {
        return errorDescriptions.getOrDefault(errorCode, "Unknown error code: " + errorCode);
    }
    
    /**
     * Map error message to standard ISO 20022 error code
     */
    public static String mapErrorToCode(String errorMessage) {
        for (Map.Entry<Pattern, String> entry : errorPatterns.entrySet()) {
            if (entry.getKey().matcher(errorMessage).matches()) {
                return entry.getValue();
            }
        }
        
        // Default mappings based on common patterns
        if (errorMessage.toLowerCase().contains("schema")) {
            return SCHEMA_001;
        } else if (errorMessage.toLowerCase().contains("mandatory")) {
            return MANDATORY_001;
        } else if (errorMessage.toLowerCase().contains("format")) {
            return FORMAT_001;
        } else if (errorMessage.toLowerCase().contains("business")) {
            return BUSINESS_001;
        }
        
        return SCHEMA_001; // Default to schema validation error
    }
    
    /**
     * Get all error codes for a specific category
     */
    public static String[] getErrorCodesByCategory(String category) {
        return errorDescriptions.keySet().stream()
                .filter(code -> code.startsWith(category))
                .toArray(String[]::new);
    }
    
    /**
     * Check if an error code is valid
     */
    public static boolean isValidErrorCode(String errorCode) {
        return errorDescriptions.containsKey(errorCode);
    }
}
