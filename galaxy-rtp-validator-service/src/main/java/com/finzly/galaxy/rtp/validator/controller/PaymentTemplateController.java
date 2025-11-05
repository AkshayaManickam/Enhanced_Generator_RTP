package com.finzly.galaxy.rtp.validator.controller;

import com.finzly.galaxy.rtp.validator.dto.PaymentInfoRequest;
import com.finzly.galaxy.rtp.validator.dto.PaymentTemplate;
import com.finzly.galaxy.rtp.validator.service.PaymentTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-templates")
@CrossOrigin(origins = "*")
@Slf4j
public class PaymentTemplateController {

    @Autowired
    private PaymentTemplateService paymentTemplateService;

    @PostMapping
    public ResponseEntity<PaymentTemplate> saveTemplate(@RequestBody PaymentInfoRequest request) {
        log.info("Received request to save payment template: {}", request.getTemplateName());
        try {
            PaymentTemplate template = paymentTemplateService.saveTemplate(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(template);
        } catch (Exception e) {
            log.error("Error saving payment template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<PaymentTemplate>> getAllTemplates() {
        log.info("Received request to get all payment templates");
        try {
            List<PaymentTemplate> templates = paymentTemplateService.getAllTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("Error retrieving payment templates", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentTemplate> getTemplateById(@PathVariable Long id) {
        log.info("Received request to get payment template by ID: {}", id);
        try {
            PaymentTemplate template = paymentTemplateService.getTemplateById(id);
            if (template == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            log.error("Error retrieving payment template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentTemplate> updateTemplate(@PathVariable Long id, @RequestBody PaymentInfoRequest request) {
        log.info("Received request to update payment template ID: {}", id);
        try {
            PaymentTemplate template = paymentTemplateService.updateTemplate(id, request);
            if (template == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(template);
        } catch (Exception e) {
            log.error("Error updating payment template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        log.info("Received request to delete payment template ID: {}", id);
        try {
            boolean deleted = paymentTemplateService.deleteTemplate(id);
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting payment template", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTemplateCount() {
        log.info("Received request to get template count");
        try {
            long count = paymentTemplateService.getTemplateCount();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error getting template count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

