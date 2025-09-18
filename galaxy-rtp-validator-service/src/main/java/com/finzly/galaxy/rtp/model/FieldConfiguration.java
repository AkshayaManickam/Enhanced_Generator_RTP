package com.finzly.galaxy.rtp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldConfiguration {
    private List<FieldDefinition> mandatoryFields;
    private List<FieldDefinition> optionalFields;
    private List<FieldDefinition> conditionalFields;
    private List<FieldGroup> fieldGroups;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldDefinition {
        private String path;
        private String description;
        private String dataType;
        private String sampleValue;
        private String isoDescription;
        private String condition; // For conditional fields
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldGroup {
        private String name;
        private String description;
        private List<String> fields;
    }
}

