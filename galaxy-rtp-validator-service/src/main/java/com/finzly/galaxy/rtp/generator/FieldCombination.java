package com.finzly.galaxy.rtp.generator;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a combination of fields that can be included in a pacs.008 message
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldCombination {
    
    /**
     * Unique identifier for this combination
     */
    private String combinationId;
    
    /**
     * Set of field names included in this combination
     */
    private Set<String> includedFields;
    
    /**
     * Set of field names excluded in this combination
     */
    private Set<String> excludedFields;
    
    /**
     * Field values for this combination
     */
    private Map<String, Object> fieldValues;
    
    /**
     * Priority/weight for this combination (for ordering)
     */
    private int priority;
    
    /**
     * Description of this combination
     */
    private String description;
    
    /**
     * Whether this combination is valid according to business rules
     */
    private boolean valid;
    
    /**
     * Validation errors if any
     */
    private List<String> validationErrors;
    
    /**
     * Tags for categorizing combinations
     */
    private Set<String> tags;
    
    /**
     * Estimated complexity score (higher = more complex)
     */
    private int complexityScore;
    
    /**
     * Check if this combination includes a specific field
     */
    public boolean includesField(String fieldName) {
        return includedFields.contains(fieldName);
    }
    
    /**
     * Check if this combination excludes a specific field
     */
    public boolean excludesField(String fieldName) {
        return excludedFields.contains(fieldName);
    }
    
    /**
     * Get the value for a specific field in this combination
     */
    public Object getFieldValue(String fieldName) {
        return fieldValues.get(fieldName);
    }
    
    /**
     * Set the value for a specific field in this combination
     */
    public void setFieldValue(String fieldName, Object value) {
        fieldValues.put(fieldName, value);
    }
    
    /**
     * Calculate complexity score based on included fields
     */
    public int calculateComplexityScore() {
        int score = 0;
        
        // Base score for number of fields
        score += includedFields.size();
        
        // Additional score for complex field types
        for (String field : includedFields) {
            if (field.contains("Address") || field.contains("Agent")) {
                score += 2; // Address and Agent fields are more complex
            }
            if (field.contains("Account") || field.contains("Identification")) {
                score += 1; // Account and ID fields add complexity
            }
        }
        
        this.complexityScore = score;
        return score;
    }
    
    /**
     * Generate a human-readable description of this combination
     */
    public String generateDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("Combination with ").append(includedFields.size()).append(" fields: ");
        
        int count = 0;
        for (String field : includedFields) {
            if (count > 0) desc.append(", ");
            desc.append(field);
            count++;
            if (count >= 5) { // Limit description length
                desc.append("...");
                break;
            }
        }
        
        this.description = desc.toString();
        return this.description;
    }
    
    /**
     * Create a copy of this combination
     */
    public FieldCombination copy() {
        return FieldCombination.builder()
                .combinationId(this.combinationId)
                .includedFields(Set.copyOf(this.includedFields))
                .excludedFields(Set.copyOf(this.excludedFields))
                .fieldValues(Map.copyOf(this.fieldValues))
                .priority(this.priority)
                .description(this.description)
                .valid(this.valid)
                .validationErrors(List.copyOf(this.validationErrors))
                .tags(Set.copyOf(this.tags))
                .complexityScore(this.complexityScore)
                .build();
    }
}
