package com.finzly.galaxy.rtp.util;

import com.finzly.galaxy.rtp.model.Document;
import com.finzly.galaxy.rtp.validator.XsdValidator;
import com.finzly.galaxy.rtp.validator.ValidationResult;
import lombok.extern.slf4j.Slf4j;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Utility class for XML processing operations
 */
@Slf4j
public class XmlProcessor {
    
    private final JAXBContext jaxbContext;
    private final XsdValidator validator;
    
    public XmlProcessor() throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(Document.class);
        this.validator = null; // Will be initialized when XSD path is provided
    }
    
    public XmlProcessor(String xsdPath) throws Exception {
        this.jaxbContext = JAXBContext.newInstance(Document.class);
        this.validator = new XsdValidator(xsdPath);
    }
    
    /**
     * Marshals Java object to XML string
     * 
     * @param document Document object to marshal
     * @return XML string representation
     * @throws JAXBException if marshaling fails
     */
    public String marshalToXml(Document document) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter writer = new StringWriter();
        marshaller.marshal(document, writer);
        String xmlWithPrefixes = writer.toString();
        String xmlWithoutPrefixes = xmlWithPrefixes.replaceAll("ns\\d+:", "")
                .replaceAll("xmlns:ns\\d+=\"[^\"]+\"", "xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\"");
        return xmlWithoutPrefixes;
    }
    public String marshalToXmlFile(Document document, File outputFile) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(document, outputFile);
        String xmlWithPrefixes = outputFile.toString();
        String xmlWithoutPrefixes = xmlWithPrefixes.replaceAll("ns\\d+:", "")
                .replaceAll("xmlns:ns\\d+=\"[^\"]+\"", "xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\"");
        return xmlWithoutPrefixes;
    }
    /**
     * Marshals Java object to XML file
     * 
     * @param document Document object to marshal
     * @param outputFile Output file path
     * @throws JAXBException if marshaling fails
     */
    public void marshalToFile(Document document, File outputFile) throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

        marshaller.marshal(document, outputFile);
        log.info("Document marshaled to file: {}", outputFile.getAbsolutePath());
    }
    
    /**
     * Unmarshals XML string to Java object
     * 
     * @param xmlContent XML content as string
     * @return Document object
     * @throws JAXBException if unmarshaling fails
     */
    public Document unmarshalFromXml(String xmlContent) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(xmlContent);
        return (Document) unmarshaller.unmarshal(reader);
    }
    
    /**
     * Unmarshals XML file to Java object
     * 
     * @param inputFile Input XML file
     * @return Document object
     * @throws JAXBException if unmarshaling fails
     */
    public Document unmarshalFromFile(File inputFile) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (Document) unmarshaller.unmarshal(inputFile);
    }
    
    /**
     * Validates XML content against XSD schema
     * 
     * @param xmlContent XML content to validate
     * @return ValidationResult
     */
    public ValidationResult validateXml(String xmlContent) {
        if (validator == null) {
            ValidationResult result = new ValidationResult(false);
            result.addError("Validator not initialized - XSD path not provided");
            return result;
        }
        return validator.validateXml(xmlContent);
    }
    
    /**
     * Validates XML file against XSD schema
     * 
     * @param xmlFile XML file to validate
     * @return ValidationResult
     */
    public ValidationResult validateXmlFile(InputStream xmlFile) {
        if (validator == null) {
            ValidationResult result = new ValidationResult(false);
            result.addError("Validator not initialized - XSD path not provided");
            return result;
        }
        return validator.validateXmlFile(xmlFile);
    }
    
    /**
     * Processes XML content: unmarshals, validates, and returns result
     * 
     * @param xmlContent XML content to process
     * @return ProcessingResult containing document and validation status
     */
    public ProcessingResult processXml(String xmlContent) {
        ProcessingResult result = new ProcessingResult();
        
        try {
            // First validate the XML
            ValidationResult validationResult = validateXml(xmlContent);
            result.setValidationResult(validationResult);
            
            if (validationResult.isValid()) {
                // If valid, unmarshal to Java object
                Document document = unmarshalFromXml(xmlContent);
                result.setDocument(document);
                result.setSuccess(true);
                log.info("XML processing completed successfully");
            } else {
                result.setSuccess(false);
                log.error("XML validation failed, skipping unmarshaling");
            }
            
        } catch (JAXBException e) {
            result.setSuccess(false);
            result.addError("JAXB Exception: " + e.getMessage());
            log.error("JAXB Exception during XML processing", e);
        }
        
        return result;
    }
}
