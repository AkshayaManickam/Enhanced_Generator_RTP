package com.finzly.galaxy.rtp.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry containing all pacs.008 field definitions
 * This class defines all fields with their M/O/C indicators and constraints
 */
@Component
@Slf4j
public class Pacs008FieldRegistry {
    
    private Map<String, FieldDefinition> fieldDefinitions;
    private List<FieldDefinition> mandatoryFields;
    private List<FieldDefinition> optionalFields;
    private List<FieldDefinition> conditionalFields;
    
    @PostConstruct
    public void initializeFields() {
        log.info("Initializing pacs.008 field definitions - Adding fields from provided specification");
        
        fieldDefinitions = new HashMap<>();
        mandatoryFields = new ArrayList<>();
        optionalFields = new ArrayList<>();
        conditionalFields = new ArrayList<>();
        
        // Add mandatory fields from the provided specification
        initializeMandatoryFields();
        
        // Add credit transfer transaction fields
        initializeCreditTransferFields();
        
        // Add previous instructing agent fields
        initializePreviousInstructingAgentFields();
        
        // Add additional agent fields
        initializeAdditionalAgentFields();
        
        // Add intermediary agent fields
        initializeIntermediaryAgentFields();
        
        // Add ultimate debtor fields
        initializeUltimateDebtorFields();
        
        // Add debtor fields
        initializeDebtorFields();
        
        // Add additional debtor fields
        initializeAdditionalDebtorFields();
        
        // Add creditor and agent account fields
        initializeCreditorAndAgentAccountFields();
        
        // Add additional creditor fields
        initializeAdditionalCreditorFields();
        
        // Add ultimate creditor identification fields
        initializeUltimateCreditorIdentificationFields();
        
        // Add remittance and instruction fields
        initializeRemittanceAndInstructionFields();
        
        // Add invoicer and invoicee fields
        initializeInvoicerAndInvoiceeFields();
        
        // Categorize fields
        categorizeFields();
        
        log.info("Field registry initialized with {} total fields: {} mandatory, {} optional, {} conditional", 
                fieldDefinitions.size(), mandatoryFields.size(), optionalFields.size(), conditionalFields.size());
    }
    
    /**
     * Initialize mandatory fields from the provided specification
     */
    private void initializeMandatoryFields() {
        // 1.0 GrpHdr - Group Header
        addField(FieldDefinition.builder()
                .fieldName("GrpHdr")
                .xpath("/Document/FIToFICstmrCdtTrf/GrpHdr")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("GroupHeader93")
                .category(FieldDefinition.FieldCategory.GROUP_HEADER)
                .description("Group Header")
                .build());
        
        // 1.1 MsgId - Message Identification
        addField(FieldDefinition.builder()
                .fieldName("MsgId")
                .xpath("/Document/FIToFICstmrCdtTrf/GrpHdr/MsgId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.GROUP_HEADER)
                .description("Message Identification")
                .build());
        
        // 1.2 CreDtTm - Creation Date Time
        addField(FieldDefinition.builder()
                .fieldName("CreDtTm")
                .xpath("/Document/FIToFICstmrCdtTrf/GrpHdr/CreDtTm")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ISODateTime")
                .maxLength(19)
                .category(FieldDefinition.FieldCategory.GROUP_HEADER)
                .description("Creation Date Time")
                .build());
        
        // 1.4 NbOfTxs - Number Of Transactions
        addField(FieldDefinition.builder()
                .fieldName("NbOfTxs")
                .xpath("/Document/FIToFICstmrCdtTrf/GrpHdr/NbOfTxs")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max15NumericText")
                .maxLength(1)
                .category(FieldDefinition.FieldCategory.GROUP_HEADER)
                .description("Number Of Transactions")
                .build());
        
        // 1.6 TtlIntrBkSttlmAmt - Total Interbank Settlement Amount
        addField(FieldDefinition.builder()
                .fieldName("TtlIntrBkSttlmAmt")
                .xpath("/Document/FIToFICstmrCdtTrf/GrpHdr/TtlIntrBkSttlmAmt")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ActiveCurrencyAndAmount")
                .maxLength(18)
                .category(FieldDefinition.FieldCategory.AMOUNT_INFO)
                .description("Total Interbank Settlement Amount")
                .build());
        
        // 1.7 Ccy - Currency (attribute of TtlIntrBkSttlmAmt)
        addField(FieldDefinition.builder()
                .fieldName("TtlIntrBkSttlmAmtCcy")
                .xpath("/Document/FIToFICstmrCdtTrf/GrpHdr/TtlIntrBkSttlmAmt/@Ccy")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ActiveCurrencyCode")
                .maxLength(3)
                .allowedValues(Arrays.asList("USD"))
                .category(FieldDefinition.FieldCategory.AMOUNT_INFO)
                .description("Total Interbank Settlement Amount Currency")
                .build());
        
        // 1.8 IntrBkSttlmDt - Interbank Settlement Date
        addField(FieldDefinition.builder()
                .fieldName("IntrBkSttlmDt")
                .xpath("/Document/FIToFICstmrCdtTrf/GrpHdr/IntrBkSttlmDt")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ISODate")
                .maxLength(10)
                .category(FieldDefinition.FieldCategory.GROUP_HEADER)
                .description("Interbank Settlement Date")
                .build());
        
        // 1.9 SttlmInf - Settlement Information
        addField(FieldDefinition.builder()
                .fieldName("SttlmInf")
                .xpath("/Document/FIToFICstmrCdtTrf/GrpHdr/SttlmInf")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("SettlementInstruction7")
                .category(FieldDefinition.FieldCategory.SETTLEMENT_INFO)
                .description("Settlement Information")
                .build());
        
        // 1.10 SttlmMtd - Settlement Method
        addField(FieldDefinition.builder()
                .fieldName("SttlmMtd")
                .xpath("/Document/FIToFICstmrCdtTrf/GrpHdr/SttlmInf/SttlmMtd")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("SettlementMethod1Code")
                .maxLength(4)
                .allowedValues(Arrays.asList("CLRG"))
                .category(FieldDefinition.FieldCategory.SETTLEMENT_INFO)
                .description("Settlement Method")
                .build());
        
        // 1.30 ClrSys - Clearing System
        addField(FieldDefinition.builder()
                .fieldName("ClrSys")
                .xpath("/Document/FIToFICstmrCdtTrf/GrpHdr/SttlmInf/ClrSys")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ClearingSystemIdentification3Choice")
                .category(FieldDefinition.FieldCategory.SETTLEMENT_INFO)
                .description("Clearing System")
                .build());
        
        // 1.31 Cd - Code (child of ClrSys)
        addField(FieldDefinition.builder()
                .fieldName("ClrSysCd")
                .xpath("/Document/FIToFICstmrCdtTrf/GrpHdr/SttlmInf/ClrSys/Cd")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ExternalCashClearingSystem1Code")
                .maxLength(3)
                .allowedValues(Arrays.asList("TCH"))
                .category(FieldDefinition.FieldCategory.SETTLEMENT_INFO)
                .description("Clearing System Code")
                .build());
    }
    
    /**
     * Initialize credit transfer transaction fields from the provided specification
     */
    private void initializeCreditTransferFields() {
        // 2.0 CdtTrfTxInf - Credit Transfer Transaction Information
        addField(FieldDefinition.builder()
                .fieldName("CdtTrfTxInf")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("CreditTransferTransaction39")
                .category(FieldDefinition.FieldCategory.CREDIT_TRANSFER)
                .description("Credit Transfer Transaction Information")
                .build());
        
        // 2.1 PmtId - Payment Identification
        addField(FieldDefinition.builder()
                .fieldName("PmtId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("PaymentIdentification7")
                .category(FieldDefinition.FieldCategory.PAYMENT_IDENTIFICATION)
                .description("Payment Identification")
                .build());
        
        // 2.2 InstrId - Instruction Identification
        addField(FieldDefinition.builder()
                .fieldName("InstrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtId/InstrId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.PAYMENT_IDENTIFICATION)
                .description("Instruction Identification")
                .build());
        
        // 2.3 EndToEndId - End To End Identification
        addField(FieldDefinition.builder()
                .fieldName("EndToEndId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtId/EndToEndId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.PAYMENT_IDENTIFICATION)
                .description("End To End Identification")
                .build());
        
        // 2.4 TxId - Transaction Identification
        addField(FieldDefinition.builder()
                .fieldName("TxId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtId/TxId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.PAYMENT_IDENTIFICATION)
                .description("Transaction Identification")
                .build());
        
        // 2.5 UETR - UETR (Optional)
        addField(FieldDefinition.builder()
                .fieldName("UETR")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtId/UETR")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("UUIDv4Identifier")
                .maxLength(36)
                .category(FieldDefinition.FieldCategory.PAYMENT_IDENTIFICATION)
                .description("UETR")
                .build());
        
        // 2.6 ClrSysRef - Clearing System Reference (Optional)
        addField(FieldDefinition.builder()
                .fieldName("ClrSysRef")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtId/ClrSysRef")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.PAYMENT_IDENTIFICATION)
                .description("Clearing System Reference")
                .build());
        
        // 2.7 PmtTpInf - Payment Type Information
        addField(FieldDefinition.builder()
                .fieldName("PmtTpInf")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtTpInf")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("PaymentTypeInformation28")
                .category(FieldDefinition.FieldCategory.PAYMENT_TYPE_INFO)
                .description("Payment Type Information")
                .build());
        
        // 2.10 SvcLvl - Service Level
        addField(FieldDefinition.builder()
                .fieldName("SvcLvl")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtTpInf/SvcLvl")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ServiceLevel8Choice")
                .category(FieldDefinition.FieldCategory.PAYMENT_TYPE_INFO)
                .description("Service Level")
                .build());
        
        // 2.11 Cd - Code (Service Level)
        addField(FieldDefinition.builder()
                .fieldName("SvcLvlCd")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtTpInf/SvcLvl/Cd")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ExternalServiceLevel1Code")
                .maxLength(4)
                .allowedValues(Arrays.asList("SDVA"))
                .category(FieldDefinition.FieldCategory.PAYMENT_TYPE_INFO)
                .description("Service Level Code")
                .build());
        
        // 2.13 LclInstrm - Local Instrument
        addField(FieldDefinition.builder()
                .fieldName("LclInstrm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtTpInf/LclInstrm")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("LocalInstrument2Choice")
                .category(FieldDefinition.FieldCategory.PAYMENT_TYPE_INFO)
                .description("Local Instrument")
                .build());
        
        // 2.15 Prtry - Proprietary (Local Instrument)
        addField(FieldDefinition.builder()
                .fieldName("LclInstrmPrtry")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtTpInf/LclInstrm/Prtry")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .allowedValues(Arrays.asList("STANDARD", "ZELLE", "IXB", "OLO", "INTERMEDIARY", "INDIRECT DOMESTIC", "FOREIGN AFFILIATE"))
                .category(FieldDefinition.FieldCategory.PAYMENT_TYPE_INFO)
                .description("Local Instrument Proprietary")
                .build());
        
        // 2.16 CtgyPurp - Category Purpose
        addField(FieldDefinition.builder()
                .fieldName("CtgyPurp")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtTpInf/CtgyPurp")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("CategoryPurpose1Choice")
                .category(FieldDefinition.FieldCategory.PAYMENT_TYPE_INFO)
                .description("Category Purpose")
                .build());
        
        // 2.18 Prtry - Proprietary (Category Purpose)
        addField(FieldDefinition.builder()
                .fieldName("CtgyPurpPrtry")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtTpInf/CtgyPurp/Prtry")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .allowedValues(Arrays.asList("CONSUMER", "BUSINESS"))
                .category(FieldDefinition.FieldCategory.PAYMENT_TYPE_INFO)
                .description("Category Purpose Proprietary")
                .build());
        
        // 2.19 IntrBkSttlmAmt - Interbank Settlement Amount
        addField(FieldDefinition.builder()
                .fieldName("IntrBkSttlmAmt")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrBkSttlmAmt")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ActiveCurrencyAndAmount")
                .maxLength(18)
                .category(FieldDefinition.FieldCategory.AMOUNT_INFO)
                .description("Interbank Settlement Amount")
                .build());
        
        // 2.20 Ccy - Currency (attribute of IntrBkSttlmAmt)
        addField(FieldDefinition.builder()
                .fieldName("IntrBkSttlmAmtCcy")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrBkSttlmAmt/@Ccy")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ActiveCurrencyCode")
                .maxLength(3)
                .allowedValues(Arrays.asList("USD"))
                .category(FieldDefinition.FieldCategory.AMOUNT_INFO)
                .description("Interbank Settlement Amount Currency")
                .build());
        
        // 2.36 ChrgBr - Charge Bearer
        addField(FieldDefinition.builder()
                .fieldName("ChrgBr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/ChrgBr")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ChargeBearerType1Code")
                .maxLength(4)
                .allowedValues(Arrays.asList("SLEV"))
                .category(FieldDefinition.FieldCategory.CREDIT_TRANSFER)
                .description("Charge Bearer")
                .build());
        
        // 2.104 PrvsInstgAgt1 - Previous Instructing Agent 1 (Optional)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt1")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt1")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("BranchAndFinancialInstitutionIdentification6")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Previous Instructing Agent 1")
                .build());
        
        // 2.105 FinInstnId - Financial Institution Identification
        addField(FieldDefinition.builder()
                .fieldName("FinInstnId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt1/FinInstnId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("FinancialInstitutionIdentification18")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Financial Institution Identification")
                .build());
        
        // 2.106 BICFI - BICFI (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("BICFI")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt1/FinInstnId/BICFI")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("BICFIDec2014Identifier")
                .maxLength(11)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("BICFI")
                .build());
        
        // 2.107 ClrSysMmbId - Clearing System Member Identification (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("ClrSysMmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt1/FinInstnId/ClrSysMmbId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("ClearingSystemMemberIdentification2")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Clearing System Member Identification")
                .build());
        
        // 2.111 MmbId - Member Identification
        addField(FieldDefinition.builder()
                .fieldName("MmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt1/FinInstnId/ClrSysMmbId/MmbId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(9)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Member Identification")
                .build());
    }
    
    /**
     * Initialize previous instructing agent fields from the provided specification
     */
    private void initializePreviousInstructingAgentFields() {
        // 2.168 PrvsInstgAgt1Acct - Previous Instructing Agent 1Account (Optional)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt1Acct")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt1Acct")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("CashAccount38")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Previous Instructing Agent 1Account")
                .build());
        
        // 2.169 Id - Identification (child of PrvsInstgAgt1Acct)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt1AcctId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt1Acct/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("AccountIdentification4Choice")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Previous Instructing Agent 1Account Identification")
                .build());
        
        // 2.170 IBAN - IBAN (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt1AcctIBAN")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt1Acct/Id/IBAN")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("IBAN2007Identifier")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Previous Instructing Agent 1Account IBAN")
                .build());
        
        // 2.171 Othr - Other (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt1AcctOthr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt1Acct/Id/Othr")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("GenericAccountIdentification1")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Previous Instructing Agent 1Account Other")
                .build());
        
        // 2.172 Id - Identification (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt1AcctOthrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt1Acct/Id/Othr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max34Text")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Previous Instructing Agent 1Account Other Identification")
                .build());
        
        // 2.187 PrvsInstgAgt2 - Previous Instructing Agent 2 (Optional)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt2")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt2")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("BranchAndFinancialInstitutionIdentification6")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Previous Instructing Agent 2")
                .build());
        
        // 2.188 FinInstnId - Financial Institution Identification (child of PrvsInstgAgt2)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt2FinInstnId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt2/FinInstnId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("FinancialInstitutionIdentification18")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Previous Instructing Agent 2 Financial Institution Identification")
                .build());
        
        // 2.189 BICFI - BICFI (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt2BICFI")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt2/FinInstnId/BICFI")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("BICFIDec2014Identifier")
                .maxLength(11)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Previous Instructing Agent 2 BICFI")
                .build());
        
        // 2.190 ClrSysMmbId - Clearing System Member Identification (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt2ClrSysMmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt2/FinInstnId/ClrSysMmbId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("ClearingSystemMemberIdentification2")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Previous Instructing Agent 2 Clearing System Member Identification")
                .build());
        
        // 2.194 MmbId - Member Identification (child of ClrSysMmbId)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt2MmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt2/FinInstnId/ClrSysMmbId/MmbId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(9)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Previous Instructing Agent 2 Member Identification")
                .build());
        
        // 2.251 PrvsInstgAgt2Acct - Previous Instructing Agent 2Account (Optional)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt2Acct")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt2Acct")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("CashAccount38")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Previous Instructing Agent 2Account")
                .build());
        
        // 2.252 Id - Identification (child of PrvsInstgAgt2Acct)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt2AcctId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt2Acct/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("AccountIdentification4Choice")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Previous Instructing Agent 2Account Identification")
                .build());
        
        // 2.253 IBAN - IBAN (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt2AcctIBAN")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt2Acct/Id/IBAN")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("IBAN2007Identifier")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Previous Instructing Agent 2Account IBAN")
                .build());
    }
    
    /**
     * Initialize additional agent fields from the provided specification
     */
    private void initializeAdditionalAgentFields() {
        // 2.254 Othr - Other (Conditional, part of PrvsInstgAgt2Acct/Id)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt2AcctOthr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt2Acct/Id/Othr")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("GenericAccountIdentification1")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Previous Instructing Agent 2Account Other")
                .build());
        
        // 2.255 Id - Identification (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt2AcctOthrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt2Acct/Id/Othr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max34Text")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Previous Instructing Agent 2Account Other Identification")
                .build());
        
        // 2.270 PrvsInstgAgt3 - Previous Instructing Agent 3 (Optional)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt3")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt3")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("BranchAndFinancialInstitutionIdentification6")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Previous Instructing Agent 3")
                .build());
        
        // 2.271 FinInstnId - Financial Institution Identification (child of PrvsInstgAgt3)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt3FinInstnId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt3/FinInstnId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("FinancialInstitutionIdentification18")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Previous Instructing Agent 3 Financial Institution Identification")
                .build());
        
        // 2.272 BICFI - BICFI (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt3BICFI")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt3/FinInstnId/BICFI")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("BICFIDec2014Identifier")
                .maxLength(11)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Previous Instructing Agent 3 BICFI")
                .build());
        
        // 2.273 ClrSysMmbId - Clearing System Member Identification (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt3ClrSysMmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt3/FinInstnId/ClrSysMmbId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("ClearingSystemMemberIdentification2")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Previous Instructing Agent 3 Clearing System Member Identification")
                .build());
        
        // 2.277 MmbId - Member Identification (child of ClrSysMmbId)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt3MmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt3/FinInstnId/ClrSysMmbId/MmbId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(9)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Previous Instructing Agent 3 Member Identification")
                .build());
        
        // 2.334 PrvsInstgAgt3Acct - Previous Instructing Agent 3Account (Optional)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt3Acct")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt3Acct")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("CashAccount38")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Previous Instructing Agent 3Account")
                .build());
        
        // 2.335 Id - Identification (child of PrvsInstgAgt3Acct)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt3AcctId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt3Acct/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("AccountIdentification4Choice")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Previous Instructing Agent 3Account Identification")
                .build());
        
        // 2.336 IBAN - IBAN (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt3AcctIBAN")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt3Acct/Id/IBAN")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("IBAN2007Identifier")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Previous Instructing Agent 3Account IBAN")
                .build());
        
        // 2.337 Othr - Other (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt3AcctOthr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt3Acct/Id/Othr")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("GenericAccountIdentification1")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Previous Instructing Agent 3Account Other")
                .build());
        
        // 2.338 Id - Identification (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("PrvsInstgAgt3AcctOthrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PrvsInstgAgt3Acct/Id/Othr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max34Text")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Previous Instructing Agent 3Account Other Identification")
                .build());
        
        // 2.353 InstgAgt - Instructing Agent (Mandatory)
        addField(FieldDefinition.builder()
                .fieldName("InstgAgt")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/InstgAgt")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("BranchAndFinancialInstitutionIdentification6")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Instructing Agent")
                .build());
        
        // 2.354 FinInstnId - Financial Institution Identification (child of InstgAgt)
        addField(FieldDefinition.builder()
                .fieldName("InstgAgtFinInstnId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/InstgAgt/FinInstnId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("FinancialInstitutionIdentification18")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Instructing Agent Financial Institution Identification")
                .build());
        
        // 2.356 ClrSysMmbId - Clearing System Member Identification (child of InstgAgt FinInstnId)
        addField(FieldDefinition.builder()
                .fieldName("InstgAgtClrSysMmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/InstgAgt/FinInstnId/ClrSysMmbId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ClearingSystemMemberIdentification2")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Instructing Agent Clearing System Member Identification")
                .build());
        
        // 2.360 MmbId - Member Identification (child of InstgAgt ClrSysMmbId)
        addField(FieldDefinition.builder()
                .fieldName("InstgAgtMmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/InstgAgt/FinInstnId/ClrSysMmbId/MmbId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(9)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Instructing Agent Member Identification")
                .build());
        
        // 2.417 InstdAgt - Instructed Agent (Mandatory)
        addField(FieldDefinition.builder()
                .fieldName("InstdAgt")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/InstdAgt")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("BranchAndFinancialInstitutionIdentification6")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Instructed Agent")
                .build());
        
        // 2.418 FinInstnId - Financial Institution Identification (child of InstdAgt)
        addField(FieldDefinition.builder()
                .fieldName("InstdAgtFinInstnId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/InstdAgt/FinInstnId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("FinancialInstitutionIdentification18")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Instructed Agent Financial Institution Identification")
                .build());
        
        // 2.420 ClrSysMmbId - Clearing System Member Identification (child of InstdAgt FinInstnId)
        addField(FieldDefinition.builder()
                .fieldName("InstdAgtClrSysMmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/InstdAgt/FinInstnId/ClrSysMmbId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ClearingSystemMemberIdentification2")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Instructed Agent Clearing System Member Identification")
                .build());
        
        // 2.424 MmbId - Member Identification (child of InstdAgt ClrSysMmbId)
        addField(FieldDefinition.builder()
                .fieldName("InstdAgtMmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/InstdAgt/FinInstnId/ClrSysMmbId/MmbId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(9)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Instructed Agent Member Identification")
                .build());
    }
    
    /**
     * Initialize intermediary agent fields from the provided specification
     */
    private void initializeIntermediaryAgentFields() {
        // 2.481 IntrmyAgt1 - Intermediary Agent 1 (Optional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt1")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt1")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("BranchAndFinancialInstitutionIdentification6")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Intermediary Agent 1")
                .build());
        
        // 2.482 FinInstnId - Financial Institution Identification (child of IntrmyAgt1)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt1FinInstnId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt1/FinInstnId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("FinancialInstitutionIdentification18")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Intermediary Agent 1 Financial Institution Identification")
                .build());
        
        // 2.483 BICFI - BICFI (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt1BICFI")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt1/FinInstnId/BICFI")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("BICFIDec2014Identifier")
                .maxLength(11)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Intermediary Agent 1 BICFI")
                .build());
        
        // 2.484 ClrSysMmbId - Clearing System Member Identification (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt1ClrSysMmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt1/FinInstnId/ClrSysMmbId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("ClearingSystemMemberIdentification2")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Intermediary Agent 1 Clearing System Member Identification")
                .build());
        
        // 2.488 MmbId - Member Identification (child of ClrSysMmbId)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt1MmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt1/FinInstnId/ClrSysMmbId/MmbId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(9)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Intermediary Agent 1 Member Identification")
                .build());
        
        // 2.545 IntrmyAgt1Acct - Intermediary Agent 1Account (Optional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt1Acct")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt1Acct")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("CashAccount38")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Intermediary Agent 1Account")
                .build());
        
        // 2.546 Id - Identification (child of IntrmyAgt1Acct)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt1AcctId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt1Acct/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("AccountIdentification4Choice")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Intermediary Agent 1Account Identification")
                .build());
        
        // 2.547 IBAN - IBAN (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt1AcctIBAN")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt1Acct/Id/IBAN")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("IBAN2007Identifier")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Intermediary Agent 1Account IBAN")
                .build());
        
        // 2.548 Othr - Other (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt1AcctOthr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt1Acct/Id/Othr")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("GenericAccountIdentification1")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Intermediary Agent 1Account Other")
                .build());
        
        // 2.549 Id - Identification (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt1AcctOthrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt1Acct/Id/Othr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max34Text")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Intermediary Agent 1Account Other Identification")
                .build());
        
        // 2.564 IntrmyAgt2 - Intermediary Agent 2 (Optional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt2")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt2")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("BranchAndFinancialInstitutionIdentification6")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Intermediary Agent 2")
                .build());
        
        // 2.565 FinInstnId - Financial Institution Identification (child of IntrmyAgt2)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt2FinInstnId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt2/FinInstnId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("FinancialInstitutionIdentification18")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Intermediary Agent 2 Financial Institution Identification")
                .build());
        
        // 2.566 BICFI - BICFI (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt2BICFI")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt2/FinInstnId/BICFI")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("BICFIDec2014Identifier")
                .maxLength(11)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Intermediary Agent 2 BICFI")
                .build());
        
        // 2.567 ClrSysMmbId - Clearing System Member Identification (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt2ClrSysMmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt2/FinInstnId/ClrSysMmbId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("ClearingSystemMemberIdentification2")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Intermediary Agent 2 Clearing System Member Identification")
                .build());
        
        // 2.571 MmbId - Member Identification (child of ClrSysMmbId)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt2MmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt2/FinInstnId/ClrSysMmbId/MmbId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(9)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Intermediary Agent 2 Member Identification")
                .build());
        
        // 2.628 IntrmyAgt2Acct - Intermediary Agent 2Account (Optional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt2Acct")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt2Acct")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("CashAccount38")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Intermediary Agent 2Account")
                .build());
        
        // 2.629 Id - Identification (child of IntrmyAgt2Acct)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt2AcctId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt2Acct/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("AccountIdentification4Choice")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Intermediary Agent 2Account Identification")
                .build());
        
        // 2.630 IBAN - IBAN (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt2AcctIBAN")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt2Acct/Id/IBAN")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("IBAN2007Identifier")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Intermediary Agent 2Account IBAN")
                .build());
        
        // 2.631 Othr - Other (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt2AcctOthr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt2Acct/Id/Othr")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("GenericAccountIdentification1")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Intermediary Agent 2Account Other")
                .build());
        
        // 2.632 Id - Identification (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt2AcctOthrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt2Acct/Id/Othr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max34Text")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Intermediary Agent 2Account Other Identification")
                .build());
        
        // 2.647 IntrmyAgt3 - Intermediary Agent 3 (Optional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt3")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt3")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("BranchAndFinancialInstitutionIdentification6")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Intermediary Agent 3")
                .build());
        
        // 2.648 FinInstnId - Financial Institution Identification (child of IntrmyAgt3)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt3FinInstnId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt3/FinInstnId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("FinancialInstitutionIdentification18")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Intermediary Agent 3 Financial Institution Identification")
                .build());
        
        // 2.649 BICFI - BICFI (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt3BICFI")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt3/FinInstnId/BICFI")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("BICFIDec2014Identifier")
                .maxLength(11)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Intermediary Agent 3 BICFI")
                .build());
        
        // 2.650 ClrSysMmbId - Clearing System Member Identification (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt3ClrSysMmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt3/FinInstnId/ClrSysMmbId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("ClearingSystemMemberIdentification2")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Intermediary Agent 3 Clearing System Member Identification")
                .build());
        
        // 2.654 MmbId - Member Identification (child of ClrSysMmbId)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt3MmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt3/FinInstnId/ClrSysMmbId/MmbId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(9)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Intermediary Agent 3 Member Identification")
                .build());
    }
    
    /**
     * Initialize ultimate debtor fields from the provided specification
     */
    private void initializeUltimateDebtorFields() {
        // 2.711 IntrmyAgt3Acct - Intermediary Agent 3Account (Optional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt3Acct")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt3Acct")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("CashAccount38")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Intermediary Agent 3Account")
                .build());
        
        // 2.712 Id - Identification (child of IntrmyAgt3Acct)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt3AcctId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt3Acct/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("AccountIdentification4Choice")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Intermediary Agent 3Account Identification")
                .build());
        
        // 2.713 IBAN - IBAN (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt3AcctIBAN")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt3Acct/Id/IBAN")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("IBAN2007Identifier")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Intermediary Agent 3Account IBAN")
                .build());
        
        // 2.714 Othr - Other (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt3AcctOthr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt3Acct/Id/Othr")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("GenericAccountIdentification1")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Intermediary Agent 3Account Other")
                .build());
        
        // 2.715 Id - Identification (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("IntrmyAgt3AcctOthrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrmyAgt3Acct/Id/Othr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max34Text")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Intermediary Agent 3Account Other Identification")
                .build());
        
        // 2.730 UltmtDbtr - Ultimate Debtor (Optional)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("PartyIdentification135")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Debtor")
                .build());
        
        // 2.731 Nm - Name (child of UltmtDbtr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/Nm")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max140Text")
                .maxLength(140)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Debtor Name")
                .build());
        
        // 2.732 PstlAdr - Postal Address (child of UltmtDbtr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrPstlAdr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/PstlAdr")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("PostalAddress24")
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Debtor Postal Address")
                .build());
        
        // 2.741 StrtNm - Street Name (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrStrtNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/PstlAdr/StrtNm")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max70Text")
                .maxLength(70)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Debtor Street Name")
                .build());
        
        // 2.742 BldgNb - Building Number (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrBldgNb")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/PstlAdr/BldgNb")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("Max16Text")
                .maxLength(16)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Debtor Building Number")
                .build());
        
        // 2.747 PstCd - Post Code (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrPstCd")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/PstlAdr/PstCd")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max16Text")
                .maxLength(16)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Debtor Post Code")
                .build());
        
        // 2.748 TwnNm - Town Name (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrTwnNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/PstlAdr/TwnNm")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Debtor Town Name")
                .build());
        
        // 2.751 CtrySubDvsn - Country Sub Division (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrCtrySubDvsn")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/PstlAdr/CtrySubDvsn")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Debtor Country Sub Division")
                .build());
        
        // 2.752 Ctry - Country (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrCtry")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/PstlAdr/Ctry")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("CountryCode")
                .maxLength(2)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Debtor Country")
                .build());
        
        // 2.753 AdrLine - Address Line (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrAdrLine")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/PstlAdr/AdrLine")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("Max70Text")
                .maxLength(70)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Debtor Address Line")
                .build());
        
        // 2.754 Id - Identification (child of UltmtDbtr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/Id")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("Party38Choice")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Debtor Identification")
                .build());
        
        // 2.755 OrgId - Organisation Identification (child of Id)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrOrgId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/Id/OrgId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("OrganisationIdentification29")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Debtor Organisation Identification")
                .build());
        
        // 2.756 AnyBIC - Any BIC (child of OrgId)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrAnyBIC")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/Id/OrgId/AnyBIC")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("AnyBICDec2014Identifier")
                .maxLength(11)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Debtor Any BIC")
                .build());
        
        // 2.757 LEI - LEI (child of OrgId)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrLEI")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/Id/OrgId/LEI")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("LEIIdentifier")
                .maxLength(20)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Debtor LEI")
                .build());
        
        // 2.758 Othr - Other (child of OrgId)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrOthr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/Id/OrgId/Othr")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("GenericOrganisationIdentification1")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Debtor Other")
                .build());
        
        // 2.759 Id - Identification (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrOthrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/Id/OrgId/Othr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Debtor Other Identification")
                .build());
        
        // 2.760 SchmeNm - Scheme Name (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrSchmeNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/Id/OrgId/Othr/SchmeNm")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("OrganisationIdentificationSchemeName1Choice")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Debtor Scheme Name")
                .build());
        
        // 2.762 Prtry - Proprietary (child of SchmeNm)
        addField(FieldDefinition.builder()
                .fieldName("UltmtDbtrPrtry")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtDbtr/Id/OrgId/Othr/SchmeNm/Prtry")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Debtor Proprietary")
                .build());
    }
    
    /**
     * Initialize debtor fields from the provided specification
     */
    private void initializeDebtorFields() {
        // 2.854 Dbtr - Debtor (Mandatory)
        addField(FieldDefinition.builder()
                .fieldName("Dbtr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("PartyIdentification135")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Debtor")
                .build());
        
        // 2.855 Nm - Name (child of Dbtr)
        addField(FieldDefinition.builder()
                .fieldName("DbtrNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/Nm")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max140Text")
                .maxLength(140)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Debtor Name")
                .build());
        
        // 2.856 PstlAdr - Postal Address (child of Dbtr)
        addField(FieldDefinition.builder()
                .fieldName("DbtrPstlAdr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/PstlAdr")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("PostalAddress24")
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Debtor Postal Address")
                .build());
        
        // 2.865 StrtNm - Street Name (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("DbtrStrtNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/PstlAdr/StrtNm")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max70Text")
                .maxLength(70)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Debtor Street Name")
                .build());
        
        // 2.866 BldgNb - Building Number (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("DbtrBldgNb")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/PstlAdr/BldgNb")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("Max16Text")
                .maxLength(16)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Debtor Building Number")
                .build());
    }
    
    /**
     * Initialize additional debtor fields from the provided specification
     */
    private void initializeAdditionalDebtorFields() {
        // 2.871 PstCd - Post Code (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("DbtrPstCd")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/PstlAdr/PstCd")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max16Text")
                .maxLength(16)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Debtor Post Code")
                .build());
        
        // 2.872 TwnNm - Town Name (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("DbtrTwnNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/PstlAdr/TwnNm")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Debtor Town Name")
                .build());
        
        // 2.875 CtrySubDvsn - Country Sub Division (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("DbtrCtrySubDvsn")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/PstlAdr/CtrySubDvsn")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Debtor Country Sub Division")
                .build());
        
        // 2.876 Ctry - Country (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("DbtrCtry")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/PstlAdr/Ctry")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("CountryCode")
                .maxLength(2)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Debtor Country")
                .build());
        
        // 2.877 AdrLine - Address Line (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAdrLine")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/PstlAdr/AdrLine")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("Max70Text")
                .maxLength(70)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Debtor Address Line")
                .build());
        
        // 2.878 Id - Identification (child of Dbtr)
        addField(FieldDefinition.builder()
                .fieldName("DbtrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/Id")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("Party38Choice")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Debtor Identification")
                .build());
        
        // 2.879 OrgId - Organisation Identification (child of Id)
        addField(FieldDefinition.builder()
                .fieldName("DbtrOrgId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/Id/OrgId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("OrganisationIdentification29")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Debtor Organisation Identification")
                .build());
        
        // 2.881 LEI - LEI (child of OrgId)
        addField(FieldDefinition.builder()
                .fieldName("DbtrLEI")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/Id/OrgId/LEI")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("LEIIdentifier")
                .maxLength(20)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Debtor LEI")
                .build());
        
        // 2.888 PrvtId - Private Identification (child of Id)
        addField(FieldDefinition.builder()
                .fieldName("DbtrPrvtId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/Id/PrvtId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("PersonIdentification13")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Debtor Private Identification")
                .build());
        
        // 2.889 DtAndPlcOfBirth - Date And Place Of Birth (child of PrvtId)
        addField(FieldDefinition.builder()
                .fieldName("DbtrDtAndPlcOfBirth")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/Id/PrvtId/DtAndPlcOfBirth")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("DateAndPlaceOfBirth1")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Debtor Date And Place Of Birth")
                .build());
        
        // 2.890 BirthDt - Birth Date (child of DtAndPlcOfBirth)
        addField(FieldDefinition.builder()
                .fieldName("DbtrBirthDt")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/Id/PrvtId/DtAndPlcOfBirth/BirthDt")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ISODate")
                .maxLength(10)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Debtor Birth Date")
                .build());
        
        // 2.892 CityOfBirth - City Of Birth (child of DtAndPlcOfBirth)
        addField(FieldDefinition.builder()
                .fieldName("DbtrCityOfBirth")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/Id/PrvtId/DtAndPlcOfBirth/CityOfBirth")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Debtor City Of Birth")
                .build());
        
        // 2.893 CtryOfBirth - Country Of Birth (child of DtAndPlcOfBirth)
        addField(FieldDefinition.builder()
                .fieldName("DbtrCtryOfBirth")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr/Id/PrvtId/DtAndPlcOfBirth/CtryOfBirth")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("CountryCode")
                .maxLength(2)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Debtor Country Of Birth")
                .build());
        
        // 2.916 DbtrAcct - Debtor Account (Mandatory)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAcct")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAcct")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("CashAccount38")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Debtor Account")
                .build());
        
        // 2.917 Id - Identification (child of DbtrAcct)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAcctId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAcct/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("AccountIdentification4Choice")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Debtor Account Identification")
                .build());
        
        // 2.918 IBAN - IBAN (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAcctIBAN")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAcct/Id/IBAN")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("IBAN2007Identifier")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Debtor Account IBAN")
                .build());
        
        // 2.919 Othr - Other (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAcctOthr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAcct/Id/Othr")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("GenericAccountIdentification1")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Debtor Account Other")
                .build());
        
        // 2.920 Id - Identification (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAcctOthrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAcct/Id/Othr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max34Text")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Debtor Account Other Identification")
                .build());
        
        // 2.929 Nm - Name (child of DbtrAcct)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAcctNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAcct/Nm")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("Max70Text")
                .maxLength(70)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Debtor Account Name")
                .build());
        
        // 2.935 DbtrAgt - Debtor Agent (Mandatory)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAgt")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAgt")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("BranchAndFinancialInstitutionIdentification6")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Debtor Agent")
                .build());
        
        // 2.936 FinInstnId - Financial Institution Identification (child of DbtrAgt)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAgtFinInstnId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAgt/FinInstnId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("FinancialInstitutionIdentification18")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Debtor Agent Financial Institution Identification")
                .build());
        
        // 2.937 BICFI - BICFI (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAgtBICFI")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAgt/FinInstnId/BICFI")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("BICFIDec2014Identifier")
                .maxLength(11)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Debtor Agent BICFI")
                .build());
        
        // 2.938 ClrSysMmbId - Clearing System Member Identification (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAgtClrSysMmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAgt/FinInstnId/ClrSysMmbId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("ClearingSystemMemberIdentification2")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Debtor Agent Clearing System Member Identification")
                .build());
        
        // 2.942 MmbId - Member Identification (child of ClrSysMmbId)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAgtMmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAgt/FinInstnId/ClrSysMmbId/MmbId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(9)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Debtor Agent Member Identification")
                .build());
    }
    
    /**
     * Initialize creditor and agent account fields from the provided specification
     */
    private void initializeCreditorAndAgentAccountFields() {
        // 2.999 DbtrAgtAcct - Debtor Agent Account (Optional)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAgtAcct")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAgtAcct")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("CashAccount38")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Debtor Agent Account")
                .build());
        
        // 2.1000 Id - Identification (child of DbtrAgtAcct)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAgtAcctId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAgtAcct/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("AccountIdentification4Choice")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Debtor Agent Account Identification")
                .build());
        
        // 2.1001 IBAN - IBAN (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAgtAcctIBAN")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAgtAcct/Id/IBAN")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("IBAN2007Identifier")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Debtor Agent Account IBAN")
                .build());
        
        // 2.1002 Othr - Other (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAgtAcctOthr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAgtAcct/Id/Othr")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("GenericAccountIdentification1")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Debtor Agent Account Other")
                .build());
        
        // 2.1003 Id - Identification (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("DbtrAgtAcctOthrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/DbtrAgtAcct/Id/Othr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max34Text")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Debtor Agent Account Other Identification")
                .build());
        
        // 2.1018 CdtrAgt - Creditor Agent (Mandatory)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAgt")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAgt")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("BranchAndFinancialInstitutionIdentification6")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Creditor Agent")
                .build());
        
        // 2.1019 FinInstnId - Financial Institution Identification (child of CdtrAgt)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAgtFinInstnId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAgt/FinInstnId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("FinancialInstitutionIdentification18")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Creditor Agent Financial Institution Identification")
                .build());
        
        // 2.1020 BICFI - BICFI (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAgtBICFI")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAgt/FinInstnId/BICFI")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("BICFIDec2014Identifier")
                .maxLength(11)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Creditor Agent BICFI")
                .build());
        
        // 2.1021 ClrSysMmbId - Clearing System Member Identification (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAgtClrSysMmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAgt/FinInstnId/ClrSysMmbId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("ClearingSystemMemberIdentification2")
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Creditor Agent Clearing System Member Identification")
                .build());
        
        // 2.1025 MmbId - Member Identification (child of ClrSysMmbId)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAgtMmbId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAgt/FinInstnId/ClrSysMmbId/MmbId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(9)
                .category(FieldDefinition.FieldCategory.AGENT_INFO)
                .description("Creditor Agent Member Identification")
                .build());
        
        // 2.1082 CdtrAgtAcct - Creditor Agent Account (Optional)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAgtAcct")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAgtAcct")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("CashAccount38")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Creditor Agent Account")
                .build());
        
        // 2.1083 Id - Identification (child of CdtrAgtAcct)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAgtAcctId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAgtAcct/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("AccountIdentification4Choice")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Creditor Agent Account Identification")
                .build());
        
        // 2.1084 IBAN - IBAN (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAgtAcctIBAN")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAgtAcct/Id/IBAN")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("IBAN2007Identifier")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Creditor Agent Account IBAN")
                .build());
        
        // 2.1085 Othr - Other (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAgtAcctOthr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAgtAcct/Id/Othr")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("GenericAccountIdentification1")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Creditor Agent Account Other")
                .build());
        
        // 2.1086 Id - Identification (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAgtAcctOthrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAgtAcct/Id/Othr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max34Text")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Creditor Agent Account Other Identification")
                .build());
        
        // 2.1101 Cdtr - Creditor (Mandatory)
        addField(FieldDefinition.builder()
                .fieldName("Cdtr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("PartyIdentification135")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Creditor")
                .build());
        
        // 2.1102 Nm - Name (child of Cdtr)
        addField(FieldDefinition.builder()
                .fieldName("CdtrNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/Nm")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max140Text")
                .maxLength(140)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Creditor Name")
                .build());
        
        // 2.1103 PstlAdr - Postal Address (child of Cdtr)
        addField(FieldDefinition.builder()
                .fieldName("CdtrPstlAdr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/PstlAdr")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("PostalAddress24")
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Creditor Postal Address")
                .build());
        
        // 2.1112 StrtNm - Street Name (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("CdtrStrtNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/PstlAdr/StrtNm")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max70Text")
                .maxLength(70)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Creditor Street Name")
                .build());
        
        // 2.1113 BldgNb - Building Number (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("CdtrBldgNb")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/PstlAdr/BldgNb")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("Max16Text")
                .maxLength(16)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Creditor Building Number")
                .build());
        
        // 2.1118 PstCd - Post Code (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("CdtrPstCd")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/PstlAdr/PstCd")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max16Text")
                .maxLength(16)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Creditor Post Code")
                .build());
    }
    
    /**
     * Initialize additional creditor fields from the provided specification
     */
    private void initializeAdditionalCreditorFields() {
        // 2.1119 TwnNm - Town Name (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("CdtrTwnNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/PstlAdr/TwnNm")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Creditor Town Name")
                .build());
        
        // 2.1122 CtrySubDvsn - Country Sub Division (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("CdtrCtrySubDvsn")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/PstlAdr/CtrySubDvsn")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Creditor Country Sub Division")
                .build());
        
        // 2.1123 Ctry - Country (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("CdtrCtry")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/PstlAdr/Ctry")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("CountryCode")
                .maxLength(2)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Creditor Country")
                .build());
        
        // 2.1124 AdrLine - Address Line (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAdrLine")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/PstlAdr/AdrLine")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("Max70Text")
                .maxLength(70)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Creditor Address Line")
                .build());
        
        // 2.1125 Id - Identification (child of Cdtr)
        addField(FieldDefinition.builder()
                .fieldName("CdtrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/Id")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("Party38Choice")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Creditor Identification")
                .build());
        
        // 2.1126 OrgId - Organisation Identification (child of Id)
        addField(FieldDefinition.builder()
                .fieldName("CdtrOrgId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/Id/OrgId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("OrganisationIdentification29")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Creditor Organisation Identification")
                .build());
        
        // 2.1128 LEI - LEI (child of OrgId)
        addField(FieldDefinition.builder()
                .fieldName("CdtrLEI")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/Id/OrgId/LEI")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("LEIIdentifier")
                .maxLength(20)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Creditor LEI")
                .build());
        
        // 2.1135 PrvtId - Private Identification (child of Id)
        addField(FieldDefinition.builder()
                .fieldName("CdtrPrvtId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/Id/PrvtId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("PersonIdentification13")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Creditor Private Identification")
                .build());
        
        // 2.1136 DtAndPlcOfBirth - Date And Place Of Birth (child of PrvtId)
        addField(FieldDefinition.builder()
                .fieldName("CdtrDtAndPlcOfBirth")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/Id/PrvtId/DtAndPlcOfBirth")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("DateAndPlaceOfBirth1")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Creditor Date And Place Of Birth")
                .build());
        
        // 2.1137 BirthDt - Birth Date (child of DtAndPlcOfBirth)
        addField(FieldDefinition.builder()
                .fieldName("CdtrBirthDt")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/Id/PrvtId/DtAndPlcOfBirth/BirthDt")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ISODate")
                .maxLength(10)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Creditor Birth Date")
                .build());
        
        // 2.1139 CityOfBirth - City Of Birth (child of DtAndPlcOfBirth)
        addField(FieldDefinition.builder()
                .fieldName("CdtrCityOfBirth")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/Id/PrvtId/DtAndPlcOfBirth/CityOfBirth")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Creditor City Of Birth")
                .build());
        
        // 2.1140 CtryOfBirth - Country Of Birth (child of DtAndPlcOfBirth)
        addField(FieldDefinition.builder()
                .fieldName("CdtrCtryOfBirth")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr/Id/PrvtId/DtAndPlcOfBirth/CtryOfBirth")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("CountryCode")
                .maxLength(2)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Creditor Country Of Birth")
                .build());
        
        // 2.1163 CdtrAcct - Creditor Account (Mandatory)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAcct")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAcct")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("CashAccount38")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Creditor Account")
                .build());
        
        // 2.1164 Id - Identification (child of CdtrAcct)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAcctId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAcct/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("AccountIdentification4Choice")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Creditor Account Identification")
                .build());
        
        // 2.1165 IBAN - IBAN (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAcctIBAN")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAcct/Id/IBAN")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("IBAN2007Identifier")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Creditor Account IBAN")
                .build());
        
        // 2.1166 Othr - Other (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAcctOthr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAcct/Id/Othr")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("GenericAccountIdentification1")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Creditor Account Other")
                .build());
        
        // 2.1167 Id - Identification (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAcctOthrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAcct/Id/Othr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max34Text")
                .maxLength(34)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Creditor Account Other Identification")
                .build());
        
        // 2.1176 Nm - Name (child of CdtrAcct)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAcctNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAcct/Nm")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("Max70Text")
                .maxLength(70)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Creditor Account Name")
                .build());
        
        // 2.1177 Prxy - Proxy (child of CdtrAcct)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAcctPrxy")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAcct/Prxy")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("ProxyAccountIdentification1")
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Creditor Account Proxy")
                .build());
        
        // 2.1181 Id - Identification (child of Prxy)
        addField(FieldDefinition.builder()
                .fieldName("CdtrAcctPrxyId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/CdtrAcct/Prxy/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max256Text")
                .maxLength(256)
                .category(FieldDefinition.FieldCategory.ACCOUNT_INFO)
                .description("Creditor Account Proxy Identification")
                .build());
        
        // 2.1182 UltmtCdtr - Ultimate Creditor (Optional)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("PartyIdentification135")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor")
                .build());
        
        // 2.1183 Nm - Name (child of UltmtCdtr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Nm")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max140Text")
                .maxLength(140)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Name")
                .build());
        
        // 2.1184 PstlAdr - Postal Address (child of UltmtCdtr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrPstlAdr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/PstlAdr")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("PostalAddress24")
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Creditor Postal Address")
                .build());
        
        // 2.1193 StrtNm - Street Name (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrStrtNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/PstlAdr/StrtNm")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max70Text")
                .maxLength(70)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Creditor Street Name")
                .build());
        
        // 2.1194 BldgNb - Building Number (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrBldgNb")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/PstlAdr/BldgNb")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("Max16Text")
                .maxLength(16)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Creditor Building Number")
                .build());
        
        // 2.1199 PstCd - Post Code (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrPstCd")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/PstlAdr/PstCd")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max16Text")
                .maxLength(16)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Creditor Post Code")
                .build());
        
        // 2.1200 TwnNm - Town Name (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrTwnNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/PstlAdr/TwnNm")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Creditor Town Name")
                .build());
        
        // 2.1203 CtrySubDvsn - Country Sub Division (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrCtrySubDvsn")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/PstlAdr/CtrySubDvsn")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Creditor Country Sub Division")
                .build());
        
        // 2.1204 Ctry - Country (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrCtry")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/PstlAdr/Ctry")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("CountryCode")
                .maxLength(2)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Creditor Country")
                .build());
        
        // 2.1205 AdrLine - Address Line (child of PstlAdr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrAdrLine")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/PstlAdr/AdrLine")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("Max70Text")
                .maxLength(70)
                .category(FieldDefinition.FieldCategory.ADDRESS_INFO)
                .description("Ultimate Creditor Address Line")
                .build());
    }
    
    /**
     * Initialize ultimate creditor identification fields from the provided specification
     */
    private void initializeUltimateCreditorIdentificationFields() {
        // 2.1206 Id - Identification (child of UltmtCdtr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("Party38Choice")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Identification")
                .build());
        
        // 2.1207 OrgId - Organisation Identification (child of Id)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrOrgId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/OrgId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("OrganisationIdentification29")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Organisation Identification")
                .build());
        
        // 2.1208 AnyBIC - Any BIC (child of OrgId)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrAnyBIC")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/OrgId/AnyBIC")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("AnyBICDec2014Identifier")
                .maxLength(11)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Any BIC")
                .build());
        
        // 2.1209 LEI - LEI (child of OrgId)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrLEI")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/OrgId/LEI")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("LEIIdentifier")
                .maxLength(20)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor LEI")
                .build());
        
        // 2.1210 Othr - Other (child of OrgId)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrOrgOthr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/OrgId/Othr")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("GenericOrganisationIdentification1")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Organisation Other")
                .build());
        
        // 2.1211 Id - Identification (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrOrgOthrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/OrgId/Othr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Organisation Other Identification")
                .build());
        
        // 2.1212 SchmeNm - Scheme Name (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrOrgOthrSchmeNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/OrgId/Othr/SchmeNm")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("OrganisationIdentificationSchemeName1Choice")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Organisation Other Scheme Name")
                .build());
        
        // 2.1214 Prtry - Proprietary (child of SchmeNm)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrOrgOthrSchmeNmPrtry")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/OrgId/Othr/SchmeNm/Prtry")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Organisation Other Scheme Name Proprietary")
                .build());
        
        // 2.1216 PrvtId - Private Identification (child of Id)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrPrvtId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/PrvtId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("PersonIdentification13")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Private Identification")
                .build());
        
        // 2.1217 DtAndPlcOfBirth - Date And Place Of Birth (child of PrvtId)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrDtAndPlcOfBirth")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/PrvtId/DtAndPlcOfBirth")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("DateAndPlaceOfBirth1")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Date And Place Of Birth")
                .build());
        
        // 2.1218 BirthDt - Birth Date (child of DtAndPlcOfBirth)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrBirthDt")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/PrvtId/DtAndPlcOfBirth/BirthDt")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ISODate")
                .maxLength(10)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Birth Date")
                .build());
        
        // 2.1220 CityOfBirth - City Of Birth (child of DtAndPlcOfBirth)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrCityOfBirth")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/PrvtId/DtAndPlcOfBirth/CityOfBirth")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor City Of Birth")
                .build());
        
        // 2.1221 CtryOfBirth - Country Of Birth (child of DtAndPlcOfBirth)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrCtryOfBirth")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/PrvtId/DtAndPlcOfBirth/CtryOfBirth")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("CountryCode")
                .maxLength(2)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Country Of Birth")
                .build());
        
        // 2.1222 Othr - Other (child of PrvtId)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrPrvtOthr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/PrvtId/Othr")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("GenericPersonIdentification1")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Private Other")
                .build());
        
        // 2.1223 Id - Identification (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrPrvtOthrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/PrvtId/Othr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Private Other Identification")
                .build());
        
        // 2.1224 SchmeNm - Scheme Name (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrPrvtOthrSchmeNm")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/PrvtId/Othr/SchmeNm")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("PersonIdentificationSchemeName1Choice")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Private Other Scheme Name")
                .build());
    }
    
    /**
     * Initialize remittance and instruction fields from the provided specification
     */
    private void initializeRemittanceAndInstructionFields() {
        // 2.1226 Prtry - Proprietary (child of SchmeNm)
        addField(FieldDefinition.builder()
                .fieldName("UltmtCdtrPrvtOthrSchmeNmPrtry")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/UltmtCdtr/Id/PrvtId/Othr/SchmeNm/Prtry")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Ultimate Creditor Private Other Scheme Name Proprietary")
                .build());
        
        // 2.1244 InstrForCdtrAgt - Instruction For Creditor Agent (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("InstrForCdtrAgt")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/InstrForCdtrAgt")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("InstructionForCreditorAgent1")
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Instruction For Creditor Agent")
                .build());
        
        // 2.1245 Cd - Code (child of InstrForCdtrAgt)
        addField(FieldDefinition.builder()
                .fieldName("InstrForCdtrAgtCd")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/InstrForCdtrAgt/Cd")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ExternalCreditorAgentInstruction1Code")
                .maxLength(4)
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Instruction For Creditor Agent Code")
                .build());
        
        // 2.1246 InstrInf - Instruction Information (child of InstrForCdtrAgt)
        addField(FieldDefinition.builder()
                .fieldName("InstrForCdtrAgtInstrInf")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/InstrForCdtrAgt/InstrInf")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max140Text")
                .maxLength(140)
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Instruction For Creditor Agent Instruction Information")
                .build());
        
        // 2.1250 Purp - Purpose (Optional)
        addField(FieldDefinition.builder()
                .fieldName("Purp")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Purp")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("Purpose2Choice")
                .category(FieldDefinition.FieldCategory.CREDIT_TRANSFER)
                .description("Purpose")
                .build());
        
        // 2.1251 Cd - Code (child of Purp)
        addField(FieldDefinition.builder()
                .fieldName("PurpCd")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Purp/Cd")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("ExternalPurpose1Code")
                .maxLength(4)
                .category(FieldDefinition.FieldCategory.CREDIT_TRANSFER)
                .description("Purpose Code")
                .build());
        
        // 2.1252 Prtry - Proprietary (child of Purp)
        addField(FieldDefinition.builder()
                .fieldName("PurpPrtry")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Purp/Prtry")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.CREDIT_TRANSFER)
                .description("Purpose Proprietary")
                .build());
        
        // 2.1316 RltdRmtInf - Related Remittance Information (Conditional)
        addField(FieldDefinition.builder()
                .fieldName("RltdRmtInf")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RltdRmtInf")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("RemittanceLocation7")
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Related Remittance Information")
                .build());
        
        // 2.1317 RmtId - Remittance Identification (child of RltdRmtInf)
        addField(FieldDefinition.builder()
                .fieldName("RltdRmtInfRmtId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RltdRmtInf/RmtId")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Related Remittance Information Remittance Identification")
                .build());
        
        // 2.1318 RmtLctnDtls - Remittance Location Details (child of RltdRmtInf)
        addField(FieldDefinition.builder()
                .fieldName("RltdRmtInfRmtLctnDtls")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RltdRmtInf/RmtLctnDtls")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("RemittanceLocationData1")
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Related Remittance Information Remittance Location Details")
                .build());
        
        // 2.1319 Mtd - Method (child of RmtLctnDtls)
        addField(FieldDefinition.builder()
                .fieldName("RltdRmtInfRmtLctnDtlsMtd")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RltdRmtInf/RmtLctnDtls/Mtd")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("RemittanceLocationMethod2Code")
                .maxLength(4)
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Related Remittance Information Remittance Location Details Method")
                .build());
        
        // 2.1320 ElctrncAdr - Electronic Address (child of RmtLctnDtls)
        addField(FieldDefinition.builder()
                .fieldName("RltdRmtInfRmtLctnDtlsElctrncAdr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RltdRmtInf/RmtLctnDtls/ElctrncAdr")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("Max2048Text")
                .maxLength(2048)
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Related Remittance Information Remittance Location Details Electronic Address")
                .build());
        
        // 2.1345 RmtInf - Remittance Information (Optional)
        addField(FieldDefinition.builder()
                .fieldName("RmtInf")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("RemittanceInformation16")
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information")
                .build());
        
        // 2.1346 Ustrd - Unstructured (child of RmtInf)
        addField(FieldDefinition.builder()
                .fieldName("RmtInfUstrd")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf/Ustrd")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("Max140Text")
                .maxLength(140)
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information Unstructured")
                .build());
        
        // 2.1347 Strd - Structured (child of RmtInf)
        addField(FieldDefinition.builder()
                .fieldName("RmtInfStrd")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf/Strd")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("StructuredRemittanceInformation16")
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information Structured")
                .build());
        
        // 2.1348 RfrdDocInf - Referred Document Information (child of Strd)
        addField(FieldDefinition.builder()
                .fieldName("RmtInfStrdRfrdDocInf")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf/Strd/RfrdDocInf")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("ReferredDocumentInformation7")
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information Structured Referred Document Information")
                .build());
        
        // 2.1349 Tp - Type (child of RfrdDocInf)
        addField(FieldDefinition.builder()
                .fieldName("RmtInfStrdRfrdDocInfTp")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf/Strd/RfrdDocInf/Tp")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("ReferredDocumentType4")
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information Structured Referred Document Information Type")
                .build());
        
        // 2.1350 CdOrPrtry - Code Or Proprietary (child of Tp)
        addField(FieldDefinition.builder()
                .fieldName("RmtInfStrdRfrdDocInfTpCdOrPrtry")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf/Strd/RfrdDocInf/Tp/CdOrPrtry")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ReferredDocumentType3Choice")
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information Structured Referred Document Information Type Code Or Proprietary")
                .build());
        
        // 2.1351 Cd - Code (child of CdOrPrtry)
        addField(FieldDefinition.builder()
                .fieldName("RmtInfStrdRfrdDocInfTpCdOrPrtryCd")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf/Strd/RfrdDocInf/Tp/CdOrPrtry/Cd")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("ExternalDocumentType1Code")
                .maxLength(4)
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information Structured Referred Document Information Type Code Or Proprietary Code")
                .build());
        
        // 2.1352 Prtry - Proprietary (child of CdOrPrtry)
        addField(FieldDefinition.builder()
                .fieldName("RmtInfStrdRfrdDocInfTpCdOrPrtryPrtry")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf/Strd/RfrdDocInf/Tp/CdOrPrtry/Prtry")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information Structured Referred Document Information Type Code Or Proprietary Proprietary")
                .build());
        
        // 2.1354 Nb - Number (child of RfrdDocInf)
        addField(FieldDefinition.builder()
                .fieldName("RmtInfStrdRfrdDocInfNb")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf/Strd/RfrdDocInf/Nb")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information Structured Referred Document Information Number")
                .build());
        
        // 2.1355 RltdDt - Related Date (child of RfrdDocInf)
        addField(FieldDefinition.builder()
                .fieldName("RmtInfStrdRfrdDocInfRltdDt")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf/Strd/RfrdDocInf/RltdDt")
                .fieldType(FieldDefinition.FieldType.CONDITIONAL)
                .dataType("ISODate")
                .maxLength(10)
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information Structured Referred Document Information Related Date")
                .build());
        
        // 2.1391 RfrdDocAmt - Referred Document Amount (child of Strd)
        addField(FieldDefinition.builder()
                .fieldName("RmtInfStrdRfrdDocAmt")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf/Strd/RfrdDocAmt")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("RemittanceAmount3")
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information Structured Referred Document Amount")
                .build());
        
        // 2.1394 DscntApldAmt - Discount Applied Amount (child of RfrdDocAmt)
        addField(FieldDefinition.builder()
                .fieldName("RmtInfStrdRfrdDocAmtDscntApldAmt")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf/Strd/RfrdDocAmt/DscntApldAmt")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("DiscountAmountAndType1")
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information Structured Referred Document Amount Discount Applied Amount")
                .build());
        
        // 2.1395 Tp - Type (child of DscntApldAmt)
        addField(FieldDefinition.builder()
                .fieldName("RmtInfStrdRfrdDocAmtDscntApldAmtTp")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf/Strd/RfrdDocAmt/DscntApldAmt/Tp")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("DiscountAmountType1Choice")
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information Structured Referred Document Amount Discount Applied Amount Type")
                .build());
        
        // 2.1397 Prtry - Proprietary (child of Tp)
        addField(FieldDefinition.builder()
                .fieldName("RmtInfStrdRfrdDocAmtDscntApldAmtTpPrtry")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf/Strd/RfrdDocAmt/DscntApldAmt/Tp/Prtry")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(4)
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information Structured Referred Document Amount Discount Applied Amount Type Proprietary")
                .build());
        
        // 2.1398 Amt - Amount (child of DscntApldAmt)
        addField(FieldDefinition.builder()
                .fieldName("RmtInfStrdRfrdDocAmtDscntApldAmtAmt")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf/Strd/RfrdDocAmt/DscntApldAmt/Amt")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ActiveOrHistoricCurrencyAndAmount")
                .maxLength(18)
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information Structured Referred Document Amount Discount Applied Amount Amount")
                .build());
        
        // 2.1399 Ccy - Currency (child of Amt)
        addField(FieldDefinition.builder()
                .fieldName("RmtInfStrdRfrdDocAmtDscntApldAmtAmtCcy")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf/Strd/RfrdDocAmt/DscntApldAmt/Amt/Ccy")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("ActiveOrHistoricCurrencyCode")
                .maxLength(3)
                .category(FieldDefinition.FieldCategory.OTHER)
                .description("Remittance Information Structured Referred Document Amount Discount Applied Amount Amount Currency")
                .build());
    }
    
    /**
     * Initialize invoicer and invoicee fields from the provided specification
     */
    private void initializeInvoicerAndInvoiceeFields() {
        // 2.1423 Invcr - Invoicer (Optional)
        addField(FieldDefinition.builder()
                .fieldName("Invcr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Invcr")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("PartyIdentification135")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Invoicer")
                .build());
        
        // 2.1447 Id - Identification (child of Invcr)
        addField(FieldDefinition.builder()
                .fieldName("InvcrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Invcr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Party38Choice")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Invoicer Identification")
                .build());
        
        // 2.1448 OrgId - Organisation Identification (child of Id)
        addField(FieldDefinition.builder()
                .fieldName("InvcrOrgId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Invcr/Id/OrgId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("OrganisationIdentification29")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Invoicer Organisation Identification")
                .build());
        
        // 2.1451 Othr - Other (child of Id)
        addField(FieldDefinition.builder()
                .fieldName("InvcrOthr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Invcr/Id/Othr")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("GenericOrganisationIdentification1")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Invoicer Other")
                .build());
        
        // 2.1452 Id - Identification (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("InvcrOthrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Invcr/Id/Othr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Invoicer Other Identification")
                .build());
        
        // 2.1485 Invcee - Invoicee (Optional)
        addField(FieldDefinition.builder()
                .fieldName("Invcee")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Invcee")
                .fieldType(FieldDefinition.FieldType.OPTIONAL)
                .dataType("PartyIdentification135")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Invoicee")
                .build());
        
        // 2.1509 Id - Identification (child of Invcee)
        addField(FieldDefinition.builder()
                .fieldName("InvceeId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Invcee/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Party38Choice")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Invoicee Identification")
                .build());
        
        // 2.1510 OrgId - Organisation Identification (child of Id)
        addField(FieldDefinition.builder()
                .fieldName("InvceeOrgId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Invcee/Id/OrgId")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("OrganisationIdentification29")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Invoicee Organisation Identification")
                .build());
        
        // 2.1513 Othr - Other (child of Id)
        addField(FieldDefinition.builder()
                .fieldName("InvceeOthr")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Invcee/Id/Othr")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("GenericOrganisationIdentification1")
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Invoicee Other")
                .build());
        
        // 2.1514 Id - Identification (child of Othr)
        addField(FieldDefinition.builder()
                .fieldName("InvceeOthrId")
                .xpath("/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Invcee/Id/Othr/Id")
                .fieldType(FieldDefinition.FieldType.MANDATORY)
                .dataType("Max35Text")
                .maxLength(35)
                .category(FieldDefinition.FieldCategory.PARTY_IDENTIFICATION)
                .description("Invoicee Other Identification")
                .build());
    }
    
    /**
     * Add a field definition to the registry
     */
    private void addField(FieldDefinition field) {
        fieldDefinitions.put(field.getFieldName(), field);
    }
    
    /**
     * Categorize fields by their type
     */
    private void categorizeFields() {
        mandatoryFields.clear();
        optionalFields.clear();
        conditionalFields.clear();
        
        for (FieldDefinition field : fieldDefinitions.values()) {
            switch (field.getFieldType()) {
                case MANDATORY:
                    mandatoryFields.add(field);
                    break;
                case OPTIONAL:
                    optionalFields.add(field);
                    break;
                case CONDITIONAL:
                    conditionalFields.add(field);
                    break;
            }
        }
    }
    
    /**
     * Get field definition by name
     */
    public FieldDefinition getField(String fieldName) {
        return fieldDefinitions.get(fieldName);
    }
    
    /**
     * Get all field definitions
     */
    public Collection<FieldDefinition> getAllFields() {
        return fieldDefinitions.values();
    }
    
    /**
     * Get mandatory fields
     */
    public List<FieldDefinition> getMandatoryFields() {
        return new ArrayList<>(mandatoryFields);
    }
    
    /**
     * Get optional fields
     */
    public List<FieldDefinition> getOptionalFields() {
        return new ArrayList<>(optionalFields);
    }
    
    /**
     * Get conditional fields
     */
    public List<FieldDefinition> getConditionalFields() {
        return new ArrayList<>(conditionalFields);
    }
    
    /**
     * Get fields by category
     */
    public List<FieldDefinition> getFieldsByCategory(FieldDefinition.FieldCategory category) {
        return fieldDefinitions.values().stream()
                .filter(field -> field.getCategory() == category)
                .collect(Collectors.toList());
    }
    
    /**
     * Get total field count
     */
    public int getTotalFieldCount() {
        return fieldDefinitions.size();
    }
    
    /**
     * Get mandatory field count
     */
    public int getMandatoryFieldCount() {
        return mandatoryFields.size();
    }
    
    /**
     * Get optional field count
     */
    public int getOptionalFieldCount() {
        return optionalFields.size();
    }
    
    /**
     * Get conditional field count
     */
    public int getConditionalFieldCount() {
        return conditionalFields.size();
    }
    
    /**
     * Check if field exists
     */
    public boolean hasField(String fieldName) {
        return fieldDefinitions.containsKey(fieldName);
    }
    
    /**
     * Get all field names
     */
    public Set<String> getAllFieldNames() {
        return fieldDefinitions.keySet();
    }
    
    /**
     * Get field names by type
     */
    public Set<String> getFieldNamesByType(FieldDefinition.FieldType type) {
        return fieldDefinitions.values().stream()
                .filter(field -> field.getFieldType() == type)
                .map(FieldDefinition::getFieldName)
                .collect(Collectors.toSet());
    }
}
