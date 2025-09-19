package com.finzly.galaxy.rtp.rest;

import com.finzly.galaxy.rtp.generator.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST Controller for Advanced RTP pacs.008 Message Generator
 * Provides endpoints for field selection and message generation
 */
@RestController
@RequestMapping("/api/advanced-generator")
@Slf4j
@CrossOrigin(origins = "*")
public class AdvancedGeneratorController {
    
    @Autowired
    private Pacs008FieldRegistry fieldRegistry;
    
    @Autowired
    private CombinatorialGenerator combinatorialGenerator;
    
    @Autowired
    private AdvancedMessageGenerator messageGenerator;
    
    /**
     * Get all available fields with their definitions
     */
    @GetMapping("/fields")
    public ResponseEntity<FieldRegistryResponse> getAllFields() {
        log.info("Retrieving all field definitions");
        
        try {
            Collection<FieldDefinition> allFieldsCollection = fieldRegistry.getAllFields();
            
            // Convert collection to map for response
            Map<String, FieldDefinition> allFields = allFieldsCollection.stream()
                    .collect(Collectors.toMap(FieldDefinition::getFieldName, field -> field));
            
            // Group fields by category
            Map<FieldDefinition.FieldCategory, List<FieldDefinition>> fieldsByCategory = 
                    allFieldsCollection.stream()
                            .collect(Collectors.groupingBy(FieldDefinition::getCategory));
            
            return ResponseEntity.ok(FieldRegistryResponse.builder()
                    .success(true)
                    .totalFields(fieldRegistry.getTotalFieldCount())
                    .mandatoryFields(fieldRegistry.getMandatoryFieldCount())
                    .optionalFields(fieldRegistry.getOptionalFieldCount())
                    .conditionalFields(fieldRegistry.getConditionalFieldCount())
                    .fieldsByCategory(fieldsByCategory)
                    .allFields(allFields)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error retrieving field definitions: {}", e.getMessage());
            return ResponseEntity.ok(FieldRegistryResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build());
        }
    }
    
    /**
     * Get fields by category
     */
    @GetMapping("/fields/category/{category}")
    public ResponseEntity<List<FieldDefinition>> getFieldsByCategory(@PathVariable String category) {
        log.info("Retrieving fields for category: {}", category);
        
        try {
            FieldDefinition.FieldCategory fieldCategory = FieldDefinition.FieldCategory.valueOf(category.toUpperCase());
            List<FieldDefinition> fields = fieldRegistry.getFieldsByCategory(fieldCategory);
            
            return ResponseEntity.ok(fields);
            
        } catch (Exception e) {
            log.error("Error retrieving fields for category {}: {}", category, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get field combinations preview
     */
    @PostMapping("/combinations/preview")
    public ResponseEntity<CombinationPreviewResponse> previewCombinations(@RequestBody CombinationPreviewRequest request) {
        log.info("Previewing combinations for {} selected fields", request.getSelectedFields().size());
        
        try {
            // Generate a sample of combinations
            List<FieldCombination> combinations = combinatorialGenerator.generateFieldCombinations(
                    request.getSelectedFields(), 
                    Math.min(request.getMaxCombinations(), 50) // Limit preview to 50
            );
            
            // Get statistics
            CombinatorialGenerator.CombinationStatistics statistics = 
                    combinatorialGenerator.getCombinationStatistics(combinations);
            
            return ResponseEntity.ok(CombinationPreviewResponse.builder()
                    .success(true)
                    .totalCombinations(combinations.size())
                    .previewCombinations(combinations.subList(0, Math.min(10, combinations.size())))
                    .statistics(statistics)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error previewing combinations: {}", e.getMessage());
            return ResponseEntity.ok(CombinationPreviewResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build());
        }
    }
    
    /**
     * Generate messages with selected field combinations
     */
    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> generateMessages(@RequestBody GenerationRequest request) {
        log.info("Generating {} messages with {} selected fields", 
                request.getNumberOfMessages(), request.getSelectedFields().size());
        
        try {
            // Convert request to internal format
            AdvancedMessageGenerator.GenerationRequest internalRequest = 
                    AdvancedMessageGenerator.GenerationRequest.builder()
                            .selectedFields(request.getSelectedFields())
                            .numberOfMessages(request.getNumberOfMessages())
                            .includeOptionalFields(request.isIncludeOptionalFields())
                            .includeConditionalFields(request.isIncludeConditionalFields())
                            .maxComplexity(request.getMaxComplexity())
                            .outputDirectory(request.getOutputDirectory())
                            .build();
            
            // Generate messages
            AdvancedMessageGenerator.GenerationResult result = 
                    messageGenerator.generateMessages(internalRequest);
            
            log.info("Generation result - Success: {}, Total Messages: {}, Generated Messages Count: {}", 
                    result.isSuccess(), result.getTotalMessages(), 
                    result.getGeneratedMessages() != null ? result.getGeneratedMessages().size() : 0);
            
            // Convert result to response format
            GenerationResponse response = GenerationResponse.builder()
                    .success(result.isSuccess())
                    .errorMessage(result.getErrorMessage())
                    .totalMessages(result.getTotalMessages())
                    .filePaths(result.getFilePaths())
                    .generatedMessages(convertGeneratedMessages(result.getGeneratedMessages()))
                    .statistics(convertStatistics(result.getStatistics()))
                    .build();
            
            log.info("Response - Generated Messages Count: {}", 
                    response.getGeneratedMessages() != null ? response.getGeneratedMessages().size() : 0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error generating messages: {}", e.getMessage());
            return ResponseEntity.ok(GenerationResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .totalMessages(0)
                    .build());
        }
    }
    
    /**
     * Get field usage statistics
     */
    @GetMapping("/statistics/fields")
    public ResponseEntity<FieldUsageStatistics> getFieldUsageStatistics() {
        log.info("Retrieving field usage statistics");
        
        try {
            Collection<FieldDefinition> allFields = fieldRegistry.getAllFields();
            
            Map<FieldDefinition.FieldType, Long> fieldsByType = allFields.stream()
                    .collect(Collectors.groupingBy(FieldDefinition::getFieldType, Collectors.counting()));
            
            Map<FieldDefinition.FieldCategory, Long> fieldsByCategory = allFields.stream()
                    .collect(Collectors.groupingBy(FieldDefinition::getCategory, Collectors.counting()));
            
            return ResponseEntity.ok(FieldUsageStatistics.builder()
                    .totalFields(allFields.size())
                    .fieldsByType(fieldsByType)
                    .fieldsByCategory(fieldsByCategory)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error retrieving field usage statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Validate field selection
     */
    @PostMapping("/validate-selection")
    public ResponseEntity<ValidationResponse> validateFieldSelection(@RequestBody FieldSelectionRequest request) {
        log.info("Validating field selection with {} fields", request.getSelectedFields().size());
        
        try {
            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            
            // Check if all mandatory fields are included
            Set<String> mandatoryFields = fieldRegistry.getMandatoryFields().stream()
                    .map(FieldDefinition::getFieldName)
                    .collect(Collectors.toSet());
            
            Set<String> missingMandatory = new HashSet<>(mandatoryFields);
            missingMandatory.removeAll(request.getSelectedFields());
            
            if (!missingMandatory.isEmpty()) {
                errors.add("Missing mandatory fields: " + String.join(", ", missingMandatory));
            }
            
            // Check for invalid field names
            Set<String> validFields = fieldRegistry.getAllFields().stream()
                    .map(FieldDefinition::getFieldName)
                    .collect(Collectors.toSet());
            Set<String> invalidFields = request.getSelectedFields().stream()
                    .filter(field -> !validFields.contains(field))
                    .collect(Collectors.toSet());
            
            if (!invalidFields.isEmpty()) {
                errors.add("Invalid field names: " + String.join(", ", invalidFields));
            }
            
            // Check for potential issues
            if (request.getSelectedFields().size() < 10) {
                warnings.add("Very few fields selected - consider including more fields for better test coverage");
            }
            
            if (request.getSelectedFields().size() > 50) {
                warnings.add("Many fields selected - this may result in a large number of combinations");
            }
            
            return ResponseEntity.ok(ValidationResponse.builder()
                    .valid(errors.isEmpty())
                    .errors(errors)
                    .warnings(warnings)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error validating field selection: {}", e.getMessage());
            return ResponseEntity.ok(ValidationResponse.builder()
                    .valid(false)
                    .errors(Arrays.asList("Validation error: " + e.getMessage()))
                    .build());
        }
    }
    
    /**
     * Convert internal generated messages to response format
     */
    private List<GeneratedMessageResponse> convertGeneratedMessages(List<AdvancedMessageGenerator.GeneratedMessage> internalMessages) {
        if (internalMessages == null) {
            return new ArrayList<>();
        }
        
        return internalMessages.stream()
                .map(this::convertGeneratedMessage)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert single generated message to response format
     */
    private GeneratedMessageResponse convertGeneratedMessage(AdvancedMessageGenerator.GeneratedMessage internalMessage) {
        return GeneratedMessageResponse.builder()
                .messageIndex(internalMessage.getMessageIndex())
                .combination(internalMessage.getCombination())
                .xmlContent(internalMessage.getXmlContent())
                .valid(internalMessage.isValid())
                .validationErrors(internalMessage.getValidationErrors())
                .generatedAt(internalMessage.getGeneratedAt().toString())
                .build();
    }
    
    /**
     * Convert internal statistics to response format
     */
    private GenerationStatistics convertStatistics(AdvancedMessageGenerator.GenerationStatistics internalStats) {
        return GenerationStatistics.builder()
                .totalMessages(internalStats.getTotalMessages())
                .validMessages(internalStats.getValidMessages())
                .invalidMessages(internalStats.getInvalidMessages())
                .fieldUsageStatistics(internalStats.getFieldUsageStatistics())
                .minComplexity(internalStats.getMinComplexity())
                .maxComplexity(internalStats.getMaxComplexity())
                .avgComplexity(internalStats.getAvgComplexity())
                .totalCombinations(internalStats.getTotalCombinations())
                .build();
    }
    
    // Request and Response classes
    
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class FieldRegistryResponse {
        private boolean success;
        private String errorMessage;
        private int totalFields;
        private int mandatoryFields;
        private int optionalFields;
        private int conditionalFields;
        private Map<FieldDefinition.FieldCategory, List<FieldDefinition>> fieldsByCategory;
        private Map<String, FieldDefinition> allFields;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CombinationPreviewRequest {
        private Set<String> selectedFields;
        private int maxCombinations;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CombinationPreviewResponse {
        private boolean success;
        private String errorMessage;
        private int totalCombinations;
        private List<FieldCombination> previewCombinations;
        private CombinatorialGenerator.CombinationStatistics statistics;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class GenerationRequest {
        private Set<String> selectedFields;
        private int numberOfMessages;
        private boolean includeOptionalFields;
        private boolean includeConditionalFields;
        private int maxComplexity;
        private String outputDirectory;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class GenerationResponse {
        private boolean success;
        private String errorMessage;
        private int totalMessages;
        private List<String> filePaths;
        private List<GeneratedMessageResponse> generatedMessages;
        private GenerationStatistics statistics;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class GeneratedMessageResponse {
        private int messageIndex;
        private FieldCombination combination;
        private String xmlContent;
        private boolean valid;
        private List<String> validationErrors;
        private String generatedAt;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class GenerationStatistics {
        private int totalMessages;
        private int validMessages;
        private int invalidMessages;
        private Map<String, Long> fieldUsageStatistics;
        private int minComplexity;
        private int maxComplexity;
        private double avgComplexity;
        private int totalCombinations;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class FieldUsageStatistics {
        private int totalFields;
        private Map<FieldDefinition.FieldType, Long> fieldsByType;
        private Map<FieldDefinition.FieldCategory, Long> fieldsByCategory;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class FieldSelectionRequest {
        private Set<String> selectedFields;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ValidationResponse {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
    }
}
