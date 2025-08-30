package com.finzly.galaxy.rtp.validator;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Enhanced XSD Validator that validates both syntax and semantic correctness
 * according to ISO 20022 specifications
 */
@Slf4j
public class XSDEnhancedValidator {

    private final Schema schema;
    private final DocumentBuilder documentBuilder;

    public XSDEnhancedValidator(String xsdPath) throws SAXException, ParserConfigurationException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaSource = new StreamSource(xsdPath);
        this.schema = factory.newSchema(schemaSource);
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        this.documentBuilder = dbf.newDocumentBuilder();
    }

    public XSDEnhancedValidator(ByteArrayInputStream xsdInputStream) throws SAXException, ParserConfigurationException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaSource = new StreamSource(xsdInputStream);
        this.schema = factory.newSchema(schemaSource);
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        this.documentBuilder = dbf.newDocumentBuilder();
    }

    /**
     * Comprehensive validation including XSD syntax and semantic rules
     */
    public ValidationResult validateXmlComprehensive(String xmlContent) {
        ValidationResult result = new ValidationResult();

        try {
            // 1. XSD Schema Validation (Syntax)
            ValidationResult xsdResult = validateXsdSyntax(xmlContent);
            if (!xsdResult.isValid()) {
                result.setValid(false);
                result.getSchemaErrors().addAll(xsdResult.getSchemaErrors());
                result.getValidationErrors().addAll(xsdResult.getValidationErrors());
            }

            // 2. Semantic Validation (Business Rules)
            ValidationResult semanticResult = validateSemanticRules(xmlContent);
            if (!semanticResult.isValid()) {
                result.setValid(false);
                result.getBusinessRuleErrors().addAll(semanticResult.getBusinessRuleErrors());
                result.getValidationErrors().addAll(semanticResult.getValidationErrors());
            }

            if (result.isValid()) {
                result.setValid(true);
                log.info("Comprehensive validation successful");
            }

        } catch (Exception e) {
            result.setValid(false);
            ValidationError error = ValidationError.fromMessage("Validation error: " + e.getMessage());
            error.setCode(ISO20022ErrorCodes.SCHEMA_001);
            result.addError(error);
            log.error("Validation error", e);
        }

        return result;
    }

    /**
     * XSD Schema validation (syntax only)
     */
    private ValidationResult validateXsdSyntax(String xmlContent) {
        ValidationResult result = new ValidationResult();

        try {
            Validator validator = schema.newValidator();
            Source xmlSource = new StreamSource(new StringReader(xmlContent));

            ValidationErrorHandler errorHandler = new ValidationErrorHandler();
            validator.setErrorHandler(errorHandler);

            validator.validate(xmlSource);

            if (errorHandler.hasErrors()) {
                result.setValid(false);
                result.setErrors(errorHandler.getErrors());
                result.setValidationErrors(errorHandler.getErrorsWithCodes());
                log.error("XSD validation failed with {} errors", errorHandler.getErrors().size());
            } else {
                result.setValid(true);
            }

        } catch (Exception e) {
            result.setValid(false);
            ValidationError error = ValidationError.fromMessage("XSD validation failed: " + e.getMessage());
            error.setCode(ISO20022ErrorCodes.SCHEMA_001);
            result.addError(error);
        }

        return result;
    }

    /**
     * Semantic validation based on ISO 20022 business rules
     */
    private ValidationResult validateSemanticRules(String xmlContent) {
        ValidationResult result = new ValidationResult();

        try {
            Document doc = documentBuilder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
            doc.getDocumentElement().normalize();

            // Validate Message ID format
            validateMessageIdSemantic(doc, result);

            // Validate IBAN format
            validateIBANSemantic(doc, result);

            // Validate BIC format
            validateBICSemantic(doc, result);

            // Validate currency codes
            validateCurrencyCodes(doc, result);

            // Validate amounts
            validateAmounts(doc, result);

            // Validate dates
            validateDates(doc, result);

            // Validate charge bearer codes
            validateChargeBearerCodes(doc, result);

            // Validate purpose codes
            validatePurposeCodes(doc, result);

            // Validate instruction codes
            validateInstructionCodes(doc, result);

            // Validate settlement methods
            validateSettlementMethods(doc, result);

        } catch (Exception e) {
            result.setValid(false);
            ValidationError error = ValidationError.fromMessage("Semantic validation error: " + e.getMessage());
            error.setCode(ISO20022ErrorCodes.BUSINESS_001);
            result.addError(error);
        }

        return result;
    }

    private void validateMessageIdSemantic(Document doc, ValidationResult result) {
        NodeList msgIdList = doc.getElementsByTagName("MsgId");
        for (int i = 0; i < msgIdList.getLength(); i++) {
            String msgId = msgIdList.item(i).getTextContent();
            
            // Max35Text validation: 1-35 characters
            if (msgId.length() < 1 || msgId.length() > 35) {
                ValidationError error = ValidationError.fromMessage(
                    "Invalid Message ID length: " + msgId + " - must be 1-35 characters (Max35Text)"
                );
                error.setCode(ISO20022ErrorCodes.FORMAT_001);
                result.addError(error);
                result.setValid(false);
            }

            // Check for invalid patterns
            if (msgId.contains("__") || msgId.contains("  ") || msgId.contains("\t") || msgId.contains("\n")) {
                ValidationError error = ValidationError.fromMessage(
                    "Invalid Message ID format: " + msgId + " - contains invalid character sequences"
                );
                error.setCode(ISO20022ErrorCodes.BUSINESS_011);
                result.addError(error);
                result.setValid(false);
            }
        }
    }

    private void validateIBANSemantic(Document doc, ValidationResult result) {
        NodeList ibanList = doc.getElementsByTagName("IBAN");
        Pattern ibanPattern = Pattern.compile("[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}");
        
        for (int i = 0; i < ibanList.getLength(); i++) {
            String iban = ibanList.item(i).getTextContent();
            
            if (!ibanPattern.matcher(iban).matches()) {
                ValidationError error = ValidationError.fromMessage(
                    "Invalid IBAN format: " + iban + " - must match pattern [A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}"
                );
                error.setCode(ISO20022ErrorCodes.BUSINESS_001);
                result.addError(error);
                result.setValid(false);
            }
        }
    }

    private void validateBICSemantic(Document doc, ValidationResult result) {
        NodeList bicList = doc.getElementsByTagName("BIC");
        Pattern bicPattern = Pattern.compile("[A-Z0-9]{4,4}[A-Z]{2,2}[A-Z0-9]{2,2}([A-Z0-9]{3,3}){0,1}");
        
        for (int i = 0; i < bicList.getLength(); i++) {
            String bic = bicList.item(i).getTextContent();
            
            if (!bicPattern.matcher(bic).matches()) {
                ValidationError error = ValidationError.fromMessage(
                    "Invalid BIC format: " + bic + " - must match pattern [A-Z0-9]{4,4}[A-Z]{2,2}[A-Z0-9]{2,2}([A-Z0-9]{3,3}){0,1}"
                );
                error.setCode(ISO20022ErrorCodes.BUSINESS_002);
                result.addError(error);
                result.setValid(false);
            }
        }
    }

    private void validateCurrencyCodes(Document doc, ValidationResult result) {
        NodeList currencyList = doc.getElementsByTagName("Ccy");
        for (int i = 0; i < currencyList.getLength(); i++) {
            String currency = currencyList.item(i).getTextContent();
            
            // ActiveCurrencyCode validation: 3 uppercase letters
            if (!currency.matches("[A-Z]{3,3}")) {
                ValidationError error = ValidationError.fromMessage(
                    "Invalid currency code: " + currency + " - must be 3 uppercase letters"
                );
                error.setCode(ISO20022ErrorCodes.BUSINESS_003);
                result.addError(error);
                result.setValid(false);
            }
        }
    }

    private void validateAmounts(Document doc, ValidationResult result) {
        NodeList amountList = doc.getElementsByTagName("IntrBkSttlmAmt");
        for (int i = 0; i < amountList.getLength(); i++) {
            Element amountElement = (Element) amountList.item(i);
            String amountStr = amountElement.getTextContent();
            
            try {
                double amount = Double.parseDouble(amountStr);
                // ActiveCurrencyAndAmount validation: min 0.01, max 18 digits, 2 fraction digits
                if (amount < 0.01) {
                    ValidationError error = ValidationError.fromMessage(
                        "Invalid amount: " + amountStr + " - must be >= 0.01"
                    );
                    error.setCode(ISO20022ErrorCodes.BUSINESS_004);
                    result.addError(error);
                    result.setValid(false);
                }
                
                if (amountStr.replace(".", "").length() > 18) {
                    ValidationError error = ValidationError.fromMessage(
                        "Invalid amount: " + amountStr + " - too many digits (max 18 total)"
                    );
                    error.setCode(ISO20022ErrorCodes.BUSINESS_004);
                    result.addError(error);
                    result.setValid(false);
                }
                
                if (amountStr.contains(".") && amountStr.split("\\.")[1].length() > 2) {
                    ValidationError error = ValidationError.fromMessage(
                        "Invalid amount: " + amountStr + " - too many decimal places (max 2)"
                    );
                    error.setCode(ISO20022ErrorCodes.BUSINESS_004);
                    result.addError(error);
                    result.setValid(false);
                }
                
            } catch (NumberFormatException e) {
                ValidationError error = ValidationError.fromMessage(
                    "Invalid amount format: " + amountStr + " - must be a valid decimal number"
                );
                error.setCode(ISO20022ErrorCodes.BUSINESS_004);
                result.addError(error);
                result.setValid(false);
            }
        }
    }

    private void validateDates(Document doc, ValidationResult result) {
        // Validate CreDtTm (ISODateTime)
        NodeList creDtTmList = doc.getElementsByTagName("CreDtTm");
        for (int i = 0; i < creDtTmList.getLength(); i++) {
            String dateStr = creDtTmList.item(i).getTextContent();
            if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*")) {
                ValidationError error = ValidationError.fromMessage(
                    "Invalid creation date format: " + dateStr + " - must be ISO dateTime format"
                );
                error.setCode(ISO20022ErrorCodes.BUSINESS_005);
                result.addError(error);
                result.setValid(false);
            }
        }

        // Validate IntrBkSttlmDt (ISODate)
        NodeList settlementDateList = doc.getElementsByTagName("IntrBkSttlmDt");
        for (int i = 0; i < settlementDateList.getLength(); i++) {
            String dateStr = settlementDateList.item(i).getTextContent();
            if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                ValidationError error = ValidationError.fromMessage(
                    "Invalid settlement date format: " + dateStr + " - must be ISO date format (YYYY-MM-DD)"
                );
                error.setCode(ISO20022ErrorCodes.BUSINESS_005);
                result.addError(error);
                result.setValid(false);
            }
        }
    }

    private void validateChargeBearerCodes(Document doc, ValidationResult result) {
        NodeList chrgBrList = doc.getElementsByTagName("ChrgBr");
        for (int i = 0; i < chrgBrList.getLength(); i++) {
            String chrgBr = chrgBrList.item(i).getTextContent();
            
            // ChargeBearerType1Code validation
            if (!chrgBr.equals("SLEV")) {
                ValidationError error = ValidationError.fromMessage(
                    "Invalid charge bearer code: " + chrgBr + " - must be 'SLEV'"
                );
                error.setCode(ISO20022ErrorCodes.BUSINESS_009);
                result.addError(error);
                result.setValid(false);
            }
        }
    }

    private void validatePurposeCodes(Document doc, ValidationResult result) {
        NodeList purposeList = doc.getElementsByTagName("Cd");
        for (int i = 0; i < purposeList.getLength(); i++) {
            Element purposeElement = (Element) purposeList.item(i);
            if (purposeElement.getParentNode().getNodeName().equals("Purp")) {
                String purpose = purposeElement.getTextContent();
                
                // ExternalPurpose1Code validation (common values)
                String[] validPurposes = {"SALA", "CASH", "BUSINESS", "CONSUMER"};
                boolean valid = false;
                for (String validPurpose : validPurposes) {
                    if (purpose.equals(validPurpose)) {
                        valid = true;
                        break;
                    }
                }
                
                if (!valid) {
                    ValidationError error = ValidationError.fromMessage(
                        "Invalid purpose code: " + purpose + " - must be a valid ISO 20022 purpose code"
                    );
                    error.setCode(ISO20022ErrorCodes.BUSINESS_010);
                    result.addError(error);
                    result.setValid(false);
                }
            }
        }
    }

    private void validateInstructionCodes(Document doc, ValidationResult result) {
        NodeList instructionList = doc.getElementsByTagName("Cd");
        for (int i = 0; i < instructionList.getLength(); i++) {
            Element instructionElement = (Element) instructionList.item(i);
            if (instructionElement.getParentNode().getNodeName().equals("InstrForCdtrAgt")) {
                String instruction = instructionElement.getTextContent();
                
                // Instruction3Code_TCH validation
                String[] validInstructions = {"PRTK", "TKCM", "TKSG", "TKSP", "TKVE", "TKXP", "TOKN", "VLTK", "INST1"};
                boolean valid = false;
                for (String validInstruction : validInstructions) {
                    if (instruction.equals(validInstruction)) {
                        valid = true;
                        break;
                    }
                }
                
                if (!valid) {
                    ValidationError error = ValidationError.fromMessage(
                        "Invalid instruction code: " + instruction + " - must be a valid TCH instruction code"
                    );
                    error.setCode(ISO20022ErrorCodes.BUSINESS_010);
                    result.addError(error);
                    result.setValid(false);
                }
            }
        }
    }

    private void validateSettlementMethods(Document doc, ValidationResult result) {
        NodeList settlementList = doc.getElementsByTagName("SttlmMtd");
        for (int i = 0; i < settlementList.getLength(); i++) {
            String settlement = settlementList.item(i).getTextContent();
            
            // Common settlement methods
            String[] validSettlements = {"CLRG", "COVE", "TAGV"};
            boolean valid = false;
            for (String validSettlement : validSettlements) {
                if (settlement.equals(validSettlement)) {
                    valid = true;
                    break;
                }
            }
            
            if (!valid) {
                ValidationError error = ValidationError.fromMessage(
                    "Invalid settlement method: " + settlement + " - must be a valid ISO 20022 settlement method"
                );
                error.setCode(ISO20022ErrorCodes.BUSINESS_008);
                result.addError(error);
                result.setValid(false);
            }
        }
    }
}
