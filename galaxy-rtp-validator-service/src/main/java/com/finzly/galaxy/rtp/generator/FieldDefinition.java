package com.finzly.galaxy.rtp.generator;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents a field definition in the pacs.008 message structure
 * Based on TCH RTP Message Specification v5.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldDefinition {
    
    /**
     * Field name (e.g., "MsgId", "CreDtTm")
     */
    private String fieldName;
    
    /**
     * Full XPath to the field in the XML structure
     */
    private String xpath;
    
    /**
     * Field type: MANDATORY, OPTIONAL, CONDITIONAL
     */
    private FieldType fieldType;
    
    /**
     * Data type (e.g., "Max35Text", "ISODateTime", "ActiveCurrencyAndAmount")
     */
    private String dataType;
    
    /**
     * Maximum length constraint
     */
    private Integer maxLength;
    
    /**
     * Minimum length constraint
     */
    private Integer minLength;
    
    /**
     * Pattern constraint (regex)
     */
    private String pattern;
    
    /**
     * Allowed values for enumerated fields
     */
    private List<String> allowedValues;
    
    /**
     * Default value if any
     */
    private String defaultValue;
    
    /**
     * Parent field name for nested structures
     */
    private String parentField;
    
    /**
     * Child fields for complex types
     */
    private List<FieldDefinition> childFields;
    
    /**
     * Conditional rules (when this field is required)
     */
    private List<ConditionalRule> conditionalRules;
    
    /**
     * Field description from specification
     */
    private String description;
    
    /**
     * Business rules and constraints
     */
    private List<String> businessRules;
    
    /**
     * Example values for testing
     */
    private List<String> exampleValues;
    
    /**
     * Whether this field can have multiple values
     */
    private boolean multipleValues;
    
    /**
     * Field category for grouping
     */
    private FieldCategory category;
    
    /**
     * Additional attributes for the field
     */
    private Map<String, String> attributes;
    
    public enum FieldType {
        MANDATORY("M"),
        OPTIONAL("O"), 
        CONDITIONAL("C");
        
        private final String code;
        
        FieldType(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
        
        public static FieldType fromCode(String code) {
            for (FieldType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            return OPTIONAL; // Default fallback
        }
    }
    
    public enum FieldCategory {
        GROUP_HEADER,
        PAYMENT_IDENTIFICATION,
        PAYMENT_TYPE_INFO,
        SETTLEMENT_INFO,
        CREDIT_TRANSFER,
        PARTY_IDENTIFICATION,
        ACCOUNT_INFO,
        ADDRESS_INFO,
        AGENT_INFO,
        AMOUNT_INFO,
        OTHER
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConditionalRule {
        private String condition;
        private String description;
        private boolean required;
    }
    
    /**
     * Check if this field should be included based on conditional rules
     */
    public boolean shouldInclude(Map<String, Object> context) {
        if (fieldType == FieldType.MANDATORY) {
            return true;
        }
        
        if (fieldType == FieldType.OPTIONAL) {
            return true; // Optional fields can be included or excluded
        }
        
        if (fieldType == FieldType.CONDITIONAL && conditionalRules != null) {
            for (ConditionalRule rule : conditionalRules) {
                // Evaluate conditional logic here
                // This would be implemented based on specific business rules
                if (evaluateCondition(rule.getCondition(), context)) {
                    return rule.isRequired();
                }
            }
        }
        
        return false;
    }
    
    private boolean evaluateCondition(String condition, Map<String, Object> context) {
        // Simple condition evaluation - can be extended for complex rules
        // For now, return true to include conditional fields
        return true;
    }
    
    /**
     * Get all possible field combinations for this field and its children
     */
    public List<FieldCombination> getFieldCombinations() {
        // This will be implemented to generate all possible combinations
        // of this field and its child fields
        return List.of();
    }
}
