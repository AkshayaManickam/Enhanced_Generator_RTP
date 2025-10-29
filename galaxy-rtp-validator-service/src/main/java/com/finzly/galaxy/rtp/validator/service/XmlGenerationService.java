package com.finzly.galaxy.rtp.validator.service;

import com.finzly.galaxy.rtp.validator.dto.XmlCombination;
import com.finzly.galaxy.rtp.validator.dto.XmlGenerationResult;
import com.finzly.galaxy.rtp.validator.model.TagType;
import com.finzly.galaxy.rtp.validator.model.XmlTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class XmlGenerationService {

    @Autowired
    private TagDataService tagDataService;

    public XmlGenerationResult generateXmlCombinations(List<String> selectedIndices) {
        long startTime = System.currentTimeMillis();

        try {
            // Get all tags
            List<XmlTag> allTags = tagDataService.getAllTags();
            
            // Identify selected optional tags
            List<XmlTag> selectedOptionalTags = new ArrayList<>();
            for (String index : selectedIndices) {
                XmlTag tag = findTagByIndex(allTags, index);
                if (tag != null && tag.isOptional()) {
                    selectedOptionalTags.add(tag);
                }
            }

            // Generate combinations (2^n)
            int n = selectedOptionalTags.size();
            int totalCombinations = (int) Math.pow(2, n);
            List<XmlCombination> combinations = new ArrayList<>();

            for (int i = 0; i < totalCombinations; i++) {
                List<XmlTag> includedOptionalTags = new ArrayList<>();
                List<String> includedTagNames = new ArrayList<>();

                // Determine which optional tags to include in this combination
                for (int j = 0; j < n; j++) {
                    if ((i & (1 << j)) != 0) {
                        XmlTag optionalTag = selectedOptionalTags.get(j);
                        includedOptionalTags.add(optionalTag);
                        includedTagNames.add(optionalTag.getXmlTag());
                    }
                }

                // Generate XML for this combination
                String xmlContent = generateXmlForCombination(allTags, includedOptionalTags, selectedIndices);
                
                String description = includedTagNames.isEmpty() 
                    ? "Base message (mandatory tags only)" 
                    : "With: " + String.join(", ", includedTagNames);

                combinations.add(XmlCombination.builder()
                        .combinationNumber(i + 1)
                        .description(description)
                        .includedOptionalTags(includedTagNames)
                        .xmlContent(xmlContent)
                        .build());
            }

            long endTime = System.currentTimeMillis();

            return XmlGenerationResult.builder()
                    .success(true)
                    .message("Successfully generated " + totalCombinations + " XML message combinations")
                    .totalCombinations(totalCombinations)
                    .combinations(combinations)
                    .generationTimeMs(endTime - startTime)
                    .build();

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            return XmlGenerationResult.builder()
                    .success(false)
                    .message("Error generating XML: " + e.getMessage())
                    .totalCombinations(0)
                    .combinations(new ArrayList<>())
                    .generationTimeMs(endTime - startTime)
                    .build();
        }
    }

    private String generateXmlForCombination(List<XmlTag> allTags, List<XmlTag> includedOptionalTags, List<String> selectedIndices) {
        StringBuilder xml = new StringBuilder();
        
        // XML Header
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        
        // Generate content recursively
        for (XmlTag rootTag : allTags) {
            generateTagXml(xml, rootTag, includedOptionalTags, selectedIndices, 1);
        }
        
        xml.append("</Document>");
        
        return xml.toString();
    }

    private void generateTagXml(StringBuilder xml, XmlTag tag, List<XmlTag> includedOptionalTags, 
                                 List<String> selectedIndices, int indentLevel) {
        boolean shouldInclude = false;

        // Include if mandatory
        if (tag.isMandatory()) {
            shouldInclude = true;
        }
        // Include if optional and selected, and it's in the current combination
        else if (tag.isOptional()) {
            if (selectedIndices.contains(tag.getIndex()) && includedOptionalTags.contains(tag)) {
                shouldInclude = true;
            }
        }
        // Skip conditional tags
        else if (tag.isConditional()) {
            shouldInclude = false;
        }

        if (shouldInclude) {
            String indent = "  ".repeat(indentLevel);
            
            // Check if tag has children
            if (tag.getChildren() != null && !tag.getChildren().isEmpty()) {
                xml.append(indent).append("<").append(tag.getXmlTag()).append(">\n");
                
                // Process children
                for (XmlTag child : tag.getChildren()) {
                    generateTagXml(xml, child, includedOptionalTags, selectedIndices, indentLevel + 1);
                }
                
                xml.append(indent).append("</").append(tag.getXmlTag()).append(">\n");
            } else {
                // Leaf node - generate sample value
                String sampleValue = generateSampleValue(tag);
                xml.append(indent).append("<").append(tag.getXmlTag()).append(">")
                   .append(sampleValue)
                   .append("</").append(tag.getXmlTag()).append(">\n");
            }
        }
    }

    private String generateSampleValue(XmlTag tag) {
        String tagName = tag.getXmlTag().toUpperCase();
        
        // Generate contextual sample data
        if (tagName.contains("ID") || tagName.equals("MSGID")) {
            return "MSG" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } else if (tagName.contains("DT") || tagName.contains("TM") || tagName.equals("CREDTTM")) {
            return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        } else if (tagName.contains("AMT") || tagName.equals("TTLINTRBKSTTLMAMT") || tagName.equals("INTRBKSTTLMAMT")) {
            return "1000.00";
        } else if (tagName.contains("CCY")) {
            return "USD";
        } else if (tagName.contains("NM") || tagName.equals("NM")) {
            return "Sample Name";
        } else if (tagName.contains("BICFI") || tagName.contains("BIC")) {
            return "BANKUS33XXX";
        } else if (tagName.contains("IBAN")) {
            return "GB33BUKB20201555555555";
        } else if (tagName.contains("NBOFTXS")) {
            return "1";
        } else if (tagName.contains("CTRY")) {
            return "US";
        } else if (tagName.contains("PSTLCD") || tagName.contains("PSTCD")) {
            return "10001";
        } else if (tagName.contains("CD") && tag.getLength() != null && tag.getLength() <= 4) {
            return "SALA";
        } else {
            // Default sample value
            return "Sample" + tag.getXmlTag();
        }
    }

    private XmlTag findTagByIndex(List<XmlTag> tags, String index) {
        for (XmlTag tag : tags) {
            if (tag.getIndex().equals(index)) {
                return tag;
            }
            XmlTag found = findTagByIndex(tag.getChildren(), index);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}

