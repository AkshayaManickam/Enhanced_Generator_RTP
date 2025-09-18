package com.finzly.galaxy.rtp.service;

import com.finzly.galaxy.rtp.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExcelReportService {
    
    @Autowired
    private FieldConfigurationService fieldConfigurationService;
    
    public byte[] generateExcelReport(List<EnhancedGeneratorResponse.GeneratedMessage> messages,
                                    EnhancedGeneratorRequest request,
                                    EnhancedGeneratorResponse.GenerationMetadata metadata) {
        
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle descriptionStyle = createDescriptionStyle(workbook);
            
            // Summary sheet
            createSummarySheet(workbook, messages, request, metadata, headerStyle, dataStyle);
            
            // Messages sheet
            createMessagesSheet(workbook, messages, headerStyle, dataStyle);
            
            // Field definitions sheet
            createFieldDefinitionsSheet(workbook, headerStyle, dataStyle, descriptionStyle);
            
            // Test scenarios sheet
            createTestScenariosSheet(workbook, messages, headerStyle, dataStyle);
            
            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("Error generating Excel report", e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }
    
    private void createSummarySheet(Workbook workbook, 
                                   List<EnhancedGeneratorResponse.GeneratedMessage> messages,
                                   EnhancedGeneratorRequest request,
                                   EnhancedGeneratorResponse.GenerationMetadata metadata,
                                   CellStyle headerStyle, 
                                   CellStyle dataStyle) {
        
        Sheet sheet = workbook.createSheet("Summary");
        
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("RTP Message Generation Report");
        titleCell.setCellStyle(headerStyle);
        
        // Generation details
        rowNum = addSummarySection(sheet, rowNum, "Generation Details", headerStyle, dataStyle,
                "Generated At", metadata.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "Message Type", metadata.getMessageType(),
                "Total Messages", String.valueOf(metadata.getTotalMessagesGenerated()),
                "Test Scenario", metadata.getTestScenario(),
                "Report Format", metadata.getReportFormat());
        
        // Generation options
        rowNum = addSummarySection(sheet, rowNum, "Generation Options", headerStyle, dataStyle,
                "Amount Range", (String) metadata.getGenerationOptions().get("amountRange"),
                "Currency", (String) metadata.getGenerationOptions().get("currency"),
                "Business Type", (String) metadata.getGenerationOptions().get("businessType"),
                "Service Level", (String) metadata.getGenerationOptions().get("serviceLevel"));
        
        // Field selection summary
        rowNum = addSummarySection(sheet, rowNum, "Field Selection Summary", headerStyle, dataStyle,
                "Selected Optional Fields", String.valueOf(metadata.getSelectedOptionalFields() != null ? metadata.getSelectedOptionalFields().size() : 0),
                "Selected Conditional Fields", String.valueOf(metadata.getSelectedConditionalFields() != null ? metadata.getSelectedConditionalFields().size() : 0));
        
        // Validation summary
        long validMessages = messages.stream().mapToLong(m -> m.isValid() ? 1 : 0).sum();
        long invalidMessages = messages.size() - validMessages;
        
        rowNum = addSummarySection(sheet, rowNum, "Validation Summary", headerStyle, dataStyle,
                "Valid Messages", String.valueOf(validMessages),
                "Invalid Messages", String.valueOf(invalidMessages),
                "Validation Rate", String.format("%.2f%%", (double) validMessages / messages.size() * 100));
        
        // Auto-size columns
        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createMessagesSheet(Workbook workbook, 
                                   List<EnhancedGeneratorResponse.GeneratedMessage> messages,
                                   CellStyle headerStyle, 
                                   CellStyle dataStyle) {
        
        Sheet sheet = workbook.createSheet("Generated Messages");
        
        // Headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "Message ID", "Test Scenario", "Valid", "Included Fields Count", 
            "Excluded Fields Count", "Validation Errors", "Included Fields", "Excluded Fields"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows
        for (int i = 0; i < messages.size(); i++) {
            EnhancedGeneratorResponse.GeneratedMessage message = messages.get(i);
            Row row = sheet.createRow(i + 1);
            
            row.createCell(0).setCellValue(message.getMessageId());
            row.createCell(1).setCellValue(message.getTestScenario());
            row.createCell(2).setCellValue(message.isValid() ? "Yes" : "No");
            row.createCell(3).setCellValue(message.getIncludedFields().size());
            row.createCell(4).setCellValue(message.getExcludedFields().size());
            row.createCell(5).setCellValue(String.join("; ", message.getValidationErrors()));
            row.createCell(6).setCellValue(String.join("; ", message.getIncludedFields()));
            row.createCell(7).setCellValue(String.join("; ", message.getExcludedFields()));
            
            // Apply data style
            for (int j = 0; j < headers.length; j++) {
                row.getCell(j).setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createFieldDefinitionsSheet(Workbook workbook,
                                           CellStyle headerStyle,
                                           CellStyle dataStyle,
                                           CellStyle descriptionStyle) {
        
        Sheet sheet = workbook.createSheet("Field Definitions");
        
        // Headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "Field Path", "Field Type", "Description", "ISO Description", 
            "Data Type", "Sample Value", "Condition"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        FieldConfiguration config = fieldConfigurationService.getFieldConfiguration();
        int rowNum = 1;
        
        // Mandatory fields
        for (FieldConfiguration.FieldDefinition field : config.getMandatoryFields()) {
            Row row = sheet.createRow(rowNum++);
            addFieldDefinitionRow(row, field, "Mandatory", dataStyle, descriptionStyle);
        }
        
        // Optional fields
        for (FieldConfiguration.FieldDefinition field : config.getOptionalFields()) {
            Row row = sheet.createRow(rowNum++);
            addFieldDefinitionRow(row, field, "Optional", dataStyle, descriptionStyle);
        }
        
        // Conditional fields
        for (FieldConfiguration.FieldDefinition field : config.getConditionalFields()) {
            Row row = sheet.createRow(rowNum++);
            addFieldDefinitionRow(row, field, "Conditional", dataStyle, descriptionStyle);
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void createTestScenariosSheet(Workbook workbook,
                                        List<EnhancedGeneratorResponse.GeneratedMessage> messages,
                                        CellStyle headerStyle,
                                        CellStyle dataStyle) {
        
        Sheet sheet = workbook.createSheet("Test Scenarios");
        
        // Headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "Scenario", "Message Count", "Field Combinations", "Validation Results", "Description"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Group messages by scenario
        Map<String, List<EnhancedGeneratorResponse.GeneratedMessage>> scenarioGroups = 
                messages.stream().collect(Collectors.groupingBy(EnhancedGeneratorResponse.GeneratedMessage::getTestScenario));
        
        int rowNum = 1;
        for (Map.Entry<String, List<EnhancedGeneratorResponse.GeneratedMessage>> entry : scenarioGroups.entrySet()) {
            String scenario = entry.getKey();
            List<EnhancedGeneratorResponse.GeneratedMessage> scenarioMessages = entry.getValue();
            
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(scenario);
            row.createCell(1).setCellValue(scenarioMessages.size());
            
            // Get unique field combinations
            Set<String> uniqueFieldCombinations = scenarioMessages.stream()
                    .map(m -> String.join(", ", m.getIncludedFields()))
                    .collect(Collectors.toSet());
            row.createCell(2).setCellValue(String.join(" | ", uniqueFieldCombinations));
            
            // Validation results
            long validCount = scenarioMessages.stream().mapToLong(m -> m.isValid() ? 1 : 0).sum();
            row.createCell(3).setCellValue(String.format("%d/%d valid", validCount, scenarioMessages.size()));
            
            // Description
            row.createCell(4).setCellValue(getScenarioDescription(scenario));
            
            // Apply data style
            for (int j = 0; j < headers.length; j++) {
                row.getCell(j).setCellStyle(dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    
    private void addFieldDefinitionRow(Row row, FieldConfiguration.FieldDefinition field, 
                                     String fieldType, CellStyle dataStyle, CellStyle descriptionStyle) {
        row.createCell(0).setCellValue(field.getPath());
        row.createCell(1).setCellValue(fieldType);
        row.createCell(2).setCellValue(field.getDescription());
        row.createCell(3).setCellValue(field.getIsoDescription() != null ? field.getIsoDescription() : "");
        row.createCell(4).setCellValue(field.getDataType());
        row.createCell(5).setCellValue(field.getSampleValue());
        row.createCell(6).setCellValue(field.getCondition() != null ? field.getCondition() : "");
        
        // Apply styles
        for (int i = 0; i < 7; i++) {
            Cell cell = row.getCell(i);
            if (i == 2 || i == 3) { // Description columns
                cell.setCellStyle(descriptionStyle);
            } else {
                cell.setCellStyle(dataStyle);
            }
        }
    }
    
    private int addSummarySection(Sheet sheet, int startRow, String sectionTitle, 
                                CellStyle headerStyle, CellStyle dataStyle, String... keyValuePairs) {
        
        Row titleRow = sheet.createRow(startRow++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(sectionTitle);
        titleCell.setCellStyle(headerStyle);
        
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            Row row = sheet.createRow(startRow++);
            row.createCell(0).setCellValue(keyValuePairs[i] + ":");
            row.createCell(1).setCellValue(keyValuePairs[i + 1]);
            row.getCell(0).setCellStyle(dataStyle);
            row.getCell(1).setCellStyle(dataStyle);
        }
        
        return startRow + 1; // Add extra space
    }
    
    private String getScenarioDescription(String scenario) {
        switch (scenario) {
            case "basic":
                return "Basic test scenario with mandatory fields only";
            case "comprehensive":
                return "Comprehensive test scenario with all available fields";
            case "edge_cases":
                return "Edge case testing with boundary values and special conditions";
            default:
                return "Custom test scenario";
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }
    
    private CellStyle createDescriptionStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setWrapText(true);
        return style;
    }
}
