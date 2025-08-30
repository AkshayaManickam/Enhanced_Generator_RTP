package com.finzly.galaxy.rtp.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Enhanced XSD Schema Validator for PACS.008 messages with ISO 20022 error codes
 */
@Slf4j
public class XsdValidator {

    private final Schema schema;

    public XsdValidator(String xsdPath) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaSource = new StreamSource(new File(xsdPath));
        this.schema = factory.newSchema(schemaSource);
    }

    public XsdValidator(InputStream xsdInputStream) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaSource = new StreamSource(xsdInputStream);
        this.schema = factory.newSchema(schemaSource);
    }

    /**
     * Validates XML string against the XSD schema with ISO 20022 error codes
     *
     * @param xmlContent XML content as string
     * @return ValidationResult containing validation status and any errors with codes
     */
    public ValidationResult validateXml(String xmlContent) {
        ValidationResult result = new ValidationResult();

        try {
            Validator validator = schema.newValidator();
            Source xmlSource = new StreamSource(new StringReader(xmlContent));

            // Set up enhanced error handler to collect validation errors with codes
            ValidationErrorHandler errorHandler = new ValidationErrorHandler();
            validator.setErrorHandler(errorHandler);

            // Perform validation
            validator.validate(xmlSource);

            if (errorHandler.hasErrors()) {
                result.setValid(false);
                result.setErrors(errorHandler.getErrors());
                result.setValidationErrors(errorHandler.getErrorsWithCodes());
                log.error("XML validation failed with {} errors", errorHandler.getErrors().size());
                
                // Log detailed error information
                for (ValidationError error : errorHandler.getErrorsWithCodes()) {
                    log.error("Validation Error [{}]: {} at {}", 
                        error.getCode(), error.getMessage(), error.getLocation());
                }
            } else {
                result.setValid(true);
                log.info("XML validation successful");
            }

        } catch (SAXException e) {
            result.setValid(false);
            ValidationError error = ValidationError.fromMessage("SAX Exception: " + e.getMessage());
            error.setCode(ISO20022ErrorCodes.SCHEMA_001);
            result.addError(error);
            log.error("SAX Exception during validation", e);
        } catch (IOException e) {
            result.setValid(false);
            ValidationError error = ValidationError.fromMessage("IO Exception: " + e.getMessage());
            error.setCode(ISO20022ErrorCodes.SCHEMA_001);
            result.addError(error);
            log.error("IO Exception during validation", e);
        }

        return result;
    }

    /**
     * Validates XML file against the XSD schema with ISO 20022 error codes
     *
     * @param xmlFile XML file to validate
     * @return ValidationResult containing validation status and any errors with codes
     */
    public ValidationResult validateXmlFile(InputStream xmlFile) {
        ValidationResult result = new ValidationResult();

        try {
            Validator validator = schema.newValidator();
            Source xmlSource = new StreamSource(xmlFile);

            // Set up enhanced error handler to collect validation errors with codes
            ValidationErrorHandler errorHandler = new ValidationErrorHandler();
            validator.setErrorHandler(errorHandler);

            // Perform validation
            validator.validate(xmlSource);

            if (errorHandler.hasErrors()) {
                result.setValid(false);
                result.setErrors(errorHandler.getErrors());
                result.setValidationErrors(errorHandler.getErrorsWithCodes());
                log.error("XML file validation failed with {} errors", errorHandler.getErrors().size());
                
                // Log detailed error information
                for (ValidationError error : errorHandler.getErrorsWithCodes()) {
                    log.error("Validation Error [{}]: {} at {}", 
                        error.getCode(), error.getMessage(), error.getLocation());
                }
            } else {
                result.setValid(true);
                log.info("XML file validation successful");
            }

        } catch (SAXException e) {
            result.setValid(false);
            ValidationError error = ValidationError.fromMessage("SAX Exception: " + e.getMessage());
            error.setCode(ISO20022ErrorCodes.SCHEMA_001);
            result.addError(error);
            log.error("SAX Exception during file validation", e);
        } catch (IOException e) {
            result.setValid(false);
            ValidationError error = ValidationError.fromMessage("IO Exception: " + e.getMessage());
            error.setCode(ISO20022ErrorCodes.SCHEMA_001);
            result.addError(error);
            log.error("IO Exception during file validation", e);
        }

        return result;
    }
}
