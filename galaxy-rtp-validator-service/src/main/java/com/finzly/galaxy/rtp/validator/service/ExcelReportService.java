package com.finzly.galaxy.rtp.validator.service;

import com.finzly.galaxy.rtp.validator.dto.XmlCombination;
import com.finzly.galaxy.rtp.validator.dto.XmlGenerationResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelReportService {

    public byte[] generateExcelReport(XmlGenerationResult result, List<String> selectedTagIndices) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create styles
            Map<String, CellStyle> styles = createStyles(workbook);

            // Sheet 1: Summary and Tag Information
            createSummarySheet(workbook, result, selectedTagIndices, styles);

            // Sheet 2-N: Individual combination sheets
            for (XmlCombination combination : result.getCombinations()) {
                createCombinationSheet(workbook, combination, styles);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createSummarySheet(Workbook workbook, XmlGenerationResult result, 
                                   List<String> selectedTagIndices, Map<String, CellStyle> styles) {
        Sheet sheet = workbook.createSheet("Summary");
        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("PACS.008 XML Generation Report");
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
        rowNum++;

        // Generation Summary Section
        Row summaryHeaderRow = sheet.createRow(rowNum++);
        Cell summaryHeaderCell = summaryHeaderRow.createCell(0);
        summaryHeaderCell.setCellValue("Generation Summary");
        summaryHeaderCell.setCellStyle(styles.get("sectionHeader"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));

        // Summary data
        createDataRow(sheet, rowNum++, "Total Combinations Generated", 
                     String.valueOf(result.getTotalCombinations()), styles);
        createDataRow(sheet, rowNum++, "Optional Tags Selected", 
                     String.valueOf(selectedTagIndices.size()), styles);
        createDataRow(sheet, rowNum++, "Generation Time", 
                     result.getGenerationTimeMs() + " ms", styles);
        createDataRow(sheet, rowNum++, "Status", 
                     result.isSuccess() ? "SUCCESS" : "FAILED", styles);
        rowNum++;

        // Selected Tags Section
        Row tagsHeaderRow = sheet.createRow(rowNum++);
        Cell tagsHeaderCell = tagsHeaderRow.createCell(0);
        tagsHeaderCell.setCellValue("Selected Tag Indices");
        tagsHeaderCell.setCellStyle(styles.get("sectionHeader"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));

        // Table header for selected tags
        Row tagTableHeaderRow = sheet.createRow(rowNum++);
        createHeaderCell(tagTableHeaderRow, 0, "Sr. No.", styles);
        createHeaderCell(tagTableHeaderRow, 1, "Tag Index", styles);
        
        // Selected tags data
        int srNo = 1;
        for (String tagIndex : selectedTagIndices) {
            Row row = sheet.createRow(rowNum++);
            Cell cell1 = row.createCell(0);
            cell1.setCellValue(srNo++);
            cell1.setCellStyle(styles.get("data"));
            
            Cell cell2 = row.createCell(1);
            cell2.setCellValue(tagIndex);
            cell2.setCellStyle(styles.get("data"));
        }
        rowNum++;

        // Combination Breakdown Section
        Row breakdownHeaderRow = sheet.createRow(rowNum++);
        Cell breakdownHeaderCell = breakdownHeaderRow.createCell(0);
        breakdownHeaderCell.setCellValue("Combination Breakdown");
        breakdownHeaderCell.setCellStyle(styles.get("sectionHeader"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 4));

        // Table header for combinations
        Row combTableHeaderRow = sheet.createRow(rowNum++);
        createHeaderCell(combTableHeaderRow, 0, "Combination #", styles);
        createHeaderCell(combTableHeaderRow, 1, "Description", styles);
        createHeaderCell(combTableHeaderRow, 2, "Optional Tags Count", styles);
        createHeaderCell(combTableHeaderRow, 3, "Included Optional Tags", styles);

        // Combinations data
        for (XmlCombination combination : result.getCombinations()) {
            Row row = sheet.createRow(rowNum++);
            
            Cell cell1 = row.createCell(0);
            cell1.setCellValue(combination.getCombinationNumber());
            cell1.setCellStyle(styles.get("data"));
            
            Cell cell2 = row.createCell(1);
            cell2.setCellValue(combination.getDescription());
            cell2.setCellStyle(styles.get("data"));
            
            Cell cell3 = row.createCell(2);
            cell3.setCellValue(combination.getIncludedOptionalTags() != null ? 
                              combination.getIncludedOptionalTags().size() : 0);
            cell3.setCellStyle(styles.get("data"));
            
            Cell cell4 = row.createCell(3);
            if (combination.getIncludedOptionalTags() != null && !combination.getIncludedOptionalTags().isEmpty()) {
                cell4.setCellValue(String.join(", ", combination.getIncludedOptionalTags()));
            } else {
                cell4.setCellValue("None (Base message with mandatory tags only)");
            }
            cell4.setCellStyle(styles.get("data"));
        }

        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
            // Add extra width for better readability
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
    }

    private void createCombinationSheet(Workbook workbook, XmlCombination combination, 
                                       Map<String, CellStyle> styles) {
        String sheetName = "Combination " + combination.getCombinationNumber();
        Sheet sheet = workbook.createSheet(sheetName);
        int rowNum = 0;

        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Combination #" + combination.getCombinationNumber());
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
        rowNum++;

        // Combination Information Section
        Row infoHeaderRow = sheet.createRow(rowNum++);
        Cell infoHeaderCell = infoHeaderRow.createCell(0);
        infoHeaderCell.setCellValue("Combination Information");
        infoHeaderCell.setCellStyle(styles.get("sectionHeader"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

        // Combination details
        createDataRow(sheet, rowNum++, "Combination Number", 
                     String.valueOf(combination.getCombinationNumber()), styles);
        createDataRow(sheet, rowNum++, "Description", 
                     combination.getDescription(), styles);
        
        // Optional tags included
        Row optionalTagsLabelRow = sheet.createRow(rowNum++);
        Cell labelCell = optionalTagsLabelRow.createCell(0);
        labelCell.setCellValue("Included Optional Tags");
        labelCell.setCellStyle(styles.get("label"));
        
        Cell valueCell = optionalTagsLabelRow.createCell(1);
        if (combination.getIncludedOptionalTags() != null && !combination.getIncludedOptionalTags().isEmpty()) {
            valueCell.setCellValue(String.join(", ", combination.getIncludedOptionalTags()));
        } else {
            valueCell.setCellValue("None (Base message with mandatory tags only)");
        }
        valueCell.setCellStyle(styles.get("data"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 1, 2));
        rowNum++;

        // Optional tags count
        createDataRow(sheet, rowNum++, "Optional Tags Count", 
                     String.valueOf(combination.getIncludedOptionalTags() != null ? 
                                   combination.getIncludedOptionalTags().size() : 0), styles);
        rowNum++;

        // XML Content Section
        Row xmlHeaderRow = sheet.createRow(rowNum++);
        Cell xmlHeaderCell = xmlHeaderRow.createCell(0);
        xmlHeaderCell.setCellValue("XML Message Content");
        xmlHeaderCell.setCellStyle(styles.get("sectionHeader"));
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

        // XML content - Create rich text with highlighting for optional tags
        String xmlContent = combination.getXmlContent();
        if (xmlContent != null) {
            Row xmlRow = sheet.createRow(rowNum++);
            Cell xmlCell = xmlRow.createCell(0);
            
            // Create rich text string with highlighted optional tags
            RichTextString richText = createHighlightedXmlContent(workbook, xmlContent);
            xmlCell.setCellValue(richText);
            xmlCell.setCellStyle(styles.get("xmlSingleCell"));
            
            // Merge across all columns for full width
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));
            
            // Set row height based on content (approximately 15 points per line)
            int lineCount = xmlContent.split("\n").length;
            int rowHeight = Math.min(lineCount * 300, 32767); // Max row height is 32767 (409 points)
            xmlRow.setHeight((short) rowHeight);
        }

        // Auto-size columns
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
        
        // Set wider width for XML content column
        sheet.setColumnWidth(0, 25000);
    }

    private RichTextString createHighlightedXmlContent(Workbook workbook, String xmlContent) {
        // Create fonts for normal and highlighted text
        Font normalFont = workbook.createFont();
        normalFont.setFontName("Courier New");
        normalFont.setFontHeightInPoints((short) 9);
        normalFont.setColor(IndexedColors.BLACK.getIndex());

        Font highlightFont = workbook.createFont();
        highlightFont.setFontName("Courier New");
        highlightFont.setFontHeightInPoints((short) 10);
        highlightFont.setBold(true);
        highlightFont.setColor(IndexedColors.BLUE.getIndex()); // Blue color for optional tags

        // First, clean the content by removing markers
        String cleanContent = xmlContent
            .replace("<!--OPTIONAL_START-->", "")
            .replace("<!--OPTIONAL_END-->", "")
            .replace("<!--OPTIONAL_TAG-->", "");
        
        // Create the rich text string with clean content
        XSSFRichTextString richText = new XSSFRichTextString(cleanContent);
        
        // Apply normal font to entire content first
        richText.applyFont(0, cleanContent.length(), normalFont);
        
        // Now find and highlight the optional tag sections
        int originalIndex = 0;
        
        while (originalIndex < xmlContent.length()) {
            int startMarkerPos = xmlContent.indexOf("<!--OPTIONAL_START-->", originalIndex);
            
            if (startMarkerPos == -1) {
                break; // No more optional sections
            }
            
            // Find the corresponding end marker
            int endMarkerPos = xmlContent.indexOf("<!--OPTIONAL_END-->", startMarkerPos);
            
            if (endMarkerPos == -1) {
                break; // Malformed markers
            }
            
            // Calculate the position in the cleaned content
            // Count characters before the start marker (excluding previous markers)
            String beforeMarker = xmlContent.substring(0, startMarkerPos);
            int cleanStartPos = beforeMarker
                .replace("<!--OPTIONAL_START-->", "")
                .replace("<!--OPTIONAL_END-->", "")
                .replace("<!--OPTIONAL_TAG-->", "")
                .length();
            
            // Get the content between markers
            int contentStart = startMarkerPos + "<!--OPTIONAL_START-->".length();
            String optionalContent = xmlContent.substring(contentStart, endMarkerPos);
            
            // Clean the optional content section
            String cleanOptionalContent = optionalContent
                .replace("<!--OPTIONAL_TAG-->", "");
            
            int cleanEndPos = cleanStartPos + cleanOptionalContent.length();
            
            // Apply highlighting to this section
            if (cleanStartPos < cleanContent.length() && cleanEndPos <= cleanContent.length()) {
                richText.applyFont(cleanStartPos, cleanEndPos, highlightFont);
            }
            
            // Move to after the end marker
            originalIndex = endMarkerPos + "<!--OPTIONAL_END-->".length();
        }
        
        return richText;
    }

    private void createDataRow(Sheet sheet, int rowNum, String label, String value, 
                              Map<String, CellStyle> styles) {
        Row row = sheet.createRow(rowNum);
        
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(styles.get("label"));
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(styles.get("data"));
    }

    private void createHeaderCell(Row row, int column, String value, Map<String, CellStyle> styles) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(styles.get("header"));
    }

    private Map<String, CellStyle> createStyles(Workbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldFont.setFontHeightInPoints((short) 12);

        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setColor(IndexedColors.WHITE.getIndex());

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        Font xmlFont = workbook.createFont();
        xmlFont.setFontName("Courier New");
        xmlFont.setFontHeightInPoints((short) 9);

        // Title style
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put("title", titleStyle);

        // Section header style
        CellStyle sectionHeaderStyle = workbook.createCellStyle();
        sectionHeaderStyle.setFont(boldFont);
        sectionHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        sectionHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sectionHeaderStyle.setAlignment(HorizontalAlignment.LEFT);
        sectionHeaderStyle.setBorderBottom(BorderStyle.THIN);
        styles.put("sectionHeader", sectionHeaderStyle);

        // Table header style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerStyle.setBorderTop(BorderStyle.MEDIUM);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        styles.put("header", headerStyle);

        // Label style
        CellStyle labelStyle = workbook.createCellStyle();
        labelStyle.setFont(boldFont);
        labelStyle.setAlignment(HorizontalAlignment.LEFT);
        labelStyle.setVerticalAlignment(VerticalAlignment.TOP);
        labelStyle.setBorderBottom(BorderStyle.THIN);
        labelStyle.setBorderTop(BorderStyle.THIN);
        labelStyle.setBorderLeft(BorderStyle.THIN);
        labelStyle.setBorderRight(BorderStyle.THIN);
        styles.put("label", labelStyle);

        // Data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.LEFT);
        dataStyle.setVerticalAlignment(VerticalAlignment.TOP);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setWrapText(true);
        styles.put("data", dataStyle);

        // XML style
        CellStyle xmlStyle = workbook.createCellStyle();
        xmlStyle.setFont(xmlFont);
        xmlStyle.setAlignment(HorizontalAlignment.LEFT);
        xmlStyle.setVerticalAlignment(VerticalAlignment.TOP);
        xmlStyle.setWrapText(false);
        styles.put("xml", xmlStyle);

        // XML Single Cell style (for complete XML in one cell)
        CellStyle xmlSingleCellStyle = workbook.createCellStyle();
        xmlSingleCellStyle.setFont(xmlFont);
        xmlSingleCellStyle.setAlignment(HorizontalAlignment.LEFT);
        xmlSingleCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        xmlSingleCellStyle.setWrapText(true); // Enable text wrapping
        
        // Add prominent borders
        xmlSingleCellStyle.setBorderBottom(BorderStyle.MEDIUM);
        xmlSingleCellStyle.setBorderTop(BorderStyle.MEDIUM);
        xmlSingleCellStyle.setBorderLeft(BorderStyle.MEDIUM);
        xmlSingleCellStyle.setBorderRight(BorderStyle.MEDIUM);
        
        // Set border colors to make them more visible
        xmlSingleCellStyle.setBottomBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
        xmlSingleCellStyle.setTopBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
        xmlSingleCellStyle.setLeftBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
        xmlSingleCellStyle.setRightBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
        
        // Add a light background color for better visibility
        xmlSingleCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        xmlSingleCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        styles.put("xmlSingleCell", xmlSingleCellStyle);

        return styles;
    }
}

