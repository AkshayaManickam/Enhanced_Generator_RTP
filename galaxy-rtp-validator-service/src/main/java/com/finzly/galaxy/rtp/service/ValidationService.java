package com.finzly.galaxy.rtp.service;

import com.finzly.galaxy.rtp.validator.XsdValidator;
import com.finzly.galaxy.rtp.validator.RTPRuleBookValidator;
import com.finzly.galaxy.rtp.model.ValidationResult;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import lombok.extern.slf4j.Slf4j;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ValidationService {

    public ValidationResult validateXmlMessage(String xmlContent, String messageType) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);
        result.setMessageType(messageType);
        result.setSchemaErrors(new ArrayList<>());
        result.setBusinessRuleErrors(new ArrayList<>());

        try {
            // 1. XSD Validation (always run)
            boolean xsdValid = validateXsd(xmlContent, messageType, result);
            if (!xsdValid) {
                result.setValid(false);
            }

            // 2. Business Rule Validation (always run, regardless of XSD validation result)
            boolean businessValid = validateBusinessRules(xmlContent, messageType, result);
            if (!businessValid) {
                result.setValid(false);
            }

            // Log validation summary
            if (result.isValid()) {
                log.info("Validation successful - no errors found");
            } else {
                log.info("Validation failed - Schema errors: {}, Business rule errors: {}", 
                    result.getSchemaErrors().size(), result.getBusinessRuleErrors().size());
            }

        } catch (Exception e) {
            result.setValid(false);
            result.getSchemaErrors().add("Validation error: " + e.getMessage());
        }

        return result;
    }

    private boolean validateXsd(String xmlContent, String messageType, ValidationResult result) {
        try {
            // Determine XSD file based on message type
            String xsdFileName = getXsdFileName(messageType);
            if (xsdFileName == null) {
                result.getSchemaErrors().add("Unsupported message type: " + messageType);
                return false;
            }

            // Load XSD schema
            ClassPathResource xsdResource = new ClassPathResource("xsd/" + xsdFileName);
            if (!xsdResource.exists()) {
                result.getSchemaErrors().add("XSD schema file not found: " + xsdFileName);
                return false;
            }

            // Create schema factory and validator
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Validator validator = factory.newSchema(xsdResource.getFile()).newValidator();

            // Create custom error handler to capture detailed validation errors
            CustomErrorHandler errorHandler = new CustomErrorHandler();
            validator.setErrorHandler(errorHandler);

            // Validate XML content
            validator.validate(new StreamSource(new StringReader(xmlContent)));

            // Add any validation errors to the result
            if (!errorHandler.getErrors().isEmpty()) {
                result.getSchemaErrors().addAll(errorHandler.getErrors());
                return false;
            }

            return true;

        } catch (Exception e) {
            result.getSchemaErrors().add("XSD validation failed: " + e.getMessage());
            return false;
        }
    }

    // Custom error handler to capture detailed validation errors
    private static class CustomErrorHandler implements ErrorHandler {
        private List<String> errors = new ArrayList<>();

        @Override
        public void warning(SAXParseException exception) {
            errors.add("Warning: " + exception.getMessage() + " at line " + exception.getLineNumber());
        }

        @Override
        public void error(SAXParseException exception) {
            errors.add("Error: " + exception.getMessage() + " at line " + exception.getLineNumber());
        }

        @Override
        public void fatalError(SAXParseException exception) {
            errors.add("Fatal Error: " + exception.getMessage() + " at line " + exception.getLineNumber());
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    private boolean validateBusinessRules(String xmlContent, String messageType, ValidationResult result) {
        boolean allValid = true;
        
        try {
            // Validate Message ID format
            if (!validateMessageId(xmlContent, result)) {
                allValid = false;
            }

            // Validate IBAN format
            if (!validateIBANs(xmlContent, result)) {
                allValid = false;
            }

            // Validate BIC format
            if (!validateBICs(xmlContent, result)) {
                allValid = false;
            }

            // Validate instruction IDs and other identifiers
            if (!validateIdentifiers(xmlContent, result)) {
                allValid = false;
            }

            // Validate amounts
            if (!validateAmounts(xmlContent, result)) {
                allValid = false;
            }

            // Validate dates
            if (!validateDates(xmlContent, result)) {
                allValid = false;
            }

            return allValid;
        } catch (Exception e) {
            result.getBusinessRuleErrors().add("Business rule validation error: " + e.getMessage());
            return false;
        }
    }

    private boolean validateMessageId(String xmlContent, ValidationResult result) {
        // Message ID validation regex pattern
        String msgIdPattern = "<MsgId>([^<]+)</MsgId>";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(msgIdPattern);
        java.util.regex.Matcher matcher = pattern.matcher(xmlContent);

        boolean allValid = true;
        while (matcher.find()) {
            String msgId = matcher.group(1);

            // Check for invalid character sequences
            if (msgId.contains("__") || msgId.contains("___") || msgId.contains("____")) {
                result.getBusinessRuleErrors().add("Invalid Message ID format: " + msgId + " - contains invalid underscore sequences");
                allValid = false;
            }

            // Check for other invalid patterns
            if (msgId.contains("  ") || msgId.contains("\t") || msgId.contains("\n")) {
                result.getBusinessRuleErrors().add("Invalid Message ID format: " + msgId + " - contains whitespace characters");
                allValid = false;
            }

            // Check for special characters that are not allowed
            if (msgId.matches(".*[^A-Za-z0-9_-].*")) {
                result.getBusinessRuleErrors().add("Invalid Message ID format: " + msgId + " - contains invalid special characters");
                allValid = false;
            }

            // Check length (should be reasonable)
            if (msgId.length() > 35) {
                result.getBusinessRuleErrors().add("Invalid Message ID length: " + msgId + " - exceeds maximum length of 35 characters");
                allValid = false;
            }
        }
        return allValid;
    }

    private boolean validateIBANs(String xmlContent, ValidationResult result) {
        // IBAN validation regex pattern
        String ibanPattern = "<IBAN>([^<]+)</IBAN>";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(ibanPattern);
        java.util.regex.Matcher matcher = pattern.matcher(xmlContent);

        boolean allValid = true;
        while (matcher.find()) {
            String iban = matcher.group(1);

            // Check for invalid characters
            if (iban.matches(".*[^A-Z0-9].*")) {
                result.getBusinessRuleErrors().add("Invalid IBAN format: " + iban + " - contains invalid characters");
                allValid = false;
            }

            // Check length (IBAN should be 15-34 characters)
            if (iban.length() < 15 || iban.length() > 34) {
                result.getBusinessRuleErrors().add("Invalid IBAN length: " + iban + " - should be 15-34 characters");
                allValid = false;
            }

            // Check for specific invalid patterns
            if (iban.contains("jkjjjj") || iban.contains("njnjj----")) {
                result.getBusinessRuleErrors()
                        .add("Invalid IBAN format: " + iban + " - contains invalid character sequences");
                allValid = false;
            }
        }
        return allValid;
    }

    private boolean validateBICs(String xmlContent, ValidationResult result) {
        // BIC validation regex pattern
        String bicPattern = "<BIC>([^<]+)</BIC>";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(bicPattern);
        java.util.regex.Matcher matcher = pattern.matcher(xmlContent);

        boolean allValid = true;
        while (matcher.find()) {
            String bic = matcher.group(1);

            // BIC should be 8 or 11 characters
            if (bic.length() != 8 && bic.length() != 11) {
                result.getBusinessRuleErrors().add("Invalid BIC length: " + bic + " - should be 8 or 11 characters");
                allValid = false;
            }

            // BIC should contain only letters and numbers
            if (!bic.matches("[A-Z0-9]+")) {
                result.getBusinessRuleErrors()
                        .add("Invalid BIC format: " + bic + " - should contain only letters and numbers");
                allValid = false;
            }
        }
        return allValid;
    }

    private boolean validateIdentifiers(String xmlContent, ValidationResult result) {
        // Check for invalid characters in instruction IDs
        String instrIdPattern = "<InstrId>([^<]+)</InstrId>";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(instrIdPattern);
        java.util.regex.Matcher matcher = pattern.matcher(xmlContent);

        boolean allValid = true;
        while (matcher.find()) {
            String instrId = matcher.group(1);

            // Check for underscores or other invalid characters
            if (instrId.contains("_______") || instrId.contains("_")) {
                result.getBusinessRuleErrors()
                        .add("Invalid Instruction ID: " + instrId + " - contains invalid characters");
                allValid = false;
            }
        }

        // Check for invalid characters in remittance IDs
        String rmtIdPattern = "<RmtId>([^<]+)</RmtId>";
        pattern = java.util.regex.Pattern.compile(rmtIdPattern);
        matcher = pattern.matcher(xmlContent);

        while (matcher.find()) {
            String rmtId = matcher.group(1);

            // Check for hash symbols or other invalid characters
            if (rmtId.contains("#############") || rmtId.contains("#")) {
                result.getBusinessRuleErrors()
                        .add("Invalid Remittance ID: " + rmtId + " - contains invalid characters");
                allValid = false;
            }
        }
        return allValid;
    }

    private boolean validateAmounts(String xmlContent, ValidationResult result) {
        // Validate amount format
        String amountPattern = "<IntrBkSttlmAmt Ccy=\"([^\"]+)\">([^<]+)</IntrBkSttlmAmt>";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(amountPattern);
        java.util.regex.Matcher matcher = pattern.matcher(xmlContent);

        boolean allValid = true;
        while (matcher.find()) {
            String currency = matcher.group(1);
            String amount = matcher.group(2);

            // Check if amount is a valid decimal
            try {
                Double.parseDouble(amount);
            } catch (NumberFormatException e) {
                result.getBusinessRuleErrors()
                        .add("Invalid amount format: " + amount + " - should be a valid decimal number");
                allValid = false;
            }

            // Check currency code
            if (currency.length() != 3) {
                result.getBusinessRuleErrors().add("Invalid currency code: " + currency + " - should be 3 characters");
                allValid = false;
            }
        }
        return allValid;
    }

    private boolean validateDates(String xmlContent, ValidationResult result) {
        // Validate date format
        String datePattern = "<CreDtTm>([^<]+)</CreDtTm>";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(datePattern);
        java.util.regex.Matcher matcher = pattern.matcher(xmlContent);

        boolean allValid = true;
        while (matcher.find()) {
            String dateStr = matcher.group(1);

            try {
                // Try to parse the date
                java.time.LocalDateTime.parse(dateStr);
            } catch (Exception e) {
                result.getBusinessRuleErrors()
                        .add("Invalid date format: " + dateStr + " - should be in ISO format (YYYY-MM-DDTHH:MM:SS)");
                allValid = false;
            }
        }

        // Validate settlement date
        String settlementDatePattern = "<IntrBkSttlmDt>([^<]+)</IntrBkSttlmDt>";
        pattern = java.util.regex.Pattern.compile(settlementDatePattern);
        matcher = pattern.matcher(xmlContent);

        while (matcher.find()) {
            String dateStr = matcher.group(1);

            try {
                // Try to parse the date
                java.time.LocalDate.parse(dateStr);
            } catch (Exception e) {
                result.getBusinessRuleErrors()
                        .add("Invalid settlement date format: " + dateStr + " - should be in ISO format (YYYY-MM-DD)");
                allValid = false;
            }
        }
        return allValid;
    }

    private String getXsdFileName(String messageType) {
        switch (messageType.toLowerCase()) {
            case "pacs.008":
                return "pacs.008.001.08.xsd";
            default:
                return null;
        }
    }
}
