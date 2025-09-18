package com.finzly.galaxy.rtp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnhancedGeneratorResponse {
    private boolean success;
    private String message;
    private List<GeneratedMessage> messages;
    private GenerationMetadata metadata;
    private String reportDownloadUrl; // URL to download the Excel report
    private List<String> errors;
    private List<String> warnings;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeneratedMessage {
        private String messageId;
        private String xmlContent;
        private List<String> includedFields;
        private List<String> excludedFields;
        private Map<String, String> fieldDescriptions;
        private boolean isValid;
        private List<String> validationErrors;
        private String testScenario;
        private Map<String, Object> fieldValues;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerationMetadata {
        private String messageType;
        private int totalMessagesGenerated;
        private int numberOfFiles;
        private LocalDateTime generatedAt;
        private String testScenario;
        private List<String> selectedOptionalFields;
        private List<String> selectedConditionalFields;
        private Map<String, Object> generationOptions;
        private String reportFormat;
        private boolean reportGenerated;
    }
}

