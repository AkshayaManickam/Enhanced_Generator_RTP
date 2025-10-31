package com.finzly.galaxy.rtp.validator.service;

import com.finzly.galaxy.rtp.validator.dto.XmlCombination;
import com.finzly.galaxy.rtp.validator.dto.XmlGenerationResult;
import com.finzly.galaxy.rtp.validator.model.TagType;
import com.finzly.galaxy.rtp.validator.model.XmlTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class XmlGenerationService {

    @Autowired
    private TagDataService tagDataService;

    public XmlGenerationResult generateXmlCombinations(List<String> selectedIndices) {
        long startTime = System.currentTimeMillis();

        try {
            // Get all tags
            List<XmlTag> allTags = tagDataService.getAllTags();
            
            // Identify selected optional tags
            List<XmlTag> selectedOptionalTags = new ArrayList<>();
            for (String index : selectedIndices) {
                XmlTag tag = findTagByIndex(allTags, index);
                if (tag != null && tag.isOptional()) {
                    selectedOptionalTags.add(tag);
                }
            }

            // Generate combinations (2^n)
            int n = selectedOptionalTags.size();
            int totalCombinations = (int) Math.pow(2, n);
            List<XmlCombination> combinations = new ArrayList<>();

            for (int i = 0; i < totalCombinations; i++) {
                List<XmlTag> includedOptionalTags = new ArrayList<>();
                List<String> includedTagNames = new ArrayList<>();

                // Determine which optional tags to include in this combination
                for (int j = 0; j < n; j++) {
                    if ((i & (1 << j)) != 0) {
                        XmlTag optionalTag = selectedOptionalTags.get(j);
                        includedOptionalTags.add(optionalTag);
                        includedTagNames.add(optionalTag.getXmlTag());
                    }
                }

                // Generate XML for this combination
                String xmlContent = generateXmlForCombination(allTags, includedOptionalTags, selectedIndices);
                
                String description = includedTagNames.isEmpty() 
                    ? "Base message (mandatory tags only)" 
                    : "With: " + String.join(", ", includedTagNames);

                combinations.add(XmlCombination.builder()
                        .combinationNumber(i + 1)
                        .description(description)
                        .includedOptionalTags(includedTagNames)
                        .xmlContent(xmlContent)
                        .build());
            }

            long endTime = System.currentTimeMillis();

            return XmlGenerationResult.builder()
                    .success(true)
                    .message("Successfully generated " + totalCombinations + " XML message combinations")
                    .totalCombinations(totalCombinations)
                    .combinations(combinations)
                    .generationTimeMs(endTime - startTime)
                    .build();

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            return XmlGenerationResult.builder()
                    .success(false)
                    .message("Error generating XML: " + e.getMessage())
                    .totalCombinations(0)
                    .combinations(new ArrayList<>())
                    .generationTimeMs(endTime - startTime)
                    .build();
        }
    }

    private String generateXmlForCombination(List<XmlTag> allTags, List<XmlTag> includedOptionalTags, List<String> selectedIndices) {
        StringBuilder xml = new StringBuilder();
        
        // Debug: Print which tags should be highlighted
        System.out.println("=== Generating XML for combination ===");
        System.out.println("Included optional tags in this combination:");
        for (XmlTag tag : includedOptionalTags) {
            System.out.println("  - " + tag.getXmlTag() + " (index: " + tag.getIndex() + ")");
        }
        
        // XML Header
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        
        // Generate content recursively
        for (XmlTag rootTag : allTags) {
            generateTagXml(xml, rootTag, includedOptionalTags, selectedIndices, 1);
        }
        
        xml.append("</Document>");
        
        return xml.toString();
    }

    private void generateTagXml(StringBuilder xml, XmlTag tag, List<XmlTag> includedOptionalTags, 
                                 List<String> selectedIndices, int indentLevel) {
        boolean shouldInclude = false;
        boolean isIncludedOptional = false;

        // Include if mandatory
        if (tag.isMandatory()) {
            shouldInclude = true;
        }
        // Include if optional and in this specific combination
        else if (tag.isOptional()) {
            // Check if this tag is in the included optional tags for THIS combination by comparing indices
            boolean inCombination = includedOptionalTags.stream()
                .anyMatch(t -> t.getIndex().equals(tag.getIndex()));
            
            if (inCombination) {
                shouldInclude = true;
                isIncludedOptional = true; // Mark this as an included optional tag
                System.out.println("✓ Marking optional tag for highlighting: " + tag.getXmlTag() + " (index: " + tag.getIndex() + ")");
            }
        }
        // Skip conditional tags
        else if (tag.isConditional()) {
            shouldInclude = false;
        }

        if (shouldInclude) {
            String indent = "  ".repeat(indentLevel);
            
            // Check if tag has children
            if (tag.getChildren() != null && !tag.getChildren().isEmpty()) {
                // Mark entire tag (opening to closing) if it's an included optional tag
                if (isIncludedOptional) {
                    xml.append(indent).append("<!--OPTIONAL_START--><").append(tag.getXmlTag()).append(">\n");
                } else {
                    xml.append(indent).append("<").append(tag.getXmlTag()).append(">\n");
                }
                
                // Process children (they will NOT be marked as optional even if parent is)
                for (XmlTag child : tag.getChildren()) {
                    generateTagXml(xml, child, includedOptionalTags, selectedIndices, indentLevel + 1);
                }
                
                // Close the highlight after closing tag if it's optional
                if (isIncludedOptional) {
                    xml.append(indent).append("</").append(tag.getXmlTag()).append("><!--OPTIONAL_END-->\n");
                } else {
                    xml.append(indent).append("</").append(tag.getXmlTag()).append(">\n");
                }
            } else {
                // Leaf node - generate sample value
                String sampleValue = generateSampleValue(tag);
                if (isIncludedOptional) {
                    // Highlight entire tag from opening to closing
                    xml.append(indent).append("<!--OPTIONAL_START--><").append(tag.getXmlTag()).append(">")
                       .append(sampleValue)
                       .append("</").append(tag.getXmlTag()).append("><!--OPTIONAL_END-->\n");
                } else {
                    xml.append(indent).append("<").append(tag.getXmlTag()).append(">")
                       .append(sampleValue)
                       .append("</").append(tag.getXmlTag()).append(">\n");
                }
            }
        }
    }

    private String generateSampleValue(XmlTag tag) {
        String tagName = tag.getXmlTag().toUpperCase();
        
        // Generate contextual sample data based on ISO 20022 specifications
        
        // MsgId - Message Identification (Index 1.1)
        // Format: MYYYYMMDDbbbbbbbbbbbbBAAAnnnnnnnnnnn (35 chars)
        if (tagName.equals("MSGID")) {
            return generateMessageIdentification();
        }
        // CreDtTm - Creation Date Time (Index 1.2)
        // Format: YYYY-MM-DDThh:mm:ss (19 chars, Eastern Time)
        else if (tagName.equals("CREDTTM")) {
            return generateCreationDateTime();
        }
        // NbOfTxs - Number Of Transactions (Index 1.4)
        // Must always be '1' for RTP pacs.008 messages
        else if (tagName.equals("NBOFTXS")) {
            return "1";
        }
        // TtlIntrBkSttlmAmt & IntrBkSttlmAmt - Settlement Amounts (Index 1.6, 2.19)
        // Format: Decimal with max 2 fractional digits, max 18 total digits
        // Must be greater than zero, validated against limits
        // TtlIntrBkSttlmAmt must equal IntrBkSttlmAmt
        else if (tagName.equals("TTLINTRBKSTTLMAMT") || tagName.equals("INTRBKSTTLMAMT")) {
            return generateSettlementAmount();
        }
        // Ccy - Currency (Index 1.7, 2.20)
        // Only USD supported by RTP
        // Validated with code '650' if not valid
        else if (tagName.equals("CCY")) {
            return "USD";
        }
        // IntrBkSttlmDt - Interbank Settlement Date (Index 1.8)
        // Format: YYYY-MM-DD (10 chars)
        else if (tagName.equals("INTRBKSTTLMDT")) {
            return generateSettlementDate();
        }
        // SttlmMtd - Settlement Method (Index 1.10)
        // Only 'CLRG' allowed for RTP
        else if (tagName.equals("STTLMMTD")) {
            return "CLRG";
        }
        // Cd - Clearing System Code (Index 1.31)
        // Only 'TCH' allowed for RTP (The Clearing House)
        else if (tagName.equals("CD") && tag.getIndex() != null && tag.getIndex().equals("1.31")) {
            return "TCH";
        }
        // ClrSysRef - Clearing System Reference (Index 2.6)
        // Unique reference assigned by a clearing system (max 35 chars, optional)
        else if (tagName.equals("CLRSYSREF")) {
            return generateClearingSystemReference();
        }
        // Cd - Service Level Code (Index 2.10.1)
        // Only 'SDVA' allowed for RTP (Same Day Value)
        // Mandatory ISO field required for message type, but not used by RTP
        else if (tagName.equals("CD") && tag.getIndex() != null && tag.getIndex().startsWith("2.10")) {
            return "SDVA";
        }
        // Prtry - Proprietary Local Instrument Code (Index 2.15)
        // Identifies special use or arrangement that applies to the RTP message
        // Permitted codes: STANDARD, INDIRECT DOMESTIC, INTERMEDIARY, IXB, OLD, ZELLE
        else if (tagName.equals("PRTRY") && tag.getIndex() != null && tag.getIndex().equals("2.15")) {
            return "STANDARD";
        }
        // Prtry - Proprietary Category Purpose Code (Index 2.18)
        // Identifies Debtor/Sender as business or consumer customer of the Debtor FI
        // Permitted codes: BUSINESS, CONSUMER
        else if (tagName.equals("PRTRY") && tag.getIndex() != null && tag.getIndex().equals("2.18")) {
            return "CONSUMER";
        }
        // ChrgBr - Charge Bearer (Index 2.36)
        // Only 'SLEV' allowed for RTP (Following Service Level - no charging for RTP)
        // Mandatory field, reject with code '650' if not valid
        else if (tagName.equals("CHRGBR")) {
            return "SLEV";
        }
        // InstrId - Instruction Identification (Index 2.2)
        // Format: YYYYMMDDbbbbbbbbbbbbBAAAAnnnnnnnnnnn (35 chars)
        else if (tagName.equals("INSTRID")) {
            return generateInstructionIdentification();
        }
        // EndToEndId - End To End Identification (Index 2.3)
        // Customer reference for the transaction (max 35 chars)
        else if (tagName.equals("ENDTOENDID")) {
            return generateEndToEndIdentification();
        }
        // TxId - Transaction Identification (Index 2.4)
        // Format: YYYYMMDDbbbbbbbbbbbbBAAAAnnnnnnnnnnn (35 chars)
        // Should equal InstrId when Credit Transfer is first message in transaction event
        else if (tagName.equals("TXID")) {
            return generateTransactionIdentification();
        }
        // UETR - Universally Unique Identifier (Index 2.5)
        // UUID format for end-to-end reference (optional, 36 chars)
        else if (tagName.equals("UETR")) {
            return generateUETR();
        }
        // General ID fields (not MsgId, InstrId, EndToEndId, TxId, UETR)
        else if (tagName.contains("ID")) {
            return "ID" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } 
        // Date/Time fields (general)
        else if (tagName.contains("DT") || tagName.contains("TM")) {
            return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        } 
        // Amount fields (general - not settlement amounts)
        else if (tagName.contains("AMT")) {
            return "1000.00";
        } 
        // Currency fields (general - already handled CCY specifically above)
        else if (tagName.contains("CCY")) {
            return "USD";
        } 
        // Name fields
        else if (tagName.contains("NM") || tagName.equals("NM")) {
            return "Sample Name";
        } 
        // BIC/SWIFT codes (Financial Institution Identification)
        // Used in tags like FinInstnId for identifying financial institutions
        // Applied to: Index 2.106 (PrvsInstgAgt1), Index 2.189 (PrvsInstgAgt2), 
        //             Index 2.272 (PrvsInstgAgt3), and other FI identifications
        else if (tagName.contains("BICFI") || tagName.equals("BICFI")) {
            return generateBICFI();
        }
        // BIC (general)
        else if (tagName.contains("BIC")) {
            return "BANKUS33XXX";
        }
        // MmbId - Member Identification (Index 2.111, 2.194, 2.275+)
        // Routing and Transit number (9 digits)
        // Only allowed when Local Instrument is OLO, INDIRECT DOMESTIC, or IXB
        // Applies to all Previous Instructing Agents (1, 2, 3)
        else if (tagName.equals("MMBID")) {
            return generateMemberIdentification();
        } 
        // IBAN - International Bank Account Number (Index 2.170, 2.253+)
        // Only allowed when Local Instrument is OLO or IXB
        // Format: Country Code (2) + Check digits (2) + BBAN (up to 30)
        // Applies to all agent accounts
        else if (tagName.equals("IBAN")) {
            return generateIBAN();
        } 
        // Country code
        else if (tagName.contains("CTRY")) {
            return "US";
        } 
        // Postal code
        else if (tagName.contains("PSTLCD") || tagName.contains("PSTCD")) {
            return "10001";
        } 
        // Generic code fields (max 4 chars) - but not the specific CD for clearing system
        else if (tagName.contains("CD") && tag.getLength() != null && tag.getLength() <= 4) {
            return "SALA";
        } 
        // Default fallback
        else {
            return "Sample" + tag.getXmlTag();
        }
    }
    
    /**
     * Generate Message Identification according to ISO 20022 pacs.008 specification
     * Format: MYYYYMMDDbbbbbbbbbbbbBAAAnnnnnnnnnnn (35 characters)
     * 
     * Position breakdown:
     * 01-01: Prefix 'M'
     * 02-09: File creation date (YYYYMMDD)
     * 10-20: Participant ID (11 characters)
     * 21-21: Message generation source ('B' if generated by Participant)
     * 22-24: Discretionary bank field (3 digit alphanumeric)
     * 25-35: Message serial number (11 numeric characters)
     */
    private String generateMessageIdentification() {
        StringBuilder msgId = new StringBuilder();
        
        // Position 1: Prefix 'M'
        msgId.append("M");
        
        // Position 2-9: File creation date (YYYYMMDD)
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        msgId.append(dateStr);
        
        // Position 10-20: Participant ID (11 characters) - using sample participant ID
        msgId.append("FINZLYUS001"); // 11 characters
        
        // Position 21: Message generation source ('B' for Bank/Participant)
        msgId.append("B");
        
        // Position 22-24: Discretionary bank field (3 alphanumeric characters)
        msgId.append("RTP"); // 3 characters - identifies RTP system
        
        // Position 25-35: Message serial number (11 numeric characters)
        // Generate unique serial number using timestamp + random
        long timestamp = System.currentTimeMillis() % 100000000000L; // Last 11 digits
        String serialNumber = String.format("%011d", timestamp);
        msgId.append(serialNumber);
        
        return msgId.toString(); // Total: 35 characters
    }
    
    /**
     * Generate Creation Date Time according to ISO 20022 pacs.008 specification (Index 1.2)
     * Format: YYYY-MM-DDThh:mm:ss (19 characters)
     * 
     * Requirements:
     * - Must be in Eastern Time (ET)
     * - Must be within 1 calendar day of the system's date/time
     * - Represents when the pacs.008 message itself was created
     * - Reject with code 'DT04' if validation fails
     * 
     * Example: 2017-11-12T10:05:00
     */
    private String generateCreationDateTime() {
        // Get current time in Eastern Time (America/New_York)
        ZonedDateTime etTime = ZonedDateTime.now(ZoneId.of("America/New_York"));
        
        // Format: YYYY-MM-DDThh:mm:ss (19 characters, no milliseconds, no timezone)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        
        return etTime.format(formatter);
    }
    
    /**
     * Generate Settlement Amount according to ISO 20022 pacs.008 specification (Index 1.6, 2.19)
     * 
     * Requirements:
     * - Maximum 2 decimal (fractional) digits allowed
     * - Maximum 18 total digits (including decimal digits)
     * - Must be greater than zero
     * - Currency must be USD (handled separately)
     * - TtlIntrBkSttlmAmt (Index 1.6) must equal IntrBkSttlmAmt (Index 2.19)
     * 
     * Validation Rules (Index 2.19):
     * - Global Transaction Limit (SITL)
     * - Payment Type Limit (STL)
     * - Net Position must not exceed Prefunded Balance
     * 
     * Reject Codes:
     * - '650': Structural validation failure
     * - 'AM02': Global Transaction Limit breach (SITL)
     * - 'AM13': Payment Type Limit breach (STL)
     * - 'AM04': Net Position breaches Prefunded Balance
     * - 'AM12': TtlIntrBkSttlmAmt does not equal IntrBkSttlmAmt
     * 
     * Example: 525.25
     */
    private String generateSettlementAmount() {
        // Generate a random amount between 0.01 and 999999.99
        // Keeping it reasonable for testing (6 digits before decimal + 2 after)
        double minAmount = 0.01;
        double maxAmount = 999999.99;
        
        double randomAmount = minAmount + (Math.random() * (maxAmount - minAmount));
        
        // Use BigDecimal for precise decimal handling
        BigDecimal amount = BigDecimal.valueOf(randomAmount);
        
        // Round to 2 decimal places as per specification
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        
        // Return as string with exactly 2 decimal places
        return amount.toPlainString();
    }
    
    /**
     * Generate Interbank Settlement Date according to ISO 20022 pacs.008 specification (Index 1.8)
     * Format: YYYY-MM-DD (10 characters)
     * 
     * Requirements:
     * - Must be a valid reconciliation window date
     * - Date must be the date of the Reconciliation Window within which the transaction is processed
     * - If provided date from Debtor FI doesn't match actual Reconciliation Window date, 
     *   RTP will replace it with the system's Reconciliation Window date
     * - Reject with code '650' if invalid date structure
     * 
     * Note: For sample generation, we use today's date as the reconciliation window date
     * 
     * Example: 2017-11-12
     */
    private String generateSettlementDate() {
        // Use today's date as the reconciliation window date
        LocalDateTime now = LocalDateTime.now();
        
        // Format: YYYY-MM-DD (10 characters)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        return now.format(formatter);
    }
    
    /**
     * Generate Instruction Identification according to ISO 20022 pacs.008 specification (Index 2.2)
     * Format: YYYYMMDDbbbbbbbbbbbbBAAAAnnnnnnnnnnn (35 characters)
     * 
     * Position breakdown:
     * 01-08: File creation date (YYYYMMDD)
     * 09-19: Participant ID (11 characters)
     * 20-20: Message generation source ('B' if generated by Participant)
     * 21-24: Discretionary bank field (4 digit alphanumeric)
     * 25-35: Message serial number (11 numeric characters)
     * 
     * Requirements:
     * - First 20 characters validated for structural alignment
     * - Embedded date must be within 1 calendar day of system date
     * - Participant ID must be owned by the Instructing Agent
     * - Unique identifier persisted in transactional database
     * - Used for duplicate checking
     * 
     * Reject codes:
     * - DUPL: Instruction ID matches previously completed transaction
     * - DT04: Embedded date not within 1 calendar day
     * - DS0H: Participant ID not owned by Instructing Agent
     * - '650': Structural validation failure
     * 
     * Example: 20171112021200201018STRF00000000011
     */
    private String generateInstructionIdentification() {
        StringBuilder instrId = new StringBuilder();
        
        // Position 1-8: File creation date (YYYYMMDD) - NO PREFIX like MsgId
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        instrId.append(dateStr);
        
        // Position 9-19: Participant ID (11 characters)
        instrId.append("FINZLYUS001"); // 11 characters
        
        // Position 20: Message generation source ('B' for Bank/Participant)
        instrId.append("B");
        
        // Position 21-24: Discretionary bank field (4 alphanumeric characters)
        instrId.append("RTPI"); // 4 characters - RTP Instruction identifier
        
        // Position 25-35: Message serial number (11 numeric characters)
        // Generate unique serial number using timestamp + random
        long timestamp = System.currentTimeMillis() % 100000000000L; // Last 11 digits
        String serialNumber = String.format("%011d", timestamp);
        instrId.append(serialNumber);
        
        return instrId.toString(); // Total: 35 characters
    }
    
    /**
     * Generate End To End Identification according to ISO 20022 pacs.008 specification (Index 2.3)
     * 
     * This is the customer reference assigned to the transaction by the initiating party.
     * It must be passed on throughout the entire payment chain without being changed.
     * 
     * Requirements:
     * - Maximum 35 characters
     * - Assigned by the initiating party (customer)
     * - Used for reconciliation or linking tasks
     * - If user doesn't provide a reference, TCH recommends 'NOREF'
     * 
     * Example: E2E-Ref001
     */
    private String generateEndToEndIdentification() {
        // Generate a customer reference format: E2E-<timestamp>
        long timestamp = System.currentTimeMillis() % 1000000000L; // 9 digits
        return String.format("E2E-REF%09d", timestamp);
    }
    
    /**
     * Generate Transaction Identification according to ISO 20022 pacs.008 specification (Index 2.4)
     * Format: YYYYMMDDbbbbbbbbbbbbBAAAAnnnnnnnnnnn (35 characters)
     * 
     * Requirements:
     * - Used to reference the first message within a transaction event
     * - When Credit Transfer is the first message in a transaction event, 
     *   TxId should equal InstrId
     * - When responding to a Request for Payment (pain.013), TxId must carry 
     *   the Payment Information Identification from the original pain.013
     * 
     * For sample generation, we make TxId equal to InstrId since we're generating
     * a standalone Credit Transfer (not a response to pain.013)
     * 
     * Example: 20171112021200201018SRFP00000000013
     */
    private String generateTransactionIdentification() {
        // For a standalone Credit Transfer (first message in transaction event),
        // TxId should equal InstrId
        return generateInstructionIdentification();
    }
    
    /**
     * Generate UETR (Universally Unique Identifier) according to ISO 20022 pacs.008 specification (Index 2.5)
     * 
     * UUID v4 format for end-to-end reference of a payment transaction.
     * This is optional and used to link RTP messages to services/arrangements outside RTP network.
     * 
     * Format: xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
     * Where:
     * - x is any hexadecimal digit (0-9, a-f)
     * - y is one of 8, 9, a, or b
     * - The '4' indicates UUID version 4
     * 
     * Requirements:
     * - Must follow UUID v4 specification (RFC 4122)
     * - 36 characters including hyphens
     * - Should be passed on throughout entire payment chain unchanged
     * 
     * Example: eb6305c9-1f7f-49de-aed0-16487c27b42d
     */
    private String generateUETR() {
        // Generate a UUID v4
        UUID uuid = UUID.randomUUID();
        
        // Convert to lowercase string (as per example format)
        return uuid.toString().toLowerCase();
    }
    
    /**
     * Generate Clearing System Reference according to ISO 20022 pacs.008 specification (Index 2.6)
     * 
     * Unique reference assigned by a clearing system to unambiguously identify the instruction.
     * This is an optional field.
     * 
     * Requirements:
     * - Maximum 35 characters
     * - Occurrence: [0..1] (Optional)
     * - Used to reference the instruction in a clearing system
     * 
     * Example: CLR-REF-20171112-001
     */
    private String generateClearingSystemReference() {
        // Generate a clearing system reference format: CLR-<date>-<sequence>
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequence = System.currentTimeMillis() % 1000000L; // 6 digits
        return String.format("CLR-REF-%s-%06d", dateStr, sequence);
    }
    
    /**
     * Generate BICFI (BIC Financial Institution) according to ISO 20022 pacs.008 specification (Index 2.106)
     * 
     * Code allocated to a financial institution by the ISO 9362 Registration Authority.
     * BIC = Banking telecommunication messages - Business identifier code.
     * 
     * Requirements:
     * - Length: 8 or 11 contiguous characters
     * - RegEx: [A-Z0-9]{4,4}[A-Z]{2,2}[A-Z0-9]{2,2}[A-Z0-9]{3,3}{0,1}
     * - Only allowed when Local Instrument is "OLO" or "IXB"
     * - Rejected with code '650' if used with INTERMEDIARY, INDIRECT DOMESTIC, STANDARD, or ZELLE
     * 
     * Format:
     * - Position 1-4: Institution code (4 alphanumeric)
     * - Position 5-6: Country code (2 alpha)
     * - Position 7-8: Location code (2 alphanumeric)
     * - Position 9-11: Branch code (3 alphanumeric, optional)
     * 
     * Example: CHASUS33
     */
    private String generateBICFI() {
        // Generate a sample BIC code (8 characters)
        // CHAS = Institution (Chase Bank example)
        // US = Country (United States)
        // 33 = Location code
        return "CHASUS33";
    }
    
    /**
     * Generate Member Identification according to ISO 20022 pacs.008 specification (Index 2.111)
     * 
     * Identification of the Routing and Transit number of the Intermediary FI 
     * between the Debtor FI and the RTP Sending Participant.
     * 
     * Requirements:
     * - Length: 9 characters (Routing and Transit number)
     * - Only allowed when Local Instrument is "OLO", "INDIRECT DOMESTIC", or "IXB"
     * - Rejected with code '650' if provided and Local Instrument is not OLO, INDIRECT DOMESTIC, or IXB
     * 
     * Format: 9-digit US Routing Transit Number (RTN)
     * 
     * Example: 021000021
     */
    private String generateMemberIdentification() {
        // Generate a sample 9-digit Routing and Transit Number
        // Using a well-known routing number format: 021000021 (JP Morgan Chase)
        return "021000021";
    }
    
    /**
     * Generate IBAN (International Bank Account Number) according to ISO 20022 pacs.008 specification (Index 2.170)
     * 
     * International Bank Account Number (IBAN) - identifier used internationally by financial institutions
     * to uniquely identify the account of a customer.
     * 
     * Requirements:
     * - Length: Maximum 34 characters
     * - RegEx: [A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}
     * - Format: Country Code (2 letters) + Check digits (2 numbers) + BBAN (up to 30 alphanumeric)
     * - Only allowed when Local Instrument is "OLO" or "IXB"
     * - Rejected with code '650' if used with INTERMEDIARY, INDIRECT DOMESTIC, STANDARD, or ZELLE
     * 
     * Format breakdown:
     * - Position 1-2: ISO 3166-1 alpha-2 country code (2 letters)
     * - Position 3-4: Check digits (2 numbers)
     * - Position 5-34: Basic Bank Account Number (BBAN) - country specific (up to 30 characters)
     * 
     * Standard: ISO 13616 "Banking and related financial services - International Bank Account Number (IBAN)"
     * 
     * Example: AT611904300234573201
     */
    private String generateIBAN() {
        // Generate a sample IBAN for Austria (AT)
        // AT = Country code (Austria)
        // 61 = Check digits
        // 1904300234573201 = BBAN (Bank Code + Account Number)
        return "AT611904300234573201";
    }

    private XmlTag findTagByIndex(List<XmlTag> tags, String index) {
        for (XmlTag tag : tags) {
            if (tag.getIndex().equals(index)) {
                return tag;
            }
            XmlTag found = findTagByIndex(tag.getChildren(), index);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}

