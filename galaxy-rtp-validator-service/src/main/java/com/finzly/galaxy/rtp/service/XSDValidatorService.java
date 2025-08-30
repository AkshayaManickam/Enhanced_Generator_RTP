package com.finzly.galaxy.rtp.service;

import com.finzly.galaxy.rtp.model.ValidationResult;
import org.springframework.stereotype.Service;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class XSDValidatorService {

    public ValidationResult validateXmlAgainstXsd(String xmlContent) throws Exception {
          ValidationResult result = new ValidationResult();
        result.setValid(true);
        result.setSchemaErrors(new ArrayList<>());
        result.setWarnings(new ArrayList<>());
        result.setBusinessRuleErrors(new ArrayList<>());
        result.setValidationErrors(new ArrayList<>());
        try (InputStream xsdStream = getClass().getClassLoader()
                .getResourceAsStream("xsd/pacs008.xsd")) {

            if (xsdStream == null) {
                throw new FileNotFoundException("XSD file not found in resources/xsd/pacs008.xsd");
            }

            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsdStream));
            Validator validator = schema.newValidator();

            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) {
                    result.setValid(true);
                    result.getWarnings().add("Warning: " + exception.getMessage()); }

                @Override
                public void error(SAXParseException exception) {
                    result.setValid(false);
                    result.getSchemaErrors().add("Error: " + exception.getMessage()); }

                @Override
                public void fatalError(SAXParseException exception) {
                    result.setValid(false);
                    result.getSchemaErrors().add("Fatal: " + exception.getMessage()); }
            });

            // Validate the XML from a String
            try (Reader reader = new StringReader(xmlContent)) {
                validator.validate(new StreamSource(reader));
            }
        }

        return result;
    }
}
