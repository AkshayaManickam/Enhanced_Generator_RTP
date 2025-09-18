package com.finzly.galaxy.rtp.rest;

import com.finzly.galaxy.rtp.model.*;
import com.finzly.galaxy.rtp.service.EnhancedXmlGeneratorService;
import com.finzly.galaxy.rtp.service.ExcelReportService;
import com.finzly.galaxy.rtp.service.FieldConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rtp")
@Slf4j
public class EnhancedRTPMessageGeneratorController {

    @Autowired
    private EnhancedXmlGeneratorService enhancedXmlGeneratorService;
    
    @Autowired
    private ExcelReportService excelReportService;
    
    @Autowired
    private FieldConfigurationService fieldConfigurationService;

    @PostMapping(value = "/generate-enhanced")
    public ResponseEntity<?> generateEnhancedMessages(@RequestBody EnhancedGeneratorRequest request) {
        log.info("Received enhanced generation request for {} messages", request.getNumberOfFiles());
        
        try {
            EnhancedGeneratorResponse response = enhancedXmlGeneratorService.generateMessages(request);
            
            if (response.isSuccess()) {
                log.info("Successfully generated {} messages", response.getMessages().size());
                return ResponseEntity.ok(response);
            } else {
                log.error("Message generation failed: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in enhanced message generation", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Enhanced message generation failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping(value = "/field-configuration")
    public ResponseEntity<FieldConfiguration> getFieldConfiguration() {
        try {
            FieldConfiguration config = fieldConfigurationService.getFieldConfiguration();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("Error retrieving field configuration", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(value = "/field-groups")
    public ResponseEntity<List<FieldConfiguration.FieldGroup>> getFieldGroups() {
        try {
            List<FieldConfiguration.FieldGroup> fieldGroups = fieldConfigurationService.getFieldGroups();
            return ResponseEntity.ok(fieldGroups);
        } catch (Exception e) {
            log.error("Error retrieving field groups", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(value = "/mandatory-fields")
    public ResponseEntity<List<FieldConfiguration.FieldDefinition>> getMandatoryFields() {
        try {
            List<FieldConfiguration.FieldDefinition> mandatoryFields = fieldConfigurationService.getMandatoryFields();
            return ResponseEntity.ok(mandatoryFields);
        } catch (Exception e) {
            log.error("Error retrieving mandatory fields", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(value = "/optional-fields")
    public ResponseEntity<List<FieldConfiguration.FieldDefinition>> getOptionalFields() {
        try {
            List<FieldConfiguration.FieldDefinition> optionalFields = fieldConfigurationService.getOptionalFields();
            return ResponseEntity.ok(optionalFields);
        } catch (Exception e) {
            log.error("Error retrieving optional fields", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(value = "/conditional-fields")
    public ResponseEntity<List<FieldConfiguration.FieldDefinition>> getConditionalFields() {
        try {
            List<FieldConfiguration.FieldDefinition> conditionalFields = fieldConfigurationService.getConditionalFields();
            return ResponseEntity.ok(conditionalFields);
        } catch (Exception e) {
            log.error("Error retrieving conditional fields", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/generate-report")
    public ResponseEntity<?> generateReport(@RequestBody EnhancedGeneratorRequest request) {
        log.info("Generating report for request: {}", request);
        
        try {
            // Generate messages first
            EnhancedGeneratorResponse response = enhancedXmlGeneratorService.generateMessages(request);
            
            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(response);
            }
            
            // Generate Excel report
            byte[] reportBytes = excelReportService.generateExcelReport(
                    response.getMessages(), 
                    request, 
                    response.getMetadata()
            );
            
            // Create filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("RTP_Test_Report_%s.xlsx", timestamp);
            
            // Return file as download
            ByteArrayResource resource = new ByteArrayResource(reportBytes);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(reportBytes.length)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error generating report", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Report generation failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping(value = "/supported-message-types")
    public ResponseEntity<List<String>> getSupportedMessageTypes() {
        List<String> supportedTypes = List.of("pacs.008");
        return ResponseEntity.ok(supportedTypes);
    }

    @GetMapping(value = "/generation-options")
    public ResponseEntity<Map<String, Object>> getGenerationOptions() {
        Map<String, Object> options = new HashMap<>();
        
        // Amount ranges
        options.put("amountRanges", List.of("small", "medium", "large"));
        
        // Currencies
        options.put("currencies", List.of("USD", "EUR", "GBP", "CAD"));
        
        // Business types
        options.put("businessTypes", List.of("BUSINESS", "CONSUMER"));
        
        // Service levels
        options.put("serviceLevels", List.of("SDVA"));
        
        // Test scenarios
        options.put("testScenarios", List.of("basic", "comprehensive", "edge_cases"));
        
        // Report formats
        options.put("reportFormats", List.of("excel", "json"));
        
        return ResponseEntity.ok(options);
    }
}

