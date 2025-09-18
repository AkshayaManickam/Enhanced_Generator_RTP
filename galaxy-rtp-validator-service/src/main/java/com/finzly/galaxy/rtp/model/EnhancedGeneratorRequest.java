package com.finzly.galaxy.rtp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnhancedGeneratorRequest {
    private String messageType;
    private int numberOfFiles;
    private List<String> selectedOptionalFields;
    private List<String> selectedConditionalFields;
    private Map<String, Object> customFieldValues;
    private boolean generateReport;
    private String reportFormat; // "excel" or "json"
    private String testScenario; // "basic", "comprehensive", "edge_cases"
    
    // Validation options
    private boolean validateAgainstXsd;
    private boolean includeFieldDescriptions;
    private boolean includeIsoDescriptions;
    
    // Generation options
    private String amountRange; // "small", "medium", "large"
    private String currency; // "USD", "EUR", etc.
    private String businessType; // "BUSINESS", "CONSUMER"
    private String serviceLevel; // "SDVA", etc.
}

