package com.finzly.galaxy.rtp.validator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Random;

@Service
@Slf4j
public class SampleValuesService {

    private JsonNode sampleValues;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    @PostConstruct
    public void loadSampleValues() {
        try {
            ClassPathResource resource = new ClassPathResource("sample_values.json");
            try (InputStream input = resource.getInputStream()) {
                sampleValues = objectMapper.readTree(input);
                log.info("Successfully loaded sample_values.json");
            }
        } catch (IOException e) {
            log.error("Failed to load sample_values.json: {}", e.getMessage(), e);
            sampleValues = objectMapper.createObjectNode();
        }
    }

    /**
     * Get value for a tag, applying dynamic generation rules if needed
     */
    public String getValue(String tagKey, String index) {
        if (sampleValues == null || sampleValues.isEmpty()) {
            return null;
        }

        // Try exact match first
        JsonNode tagNode = sampleValues.get(tagKey);

        // If not found, try case-insensitive match
        if (tagNode == null) {
            tagNode = findCaseInsensitive(sampleValues, tagKey);
        }

        if (tagNode == null) {
            return null;
        }

        // Check if it has a value (static value)
        if (tagNode.has("value")) {
            return tagNode.get("value").asText();
        }

        // Check if it has a template (dynamic value)
        if (tagNode.has("template")) {
            String template = tagNode.get("template").asText();
            return processTemplate(tagKey, template);
        }

        // Check dynamic generation rules
        JsonNode dynamicRules = sampleValues.get("dynamic_generation_rules");
        if (dynamicRules != null && dynamicRules.has(tagKey)) {
            JsonNode rule = dynamicRules.get(tagKey);
            return processDynamicRule(tagKey, rule);
        }

        return null;
    }

    /**
     * Find case-insensitive match in JSON node
     */
    private JsonNode findCaseInsensitive(JsonNode node, String key) {
        if (node == null || !node.isObject()) {
            return null;
        }

        String lowerKey = key.toLowerCase();

        // Try direct iteration
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            if (fieldName.toLowerCase().equals(lowerKey)) {
                return node.get(fieldName);
            }
        }

        return null;
    }

    /**
     * Process template with dynamic values
     */
    private String processTemplate(String tagKey, String template) {
        String result = template;

        // Replace {YYYYMMDD} with current date
        if (result.contains("{YYYYMMDD}")) {
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            result = result.replace("{YYYYMMDD}", currentDate);
        }

        // Replace {CURRENT_DATE} with current date in YYYY-MM-DD format
        if (result.contains("{CURRENT_DATE}")) {
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            result = result.replace("{CURRENT_DATE}", currentDate);
        }

        // Replace {CURRENT_DATETIME} with current date-time in YYYY-MM-DDThh:mm:ss
        // format
        if (result.contains("{CURRENT_DATETIME}")) {
            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            result = result.replace("{CURRENT_DATETIME}", currentDateTime);
        }

        // Replace {RANDOM_4} with 4 random digits
        if (result.contains("{RANDOM_4}")) {
            String random4 = String.format("%04d", random.nextInt(10000));
            result = result.replace("{RANDOM_4}", random4);
        }

        // Replace {RANDOM_11} with 11 random digits
        if (result.contains("{RANDOM_11}")) {
            String random11 = String.format("%011d", (long) (random.nextDouble() * 99999999999L));
            result = result.replace("{RANDOM_11}", random11);
        }

        return result;
    }

    /**
     * Process dynamic generation rule
     */
    private String processDynamicRule(String tagKey, JsonNode rule) {
        if (rule.has("format")) {
            String format = rule.get("format").asText();
            return processTemplate(tagKey, format);
        }

        if (tagKey.equals("CreDtTm") && rule.has("use_current_datetime")) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        }

        if (tagKey.equals("IntrBkSttlmDt") && rule.has("use_current_date")) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        return null;
    }

    /**
     * Get value for a tag with context (e.g., Dbtr_Nm, Cdtr_Nm)
     */
    public String getValueWithContext(String baseTag, String context, String index) {
        // Try context-specific key first (e.g., Dbtr_Nm, Cdtr_StrtNm)
        String contextKey = context + "_" + baseTag;
        String value = getValue(contextKey, index);
        if (value != null) {
            return value;
        }

        // Fall back to base tag
        return getValue(baseTag, index);
    }

    /**
     * Generate MsgId with dynamic date and random last 11 digits
     * Format from sample: M20250927101010101MTBOTS31591625732
     * Structure: M(1) + YYYYMMDD(8) + 101010101MTBOTS(15) + 11 random digits = 35
     * chars
     * Last 4 digits of the 11-digit suffix should be random
     */
    public String generateMsgId() {
        // Use format from sample: M20250927101010101MTBOTS31591625732
        // Structure: M + YYYYMMDD + 101010101MTBOTS + (11 digits where last 4 are
        // random)
        // Date is current date, last 4 digits of 11-digit suffix are random
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // Generate 11 digits: first 7 fixed (3159162) + last 4 random
        String random4 = String.format("%04d", random.nextInt(10000));
        return "M" + dateStr + "101010101MTBOTS3159162" + random4;
    }

    /**
     * Generate InstrId with dynamic date and random last 11 digits
     * Format from sample: 20250927101010101MTBAKSG01167086952
     * Structure: YYYYMMDD(8) + 101010101MTBAKSG(16) + 11 random digits = 35 chars
     * Last 4 digits of the 11-digit suffix should be random
     */
    public String generateInstrId() {
        // Use format from sample: 20250927101010101MTBAKSG01167086952
        // Structure: YYYYMMDD + 101010101MTBAKSG + (11 digits where last 4 are random)
        // Date is current date, last 4 digits of 11-digit suffix are random
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // Generate 11 digits: first 7 fixed (0116708) + last 4 random
        String random4 = String.format("%04d", random.nextInt(10000));
        return dateStr + "101010101MTBAKSG0116708" + random4;
    }

    /**
     * Generate TxId with dynamic date and random last 11 digits
     * Format from sample: 20251104101010101MTBAKSG01167076705
     * Structure: YYYYMMDD(8) + 101010101MTBAKSG(16) + 11 random digits = 35 chars
     * Last 4 digits of the 11-digit suffix should be random
     */
    public String generateTxId() {
        // Use format from sample: 20251104101010101MTBAKSG01167076705
        // Structure: YYYYMMDD + 101010101MTBAKSG + (11 digits where last 4 are random)
        // Date is current date, last 4 digits of 11-digit suffix are random
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // Generate 11 digits: first 7 fixed (0116707) + last 4 random
        String random4 = String.format("%04d", random.nextInt(10000));
        return dateStr + "101010101MTBAKSG0116707" + random4;
    }
}
