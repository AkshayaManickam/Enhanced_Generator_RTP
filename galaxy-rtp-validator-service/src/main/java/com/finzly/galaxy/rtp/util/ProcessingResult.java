package com.finzly.galaxy.rtp.util;

import com.finzly.galaxy.rtp.model.Document;
import com.finzly.galaxy.rtp.validator.ValidationResult;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of XML processing operations
 */
@Data
public class ProcessingResult {
    
    private boolean success;
    private Document document;
    private ValidationResult validationResult;
    private List<String> errors;
    
    public ProcessingResult() {
        this.errors = new ArrayList<>();
    }
    
    public void addError(String error) {
        this.errors.add(error);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (success) {
            summary.append("XML processing completed successfully.\n");
        } else {
            summary.append("XML processing failed.\n");
        }
        
        if (validationResult != null) {
            summary.append("Validation: ").append(validationResult.getErrorSummary()).append("\n");
        }
        
        if (hasErrors()) {
            summary.append("Processing errors:\n");
            for (int i = 0; i < errors.size(); i++) {
                summary.append(i + 1).append(". ").append(errors.get(i)).append("\n");
            }
        }
        
        return summary.toString();
    }
}
