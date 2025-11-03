package com.finzly.galaxy.rtp.validator.controller;

import com.finzly.galaxy.rtp.validator.dto.TagSelectionRequest;
import com.finzly.galaxy.rtp.validator.dto.TagStatistics;
import com.finzly.galaxy.rtp.validator.dto.XmlGenerationResponse;
import com.finzly.galaxy.rtp.validator.dto.XmlGenerationResult;
import com.finzly.galaxy.rtp.validator.model.XmlTag;
import com.finzly.galaxy.rtp.validator.service.ExcelReportService;
import com.finzly.galaxy.rtp.validator.service.TagService;
import com.finzly.galaxy.rtp.validator.service.XmlGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private XmlGenerationService xmlGenerationService;

    @Autowired
    private ExcelReportService excelReportService;

    @GetMapping
    public ResponseEntity<List<XmlTag>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/search")
    public ResponseEntity<List<XmlTag>> searchTags(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(tagService.searchTags(query));
    }

    @GetMapping("/statistics")
    public ResponseEntity<TagStatistics> getStatistics() {
        return ResponseEntity.ok(tagService.getTagStatistics());
    }

    @PostMapping("/selection")
    public ResponseEntity<Void> updateTagSelection(@RequestBody TagSelectionRequest request) {
        tagService.updateTagSelection(request.getSelectedTagIndices());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate-xml-combinations")
    public ResponseEntity<XmlGenerationResult> generateXmlCombinations(@RequestBody TagSelectionRequest request) {
        XmlGenerationResult result = xmlGenerationService.generateXmlCombinations(request.getSelectedTagIndices());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate-xml")
    public ResponseEntity<XmlGenerationResponse> generateXml(@RequestBody TagSelectionRequest request) {
        // Check for conditional tags
        boolean hasConditional = request.getSelectedTagIndices().stream()
                .anyMatch(index -> {
                    XmlTag tag = tagService.getAllTags().stream()
                            .flatMap(root -> getAllTagsFlat(root).stream())
                            .filter(t -> t.getIndex().equals(index))
                            .findFirst()
                            .orElse(null);
                    return tag != null && tag.isConditional();
                });

        if (hasConditional) {
            return ResponseEntity.ok(XmlGenerationResponse.builder()
                    .success(false)
                    .message("Conditional tags are not allowed for XML generation Limited in this version")
                    .build());
        }

        // Update selection
        tagService.updateTagSelection(request.getSelectedTagIndices());

        return ResponseEntity.ok(XmlGenerationResponse.builder()
                .success(true)
                .message("XML generation successful (placeholder)")
                .xmlContent("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!-- XML generation coming soon -->")
                .build());
    }

    @PostMapping("/generate-excel-report")
    public ResponseEntity<byte[]> generateExcelReport(@RequestBody TagSelectionRequest request) {
        try {
            // First generate the XML combinations
            XmlGenerationResult result = xmlGenerationService.generateXmlCombinations(request.getSelectedTagIndices());
            
            if (!result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Filter to get only OPTIONAL tags from the selected indices
            List<String> optionalTagIndices = new java.util.ArrayList<>();
            for (String index : request.getSelectedTagIndices()) {
                XmlTag tag = tagService.getAllTags().stream()
                        .flatMap(root -> getAllTagsFlat(root).stream())
                        .filter(t -> t.getIndex().equals(index))
                        .findFirst()
                        .orElse(null);
                
                // Only include if it's an OPTIONAL tag
                if (tag != null && tag.getType() == com.finzly.galaxy.rtp.validator.model.TagType.OPTIONAL) {
                    optionalTagIndices.add(index);
                }
            }

            // Generate Excel report with only optional tags
            byte[] excelBytes = excelReportService.generateExcelReport(result, optionalTagIndices);

            // Create filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "PACS008_Generation_Report_" + timestamp + ".xlsx";

            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private List<XmlTag> getAllTagsFlat(XmlTag tag) {
        List<XmlTag> result = new java.util.ArrayList<>();
        result.add(tag);
        for (XmlTag child : tag.getChildren()) {
            result.addAll(getAllTagsFlat(child));
        }
        return result;
    }
}

