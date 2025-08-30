package com.finzly.galaxy.rtp.validator;

import lombok.Getter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced error handler for XML validation that collects all validation errors with ISO 20022 error codes
 */
public class ValidationErrorHandler implements ErrorHandler {
    
    @Getter
    private final List<String> errors = new ArrayList<>();
    
    @Getter
    private final List<ValidationError> validationErrors = new ArrayList<>();
    
    @Override
    public void warning(SAXParseException exception) throws SAXException {
        String error = String.format("Warning at line %d, column %d: %s", 
            exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
        errors.add(error);
        
        ValidationError validationError = ValidationError.fromMessage(
            exception.getMessage(), 
            String.format("Line %d, Column %d", exception.getLineNumber(), exception.getColumnNumber())
        );
        validationError.setSeverity("WARNING");
        validationErrors.add(validationError);
    }
    
    @Override
    public void error(SAXParseException exception) throws SAXException {
        String error = String.format("Error at line %d, column %d: %s", 
            exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
        errors.add(error);
        
        ValidationError validationError = ValidationError.fromMessage(
            exception.getMessage(), 
            String.format("Line %d, Column %d", exception.getLineNumber(), exception.getColumnNumber())
        );
        validationError.setSeverity("ERROR");
        validationErrors.add(validationError);
    }
    
    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        String error = String.format("Fatal Error at line %d, column %d: %s", 
            exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
        errors.add(error);
        
        ValidationError validationError = ValidationError.fromMessage(
            exception.getMessage(), 
            String.format("Line %d, Column %d", exception.getLineNumber(), exception.getColumnNumber())
        );
        validationError.setSeverity("FATAL");
        validationErrors.add(validationError);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }
    
    public void clearErrors() {
        errors.clear();
        validationErrors.clear();
    }
    
    /**
     * Get errors with ISO 20022 error codes
     */
    public List<ValidationError> getErrorsWithCodes() {
        return validationErrors;
    }
    
    /**
     * Get errors by severity
     */
    public List<ValidationError> getErrorsBySeverity(String severity) {
        return validationErrors.stream()
                .filter(error -> severity.equals(error.getSeverity()))
                .collect(java.util.stream.Collectors.toList());
    }
}
