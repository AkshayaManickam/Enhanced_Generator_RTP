package com.finzly.galaxy.rtp.payment.rest;

import com.finzly.galaxy.rtp.payment.PaymentRequest;
import com.finzly.galaxy.rtp.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping(value = "/api/process-payment", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Process RTP Payment")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, String> requestBody) {
        log.info("=== Received payment processing request ===");
        log.info("Request Body: {}", requestBody);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String pacs008Message = requestBody.get("pacs008Message");
            String environment = requestBody.get("environment");
            String tenant = requestBody.get("tenant");
            
            log.info("Request details:");
            log.info("  Environment: {}", environment);
            log.info("  Tenant: {}", tenant);
            log.info("  Message length: {}", pacs008Message != null ? pacs008Message.length() : 0);
            
            // Validate inputs
            if (pacs008Message == null || pacs008Message.trim().isEmpty()) {
                log.warn("PACS.008 message is empty or null");
                response.put("success", false);
                response.put("message", "PACS.008 message is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (environment == null || environment.trim().isEmpty()) {
                log.warn("Environment is empty or null");
                response.put("success", false);
                response.put("message", "Environment is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (tenant == null || tenant.trim().isEmpty()) {
                log.warn("Tenant is empty or null");
                response.put("success", false);
                response.put("message", "Tenant is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("=== Processing payment for tenant: {}, environment: {} ===", tenant, environment);
            
            String result = paymentService.createInPayment(pacs008Message, environment, tenant);
            
            log.info("Payment service result: {}", result);
            
            if ("Success".equals(result)) {
                response.put("success", true);
                response.put("message", "Payment processed successfully");
                log.info("=== Payment processed successfully ===");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Payment processing failed: " + result);
                log.error("=== Payment processing failed: {} ===", result);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            log.error("=== Error processing payment ===", e);
            response.put("success", false);
            response.put("message", "Error processing payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Keep the old endpoint for backward compatibility
    @PostMapping(value = "/payment", produces = "application/json", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Process Incoming RTP (Legacy)")
    public ResponseEntity<String> rtpInMessage(@RequestBody String paymentRequest, @RequestParam String env, @RequestParam String tenant) {
        log.info("Received legacy payment request for tenant: {}, env: {}", tenant, env);
        paymentService.createInPayment(paymentRequest, env, tenant);
        return ResponseEntity.ok("SUCCESS");
    }
}
