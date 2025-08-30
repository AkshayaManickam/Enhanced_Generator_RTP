package com.finzly.galaxy.rtp.rest;

import com.finzly.galaxy.rtp.model.XmlGeneratorRequest;
import com.finzly.galaxy.rtp.model.ValidationRequest;
import com.finzly.galaxy.rtp.model.ValidationResult;
import com.finzly.galaxy.rtp.service.XSDValidatorService;
import com.finzly.galaxy.rtp.service.XmlGeneratorService;
import com.finzly.galaxy.rtp.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@Slf4j
public class RTPMessageGeneratorController {

    @Autowired
    private XmlGeneratorService xmlGeneratorService;

    @Autowired
    private ValidationService validationService;


    @Autowired
    private XSDValidatorService validateXmlAgainstXsd;


    @PostMapping(value = "/rtp/messages")
    public ResponseEntity<?> generateMessages(@RequestBody XmlGeneratorRequest xmlGeneratorRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<String> xmlContents = xmlGeneratorService.generateMultipleFiles(xmlGeneratorRequest.getNumberOfFiles());

            // Create response with metadata
            response.put("success", true);
            response.put("message", "Messages generated successfully");
            response.put("messages", xmlContents);
            response.put("metadata", Map.of(
                    "messageType", xmlGeneratorRequest.getMessageType(),
                    "numberOfFiles", xmlGeneratorRequest.getNumberOfFiles(),
                    "generatedAt", LocalDateTime.now().toString()));

            log.info("Successfully generated {} PACS.008 messages", xmlContents.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating messages: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Message generation failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping(value = "/rtp/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> validateMessage(@RequestBody ValidationRequest request) {
        try {
            log.info("Validating message of type: {}", request.getMessageType());

            ValidationResult validationResult = validationService.validateXmlMessage(
                    request.getXmlContent(),
                    request.getMessageType());

            Map<String, Object> response = new HashMap<>();
            response.put("success", validationResult.isValid());
            response.put("validationResult", validationResult);

            if (!validationResult.isValid()) {
                response.put("message", "Validation failed");
            } else {
                response.put("message", "Validation successful");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error validating message: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping(value = "/validate", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> validateXml(@RequestBody String xmlContent) {
        if (xmlContent == null || xmlContent.isBlank()) {
            return ResponseEntity.badRequest().body("No XML content provided");
        }

        try {
            ValidationResult validationResult = validateXmlAgainstXsd.validateXmlAgainstXsd(xmlContent);

            Map<String, Object> response = new HashMap<>();
            response.put("success", validationResult.isValid());
            response.put("validationResult", validationResult);

            if (!validationResult.isValid()) {
                response.put("message", "Validation failed");
            } else {
                response.put("message", "Validation successful");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating message: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping(value = "/rtp/supported-messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getSupportedMessageTypes() {
        List<String> supportedTypes = List.of("pacs.008");
        return ResponseEntity.ok(supportedTypes);
    }

}
