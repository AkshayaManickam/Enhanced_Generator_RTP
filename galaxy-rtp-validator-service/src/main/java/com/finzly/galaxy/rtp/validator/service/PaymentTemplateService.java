package com.finzly.galaxy.rtp.validator.service;

import com.finzly.galaxy.rtp.validator.dto.PaymentInfoRequest;
import com.finzly.galaxy.rtp.validator.dto.PaymentTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class PaymentTemplateService {
    
    // In-memory storage for templates (replace with database in production)
    private final ConcurrentHashMap<Long, PaymentTemplate> templates = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public PaymentTemplate saveTemplate(PaymentInfoRequest request) {
        log.info("Saving payment template: {}", request.getTemplateName());
        
        PaymentTemplate template = PaymentTemplate.builder()
                .id(idGenerator.getAndIncrement())
                .templateName(request.getTemplateName())
                .senderRoutingNumber(request.getSenderRoutingNumber())
                .receiverRoutingNumber(request.getReceiverRoutingNumber())
                .senderAccountNumber(request.getSenderAccountNumber())
                .receiverAccountNumber(request.getReceiverAccountNumber())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        templates.put(template.getId(), template);
        log.info("Template saved successfully with ID: {}", template.getId());
        
        return template;
    }

    public List<PaymentTemplate> getAllTemplates() {
        log.info("Retrieving all payment templates. Count: {}", templates.size());
        return new ArrayList<>(templates.values());
    }

    public PaymentTemplate getTemplateById(Long id) {
        log.info("Retrieving template by ID: {}", id);
        return templates.get(id);
    }

    public PaymentTemplate updateTemplate(Long id, PaymentInfoRequest request) {
        log.info("Updating template ID: {}", id);
        
        PaymentTemplate existing = templates.get(id);
        if (existing == null) {
            log.warn("Template not found with ID: {}", id);
            return null;
        }
        
        existing.setTemplateName(request.getTemplateName());
        existing.setSenderRoutingNumber(request.getSenderRoutingNumber());
        existing.setReceiverRoutingNumber(request.getReceiverRoutingNumber());
        existing.setSenderAccountNumber(request.getSenderAccountNumber());
        existing.setReceiverAccountNumber(request.getReceiverAccountNumber());
        existing.setAmount(request.getAmount());
        existing.setCurrency(request.getCurrency());
        existing.setUpdatedAt(LocalDateTime.now());
        
        log.info("Template updated successfully");
        return existing;
    }

    public boolean deleteTemplate(Long id) {
        log.info("Deleting template ID: {}", id);
        PaymentTemplate removed = templates.remove(id);
        return removed != null;
    }

    public long getTemplateCount() {
        return templates.size();
    }
}

