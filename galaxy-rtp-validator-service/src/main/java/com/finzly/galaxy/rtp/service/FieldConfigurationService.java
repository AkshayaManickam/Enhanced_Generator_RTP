package com.finzly.galaxy.rtp.service;

import com.finzly.galaxy.rtp.model.FieldConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import org.springframework.beans.factory.InitializingBean;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FieldConfigurationService implements InitializingBean {

    @Value("classpath:config/rtp_field_configuration.yaml")
    private Resource configResource;

    private FieldConfiguration fieldConfiguration;
    private Map<String, FieldConfiguration.FieldDefinition> fieldDefinitionMap;

    @Override
    public void afterPropertiesSet() {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = configResource.getInputStream();
            Map<String, Object> data = yaml.load(inputStream);

            fieldConfiguration = buildFieldConfiguration(data);
            fieldDefinitionMap = buildFieldDefinitionMap(fieldConfiguration);

            log.info("Field configuration loaded successfully with {} mandatory, {} optional, {} conditional fields",
                    fieldConfiguration.getMandatoryFields().size(),
                    fieldConfiguration.getOptionalFields().size(),
                    fieldConfiguration.getConditionalFields().size());

        } catch (IOException e) {
            log.error("Failed to load field configuration", e);
            throw new RuntimeException("Failed to load field configuration", e);
        }
    }

    public FieldConfiguration getFieldConfiguration() {
        return fieldConfiguration;
    }

    public Map<String, FieldConfiguration.FieldDefinition> getFieldDefinitionMap() {
        return fieldDefinitionMap;
    }

    public List<FieldConfiguration.FieldGroup> getFieldGroups() {
        return fieldConfiguration.getFieldGroups();
    }

    public List<FieldConfiguration.FieldDefinition> getMandatoryFields() {
        return fieldConfiguration.getMandatoryFields();
    }

    public List<FieldConfiguration.FieldDefinition> getOptionalFields() {
        return fieldConfiguration.getOptionalFields();
    }

    public List<FieldConfiguration.FieldDefinition> getConditionalFields() {
        return fieldConfiguration.getConditionalFields();
    }

    private FieldConfiguration buildFieldConfiguration(Map<String, Object> data) {
        List<FieldConfiguration.FieldDefinition> mandatoryFields = 
                buildFieldDefinitions((List<Map<String, Object>>) data.get("mandatory"));
        
        List<FieldConfiguration.FieldDefinition> optionalFields = 
                buildFieldDefinitions((List<Map<String, Object>>) data.get("optional"));
        
        List<FieldConfiguration.FieldDefinition> conditionalFields = 
                buildFieldDefinitions((List<Map<String, Object>>) data.get("conditional"));
        
        List<FieldConfiguration.FieldGroup> fieldGroups = 
                buildFieldGroups((List<Map<String, Object>>) data.get("fieldGroups"));

        return FieldConfiguration.builder()
                .mandatoryFields(mandatoryFields)
                .optionalFields(optionalFields)
                .conditionalFields(conditionalFields)
                .fieldGroups(fieldGroups)
                .build();
    }

    private List<FieldConfiguration.FieldDefinition> buildFieldDefinitions(List<Map<String, Object>> fieldsData) {
        if (fieldsData == null) {
            return new ArrayList<>();
        }
        
        return fieldsData.stream()
                .map(this::buildFieldDefinition)
                .collect(Collectors.toList());
    }

    private FieldConfiguration.FieldDefinition buildFieldDefinition(Map<String, Object> fieldData) {
        return FieldConfiguration.FieldDefinition.builder()
                .path((String) fieldData.get("path"))
                .description((String) fieldData.get("description"))
                .dataType((String) fieldData.get("dataType"))
                .sampleValue((String) fieldData.get("sampleValue"))
                .isoDescription((String) fieldData.get("isoDescription"))
                .condition((String) fieldData.get("condition"))
                .build();
    }

    private List<FieldConfiguration.FieldGroup> buildFieldGroups(List<Map<String, Object>> groupsData) {
        if (groupsData == null) {
            return new ArrayList<>();
        }
        
        return groupsData.stream()
                .map(this::buildFieldGroup)
                .collect(Collectors.toList());
    }

    private FieldConfiguration.FieldGroup buildFieldGroup(Map<String, Object> groupData) {
        List<String> fields = (List<String>) groupData.get("fields");
        if (fields == null) {
            fields = new ArrayList<>();
        }
        
        return FieldConfiguration.FieldGroup.builder()
                .name((String) groupData.get("name"))
                .description((String) groupData.get("description"))
                .fields(fields)
                .build();
    }

    private Map<String, FieldConfiguration.FieldDefinition> buildFieldDefinitionMap(FieldConfiguration config) {
        Map<String, FieldConfiguration.FieldDefinition> map = new HashMap<>();
        
        // Add mandatory fields
        config.getMandatoryFields().forEach(field -> map.put(field.getPath(), field));
        
        // Add optional fields
        config.getOptionalFields().forEach(field -> map.put(field.getPath(), field));
        
        // Add conditional fields
        config.getConditionalFields().forEach(field -> map.put(field.getPath(), field));
        
        return map;
    }
}
