package com.finzly.galaxy.rtp.service;

import com.finzly.galaxy.rtp.model.*;
import com.finzly.galaxy.rtp.util.XmlProcessor;
import com.finzly.galaxy.rtp.validator.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EnhancedXmlGeneratorService {
    
    @Autowired
    private FieldConfigurationService fieldConfigurationService;
    
    @Autowired
    private ValidationService validationService;
    
    private static final List<String> CHRG_BR_VALUES = List.of("SLEV");
    private static final List<String> SVC_LVL_VALUES = List.of("SDVA");
    private static final List<String> CTGY_PURP_VALUES = List.of("BUSINESS", "CONSUMER");
    private static final List<String> CURRENCY_VALUES = List.of("USD", "EUR", "GBP", "CAD");
    private static final List<String> AMOUNT_RANGES = List.of("small", "medium", "large");
    
    public EnhancedGeneratorResponse generateMessages(EnhancedGeneratorRequest request) {
        log.info("Starting enhanced message generation for {} messages", request.getNumberOfFiles());
        
        List<EnhancedGeneratorResponse.GeneratedMessage> messages = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try {
            // Get field configuration
            FieldConfiguration config = fieldConfigurationService.getFieldConfiguration();
            Map<String, FieldConfiguration.FieldDefinition> fieldDefinitions = 
                    fieldConfigurationService.getFieldDefinitionMap();
            
            // Generate messages
            for (int i = 0; i < request.getNumberOfFiles(); i++) {
                try {
                    EnhancedGeneratorResponse.GeneratedMessage message = 
                            generateSingleMessage(request, i, config, fieldDefinitions);
                    messages.add(message);
                } catch (Exception e) {
                    log.error("Error generating message {}: {}", i, e.getMessage());
                    errors.add("Failed to generate message " + (i + 1) + ": " + e.getMessage());
                }
            }
            
            // Create metadata
            EnhancedGeneratorResponse.GenerationMetadata metadata = 
                    createGenerationMetadata(request, messages);
            
            // Generate report if requested
            String reportDownloadUrl = null;
            if (request.isGenerateReport()) {
                try {
                    reportDownloadUrl = generateReport(messages, request, config);
                } catch (Exception e) {
                    log.error("Error generating report: {}", e.getMessage());
                    warnings.add("Failed to generate report: " + e.getMessage());
                }
            }
            
            return EnhancedGeneratorResponse.builder()
                    .success(true)
                    .message("Successfully generated " + messages.size() + " messages")
                    .messages(messages)
                    .metadata(metadata)
                    .reportDownloadUrl(reportDownloadUrl)
                    .errors(errors)
                    .warnings(warnings)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error in enhanced message generation", e);
            return EnhancedGeneratorResponse.builder()
                    .success(false)
                    .message("Message generation failed: " + e.getMessage())
                    .errors(List.of(e.getMessage()))
                    .build();
        }
    }
    
    private EnhancedGeneratorResponse.GeneratedMessage generateSingleMessage(
            EnhancedGeneratorRequest request, 
            int index, 
            FieldConfiguration config,
            Map<String, FieldConfiguration.FieldDefinition> fieldDefinitions) {
        
        // Create document with all mandatory fields
        Document document = createBaseDocument(index, request);
        
        // Add selected optional fields
        Set<String> includedFields = new HashSet<>();
        Set<String> excludedFields = new HashSet<>();
        Map<String, String> fieldDescriptions = new HashMap<>();
        Map<String, Object> fieldValues = new HashMap<>();
        
        // Add mandatory fields
        for (FieldConfiguration.FieldDefinition field : config.getMandatoryFields()) {
            includedFields.add(field.getPath());
            fieldDescriptions.put(field.getPath(), field.getDescription());
            if (field.getIsoDescription() != null) {
                fieldDescriptions.put(field.getPath() + ".iso", field.getIsoDescription());
            }
        }
        
        // Add selected optional fields
        if (request.getSelectedOptionalFields() != null) {
            for (String fieldPath : request.getSelectedOptionalFields()) {
                FieldConfiguration.FieldDefinition fieldDef = fieldDefinitions.get(fieldPath);
                if (fieldDef != null) {
                    addOptionalField(document, fieldPath, fieldDef, request, index);
                    includedFields.add(fieldPath);
                    fieldDescriptions.put(fieldPath, fieldDef.getDescription());
                    if (fieldDef.getIsoDescription() != null) {
                        fieldDescriptions.put(fieldPath + ".iso", fieldDef.getIsoDescription());
                    }
                }
            }
        }
        
        // Add selected conditional fields
        if (request.getSelectedConditionalFields() != null) {
            for (String fieldPath : request.getSelectedConditionalFields()) {
                FieldConfiguration.FieldDefinition fieldDef = fieldDefinitions.get(fieldPath);
                if (fieldDef != null) {
                    addConditionalField(document, fieldPath, fieldDef, request, index);
                    includedFields.add(fieldPath);
                    fieldDescriptions.put(fieldPath, fieldDef.getDescription());
                    if (fieldDef.getIsoDescription() != null) {
                        fieldDescriptions.put(fieldPath + ".iso", fieldDef.getIsoDescription());
                    }
                }
            }
        }
        
        // Track excluded fields
        for (FieldConfiguration.FieldDefinition field : config.getOptionalFields()) {
            if (!includedFields.contains(field.getPath())) {
                excludedFields.add(field.getPath());
            }
        }
        for (FieldConfiguration.FieldDefinition field : config.getConditionalFields()) {
            if (!includedFields.contains(field.getPath())) {
                excludedFields.add(field.getPath());
            }
        }
        
        // Convert to XML
        String xmlContent = convertToXml(document);
        
        // Validate if requested
        boolean isValid = true;
        List<String> validationErrors = new ArrayList<>();
        if (request.isValidateAgainstXsd()) {
            try {
                com.finzly.galaxy.rtp.model.ValidationResult validationResult = validationService.validateXmlMessage(xmlContent, request.getMessageType());
                isValid = validationResult.isValid();
                if (!isValid) {
                    validationErrors = new ArrayList<>();
                    if (validationResult.getSchemaErrors() != null) {
                        validationErrors.addAll(validationResult.getSchemaErrors());
                    }
                    if (validationResult.getBusinessRuleErrors() != null) {
                        validationErrors.addAll(validationResult.getBusinessRuleErrors());
                    }
                }
            } catch (Exception e) {
                log.warn("Validation failed for message {}: {}", index, e.getMessage());
                validationErrors.add("Validation error: " + e.getMessage());
            }
        }
        
        return EnhancedGeneratorResponse.GeneratedMessage.builder()
                .messageId("MSG" + System.currentTimeMillis() + "-" + index)
                .xmlContent(xmlContent)
                .includedFields(new ArrayList<>(includedFields))
                .excludedFields(new ArrayList<>(excludedFields))
                .fieldDescriptions(fieldDescriptions)
                .isValid(isValid)
                .validationErrors(validationErrors)
                .testScenario(request.getTestScenario())
                .fieldValues(fieldValues)
                .build();
    }
    
    private Document createBaseDocument(int index, EnhancedGeneratorRequest request) {
        // Use existing XmlGeneratorService logic but with enhanced parameters
        Document document = new Document();
        
        // Group Header
        GroupHeader93 groupHeader = new GroupHeader93();
        groupHeader.setMsgId("MSG" + System.currentTimeMillis() + "-" + index);
        groupHeader.setCreDtTm(LocalDateTime.now());
        groupHeader.setNbOfTxs("1");
        
        // Amount based on request
        BigDecimal amount = getAmountForRequest(request);
        String currency = request.getCurrency() != null ? request.getCurrency() : "USD";
        groupHeader.setTtlIntrBkSttlmAmt(new ActiveCurrencyAndAmount(amount, currency));
        groupHeader.setIntrBkSttlmDt(LocalDate.now());
        
        // Settlement Info
        SettlementInstruction7 settlementInfo = new SettlementInstruction7();
        settlementInfo.setSttlmMtd("CLRG");
        ClearingSystemIdentification3Choice clearingSystem = new ClearingSystemIdentification3Choice();
        clearingSystem.setCd("TCH");
        settlementInfo.setClrSys(clearingSystem);
        groupHeader.setSttlmInf(settlementInfo);
        
        // Credit Transfer Transaction
        CreditTransferTransaction39 creditTransfer = new CreditTransferTransaction39();
        
        // Payment ID
        PaymentIdentification7 paymentId = new PaymentIdentification7();
        paymentId.setInstrId("INSTR" + System.currentTimeMillis() + "-" + index);
        paymentId.setEndToEndId("E2E" + System.currentTimeMillis() + "-" + index);
        paymentId.setTxId("TX" + System.currentTimeMillis() + "-" + index);
        creditTransfer.setPmtId(paymentId);
        
        // Payment Type Info
        PaymentTypeInformation28 paymentTypeInfo = new PaymentTypeInformation28();
        
        ServiceLevel8Choice serviceLevel = new ServiceLevel8Choice();
        serviceLevel.setCd(request.getServiceLevel() != null ? request.getServiceLevel() : "SDVA");
        paymentTypeInfo.setSvcLvl(serviceLevel);
        
        LocalInstrument2Choice localInstrument = new LocalInstrument2Choice();
        localInstrument.setPrtry("STANDARD");
        paymentTypeInfo.setLclInstrm(localInstrument);
        
        CategoryPurpose1Choice categoryPurpose = new CategoryPurpose1Choice();
        categoryPurpose.setPrtry(request.getBusinessType() != null ? request.getBusinessType() : "BUSINESS");
        paymentTypeInfo.setCtgyPurp(categoryPurpose);
        
        creditTransfer.setPmtTpInf(paymentTypeInfo);
        creditTransfer.setIntrBkSttlmAmt(new ActiveCurrencyAndAmount(amount, currency));
        creditTransfer.setChrgBr("SLEV");
        
        // Agents
        BranchAndFinancialInstitutionIdentification6 instgAgt = new BranchAndFinancialInstitutionIdentification6();
        FinancialInstitutionIdentification18 finInstnId = new FinancialInstitutionIdentification18();
        finInstnId.setBicfi("BOFAUS3N");
        instgAgt.setFinInstnId(finInstnId);
        creditTransfer.setInstgAgt(instgAgt);
        
        BranchAndFinancialInstitutionIdentification6 instdAgt = new BranchAndFinancialInstitutionIdentification6();
        FinancialInstitutionIdentification18 finInstnId2 = new FinancialInstitutionIdentification18();
        finInstnId2.setBicfi("CHASUS33");
        instdAgt.setFinInstnId(finInstnId2);
        creditTransfer.setInstdAgt(instdAgt);
        
        // Debtor
        PartyIdentification135 debtor = new PartyIdentification135();
        debtor.setNm("John Doe");
        creditTransfer.setDbtr(debtor);
        
        CashAccount38 debtorAccount = new CashAccount38();
        AccountIdentification4Choice debtorAccountId = new AccountIdentification4Choice();
        debtorAccountId.setIban("US12345678901234567890");
        debtorAccount.setId(debtorAccountId);
        debtorAccount.setNm("John Doe Account");
        creditTransfer.setDbtrAcct(debtorAccount);
        
        // Debtor Agent (required by schema)
        BranchAndFinancialInstitutionIdentification6 debtorAgent = new BranchAndFinancialInstitutionIdentification6();
        FinancialInstitutionIdentification18 debtorAgentFinInstnId = new FinancialInstitutionIdentification18();
        debtorAgentFinInstnId.setBicfi("BOFAUS3N");
        debtorAgent.setFinInstnId(debtorAgentFinInstnId);
        creditTransfer.setDbtrAgt(debtorAgent);
        
        // Creditor
        PartyIdentification135 creditor = new PartyIdentification135();
        creditor.setNm("Jane Smith");
        creditTransfer.setCdtr(creditor);
        
        CashAccount38 creditorAccount = new CashAccount38();
        AccountIdentification4Choice creditorAccountId = new AccountIdentification4Choice();
        creditorAccountId.setIban("US98765432109876543210");
        creditorAccount.setId(creditorAccountId);
        creditorAccount.setNm("Jane Smith Account");
        creditTransfer.setCdtrAcct(creditorAccount);
        
        // Creditor Agent (required by schema)
        BranchAndFinancialInstitutionIdentification6 creditorAgent = new BranchAndFinancialInstitutionIdentification6();
        FinancialInstitutionIdentification18 creditorAgentFinInstnId = new FinancialInstitutionIdentification18();
        creditorAgentFinInstnId.setBicfi("CHASUS33");
        creditorAgent.setFinInstnId(creditorAgentFinInstnId);
        creditTransfer.setCdtrAgt(creditorAgent);
        
        // Set up document
        FIToFICustomerCreditTransferV08 fitToFiCstmrCdtTrf = new FIToFICustomerCreditTransferV08();
        fitToFiCstmrCdtTrf.setGrpHdr(groupHeader);
        fitToFiCstmrCdtTrf.setCdtTrfTxInf(creditTransfer);
        
        document.setFiToFICstmrCdtTrf(fitToFiCstmrCdtTrf);
        
        return document;
    }
    
    private BigDecimal getAmountForRequest(EnhancedGeneratorRequest request) {
        String amountRange = request.getAmountRange() != null ? request.getAmountRange() : "medium";
        switch (amountRange.toLowerCase()) {
            case "small":
                return new BigDecimal("10.00");
            case "large":
                return new BigDecimal("10000.00");
            default:
                return new BigDecimal("1000.00");
        }
    }
    
    private void addOptionalField(Document document, String fieldPath, 
                                 FieldConfiguration.FieldDefinition fieldDef,
                                 EnhancedGeneratorRequest request, int index) {
        // Implementation for adding optional fields based on fieldPath
        // This would be expanded based on the specific field requirements
        log.debug("Adding optional field: {}", fieldPath);
    }
    
    private void addConditionalField(Document document, String fieldPath,
                                    FieldConfiguration.FieldDefinition fieldDef,
                                    EnhancedGeneratorRequest request, int index) {
        // Implementation for adding conditional fields based on fieldPath
        // This would be expanded based on the specific field requirements
        log.debug("Adding conditional field: {}", fieldPath);
    }
    
    private EnhancedGeneratorResponse.GenerationMetadata createGenerationMetadata(
            EnhancedGeneratorRequest request, 
            List<EnhancedGeneratorResponse.GeneratedMessage> messages) {
        
        return EnhancedGeneratorResponse.GenerationMetadata.builder()
                .messageType(request.getMessageType())
                .totalMessagesGenerated(messages.size())
                .numberOfFiles(request.getNumberOfFiles())
                .generatedAt(LocalDateTime.now())
                .testScenario(request.getTestScenario())
                .selectedOptionalFields(request.getSelectedOptionalFields())
                .selectedConditionalFields(request.getSelectedConditionalFields())
                .generationOptions(Map.of(
                        "amountRange", request.getAmountRange() != null ? request.getAmountRange() : "medium",
                        "currency", request.getCurrency() != null ? request.getCurrency() : "USD",
                        "businessType", request.getBusinessType() != null ? request.getBusinessType() : "BUSINESS",
                        "serviceLevel", request.getServiceLevel() != null ? request.getServiceLevel() : "SDVA"
                ))
                .reportFormat(request.getReportFormat())
                .reportGenerated(request.isGenerateReport())
                .build();
    }
    
    private String generateReport(List<EnhancedGeneratorResponse.GeneratedMessage> messages,
                                 EnhancedGeneratorRequest request,
                                 FieldConfiguration config) {
        // This will be implemented in the ExcelReportService
        return "/api/rtp/reports/download/" + System.currentTimeMillis();
    }
    
    private String convertToXml(Document document) {
        try {
            XmlProcessor xmlProcessor = new XmlProcessor();
            return xmlProcessor.marshalToXml(document);
        } catch (Exception e) {
            log.error("Error converting document to XML", e);
            throw new RuntimeException("Failed to convert document to XML", e);
        }
    }
}
