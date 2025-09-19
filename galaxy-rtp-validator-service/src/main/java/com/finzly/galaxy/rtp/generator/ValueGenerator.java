package com.finzly.galaxy.rtp.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * Generates unique, compliant values for pacs.008 fields
 * Ensures XSD compliance and uniqueness across generated messages
 */
@Component
@Slf4j
public class ValueGenerator {
    
    @Autowired
    private Pacs008FieldRegistry fieldRegistry;
    
    // Counters for unique value generation
    private final AtomicLong messageCounter = new AtomicLong(1);
    private final AtomicLong instructionCounter = new AtomicLong(1);
    private final AtomicLong endToEndCounter = new AtomicLong(1);
    private final AtomicLong transactionCounter = new AtomicLong(1);
    
    // Value pools for different field types
    private final List<String> names = Arrays.asList(
            "John Doe", "Jane Smith", "Robert Johnson", "Emily Davis", "Michael Wilson",
            "Sarah Brown", "David Miller", "Lisa Garcia", "James Martinez", "Jennifer Anderson",
            "ABC Corporation", "XYZ Industries", "Global Solutions Inc", "Tech Innovations LLC",
            "Financial Services Corp", "Business Enterprises Ltd", "Digital Systems Inc"
    );
    
    private final List<String> streetNames = Arrays.asList(
            "123 Main St", "456 Oak Ave", "789 Pine Rd", "321 Elm St", "654 Maple Dr",
            "987 Cedar Ln", "147 Birch Way", "258 Spruce St", "369 Willow Ave", "741 Poplar Rd",
            "852 Ash St", "963 Hickory Ln", "159 Cherry Dr", "357 Walnut Ave", "468 Chestnut St"
    );
    
    private final List<String> cities = Arrays.asList(
            "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia",
            "San Antonio", "San Diego", "Dallas", "San Jose", "Austin", "Jacksonville",
            "Fort Worth", "Columbus", "Charlotte", "San Francisco", "Indianapolis", "Seattle"
    );
    
    private final List<String> states = Arrays.asList(
            "NY", "CA", "IL", "TX", "AZ", "PA", "FL", "OH", "NC", "GA", "MI", "NJ", "VA", "WA", "MA"
    );
    
    private final List<String> zipCodes = Arrays.asList(
            "10001", "90210", "60601", "77001", "85001", "19101", "78201", "92101", "75201", "95101",
            "73301", "32201", "76101", "43201", "28201", "94101", "46201", "98101", "02101", "30301"
    );
    
    private final List<String> bicfis = Arrays.asList(
            "BOFAUS3N", "CHASUS33", "WELLUS33", "CITIUS33", "JPMOUS33", "GOLDUS33", "MORGUS33",
            "DEUTUS33", "UBSWUS33", "HSBCUS33", "BARCUS33", "CREDUS33", "BNPPUS33", "SOCIUS33"
    );
    
    private final List<String> serviceLevels = Arrays.asList("SDVA");
    private final List<String> localInstruments = Arrays.asList("STANDARD", "ZELLE", "IXB", "OLO", "INTERMEDIARY", "INDIRECT DOMESTIC", "FOREIGN AFFILIATE");
    private final List<String> categoryPurposes = Arrays.asList("CONSUMER", "BUSINESS");
    private final List<String> chargeBearers = Arrays.asList("SLEV");
    private final List<String> currencies = Arrays.asList("USD");
    private final List<String> clearingSystems = Arrays.asList("TCH");
    
    // Amount ranges for different scenarios
    private final List<BigDecimal> amountRanges = Arrays.asList(
            new BigDecimal("10.00"), new BigDecimal("100.00"), new BigDecimal("1000.00"),
            new BigDecimal("5000.00"), new BigDecimal("10000.00"), new BigDecimal("50000.00"),
            new BigDecimal("100000.00"), new BigDecimal("500000.00"), new BigDecimal("1000000.00")
    );
    
    /**
     * Generate a unique value for the specified field
     */
    public Object generateValue(String fieldName, int messageIndex) {
        FieldDefinition field = fieldRegistry.getField(fieldName);
        if (field == null) {
            log.warn("Field definition not found for: {}", fieldName);
            return generateDefaultValue(fieldName);
        }
        
        return generateValue(field, messageIndex);
    }
    
    /**
     * Generate a unique value for the specified field definition
     */
    public Object generateValue(FieldDefinition field, int messageIndex) {
        String fieldName = field.getFieldName();
        String dataType = field.getDataType();
        
        try {
            switch (fieldName) {
                // Message Identification Fields
                case "MsgId":
                    return generateMessageId(messageIndex);
                case "InstrId":
                    return generateInstructionId(messageIndex);
                case "EndToEndId":
                    return generateEndToEndId(messageIndex);
                case "TxId":
                    return generateTransactionId(messageIndex);
                
                // Date/Time Fields
                case "CreDtTm":
                    return generateCreationDateTime(messageIndex);
                case "IntrBkSttlmDt":
                    return generateSettlementDate(messageIndex);
                
                // Amount Fields
                case "TtlIntrBkSttlmAmt":
                case "IntrBkSttlmAmt":
                    return generateAmount(messageIndex);
                case "TtlIntrBkSttlmAmtCcy":
                case "IntrBkSttlmAmtCcy":
                    return generateCurrency(messageIndex);
                
                // Name Fields
                case "DbtrNm":
                case "CdtrNm":
                    return generateName(messageIndex);
                case "DbtrAcctNm":
                case "CdtrAcctNm":
                    return generateAccountName(messageIndex);
                
                // Address Fields
                case "DbtrStrtNm":
                case "CdtrStrtNm":
                    return generateStreetName(messageIndex);
                case "DbtrPstCd":
                case "CdtrPstCd":
                    return generateZipCode(messageIndex);
                case "DbtrTwnNm":
                case "CdtrTwnNm":
                    return generateCity(messageIndex);
                case "DbtrCtrySubDvsn":
                case "CdtrCtrySubDvsn":
                    return generateState(messageIndex);
                case "DbtrCtry":
                case "CdtrCtry":
                    return "US"; // Default to US
                
                // Account Fields
                case "DbtrAcctIban":
                case "CdtrAcctIban":
                    return generateIBAN(messageIndex);
                
                // BICFI Fields
                case "InstgAgtBICFI":
                case "InstdAgtBICFI":
                case "DbtrAgtBICFI":
                case "CdtrAgtBICFI":
                    return generateBICFI(messageIndex);
                
                // Enumeration Fields
                case "SvcLvlCd":
                    return generateServiceLevel(messageIndex);
                case "LclInstrmPrtry":
                    return generateLocalInstrument(messageIndex);
                case "CtgyPurpPrtry":
                    return generateCategoryPurpose(messageIndex);
                case "ChrgBr":
                    return generateChargeBearer(messageIndex);
                case "SttlmMtd":
                    return "CLRG"; // Default settlement method
                case "ClrSysCd":
                    return generateClearingSystem(messageIndex);
                
                // Numeric Fields
                case "NbOfTxs":
                    return "1"; // Default to 1 transaction
                
                default:
                    return generateDefaultValue(fieldName, dataType, messageIndex);
            }
        } catch (Exception e) {
            log.error("Error generating value for field {}: {}", fieldName, e.getMessage());
            return generateDefaultValue(fieldName);
        }
    }
    
    /**
     * Generate multiple unique values for a field
     */
    public List<Object> generateMultipleValues(String fieldName, int count) {
        List<Object> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            values.add(generateValue(fieldName, i + 1));
        }
        return values;
    }
    
    // Specific value generation methods
    
    private String generateMessageId(int messageIndex) {
        return String.format("MSG%d%03d", System.currentTimeMillis() % 1000000, messageIndex);
    }
    
    private String generateInstructionId(int messageIndex) {
        return String.format("INSTR%d%03d", System.currentTimeMillis() % 1000000, messageIndex);
    }
    
    private String generateEndToEndId(int messageIndex) {
        return String.format("E2E%d%03d", System.currentTimeMillis() % 1000000, messageIndex);
    }
    
    private String generateTransactionId(int messageIndex) {
        return String.format("TX%d%03d", System.currentTimeMillis() % 1000000, messageIndex);
    }
    
    private LocalDateTime generateCreationDateTime(int messageIndex) {
        return LocalDateTime.now().plusMinutes(messageIndex);
    }
    
    private LocalDate generateSettlementDate(int messageIndex) {
        return LocalDate.now().plusDays(messageIndex % 30);
    }
    
    private BigDecimal generateAmount(int messageIndex) {
        BigDecimal baseAmount = amountRanges.get(messageIndex % amountRanges.size());
        BigDecimal increment = new BigDecimal(messageIndex).setScale(2, BigDecimal.ROUND_HALF_UP);
        return baseAmount.add(increment).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    private String generateCurrency(int messageIndex) {
        // Try to get currency from field registry first
        FieldDefinition field = fieldRegistry.getField("TtlIntrBkSttlmAmtCcy");
        if (field != null && field.getAllowedValues() != null && !field.getAllowedValues().isEmpty()) {
            return field.getAllowedValues().get(messageIndex % field.getAllowedValues().size());
        }
        return currencies.get(messageIndex % currencies.size());
    }
    
    private String generateName(int messageIndex) {
        return names.get(messageIndex % names.size());
    }
    
    private String generateAccountName(int messageIndex) {
        String baseName = names.get(messageIndex % names.size());
        return baseName + " Account";
    }
    
    private String generateStreetName(int messageIndex) {
        return streetNames.get(messageIndex % streetNames.size());
    }
    
    private String generateZipCode(int messageIndex) {
        return zipCodes.get(messageIndex % zipCodes.size());
    }
    
    private String generateCity(int messageIndex) {
        return cities.get(messageIndex % cities.size());
    }
    
    private String generateState(int messageIndex) {
        return states.get(messageIndex % states.size());
    }
    
    private String generateIBAN(int messageIndex) {
        return String.format("US%02d%010d%010d", 
                (messageIndex % 99) + 1, 
                System.currentTimeMillis() % 10000000000L,
                messageIndex * 1000000000L % 10000000000L);
    }
    
    private String generateBICFI(int messageIndex) {
        return bicfis.get(messageIndex % bicfis.size());
    }
    
    private String generateServiceLevel(int messageIndex) {
        FieldDefinition field = fieldRegistry.getField("SvcLvlCd");
        if (field != null && field.getAllowedValues() != null && !field.getAllowedValues().isEmpty()) {
            return field.getAllowedValues().get(messageIndex % field.getAllowedValues().size());
        }
        return serviceLevels.get(messageIndex % serviceLevels.size());
    }
    
    private String generateLocalInstrument(int messageIndex) {
        FieldDefinition field = fieldRegistry.getField("LclInstrmPrtry");
        if (field != null && field.getAllowedValues() != null && !field.getAllowedValues().isEmpty()) {
            return field.getAllowedValues().get(messageIndex % field.getAllowedValues().size());
        }
        return localInstruments.get(messageIndex % localInstruments.size());
    }
    
    private String generateCategoryPurpose(int messageIndex) {
        FieldDefinition field = fieldRegistry.getField("CtgyPurpPrtry");
        if (field != null && field.getAllowedValues() != null && !field.getAllowedValues().isEmpty()) {
            return field.getAllowedValues().get(messageIndex % field.getAllowedValues().size());
        }
        return categoryPurposes.get(messageIndex % categoryPurposes.size());
    }
    
    private String generateChargeBearer(int messageIndex) {
        FieldDefinition field = fieldRegistry.getField("ChrgBr");
        if (field != null && field.getAllowedValues() != null && !field.getAllowedValues().isEmpty()) {
            return field.getAllowedValues().get(messageIndex % field.getAllowedValues().size());
        }
        return chargeBearers.get(messageIndex % chargeBearers.size());
    }
    
    private String generateClearingSystem(int messageIndex) {
        FieldDefinition field = fieldRegistry.getField("ClrSysCd");
        if (field != null && field.getAllowedValues() != null && !field.getAllowedValues().isEmpty()) {
            return field.getAllowedValues().get(messageIndex % field.getAllowedValues().size());
        }
        return clearingSystems.get(messageIndex % clearingSystems.size());
    }
    
    /**
     * Generate default value based on data type
     */
    private Object generateDefaultValue(String fieldName, String dataType, int messageIndex) {
        switch (dataType) {
            case "Max35Text":
                return "DEFAULT_" + fieldName + "_" + messageIndex;
            case "Max140Text":
                return "Default " + fieldName + " Value " + messageIndex;
            case "Max70Text":
                return "Default " + fieldName + " " + messageIndex;
            case "Max16Text":
                return "DEFAULT" + messageIndex;
            case "ISODateTime":
                return LocalDateTime.now().plusMinutes(messageIndex);
            case "ISODate":
                return LocalDate.now().plusDays(messageIndex);
            case "ActiveCurrencyAndAmount":
                return new BigDecimal("1000.00");
            case "ActiveCurrencyCode":
                return "USD";
            case "Max15NumericText":
                return String.valueOf(messageIndex);
            default:
                return "DEFAULT_" + fieldName + "_" + messageIndex;
        }
    }
    
    /**
     * Generate simple default value
     */
    private Object generateDefaultValue(String fieldName) {
        return "DEFAULT_" + fieldName + "_" + System.currentTimeMillis();
    }
    
    /**
     * Validate generated value against field constraints
     */
    public boolean validateValue(Object value, FieldDefinition field) {
        if (value == null) {
            return field.getFieldType() != FieldDefinition.FieldType.MANDATORY;
        }
        
        String stringValue = value.toString();
        
        // Check max length
        if (field.getMaxLength() != null && stringValue.length() > field.getMaxLength()) {
            log.warn("Value {} exceeds max length {} for field {}", stringValue, field.getMaxLength(), field.getFieldName());
            return false;
        }
        
        // Check min length
        if (field.getMinLength() != null && stringValue.length() < field.getMinLength()) {
            log.warn("Value {} below min length {} for field {}", stringValue, field.getMinLength(), field.getFieldName());
            return false;
        }
        
        // Check pattern
        if (field.getPattern() != null) {
            Pattern pattern = Pattern.compile(field.getPattern());
            if (!pattern.matcher(stringValue).matches()) {
                log.warn("Value {} does not match pattern {} for field {}", stringValue, field.getPattern(), field.getFieldName());
                return false;
            }
        }
        
        // Check allowed values
        if (field.getAllowedValues() != null && !field.getAllowedValues().contains(stringValue)) {
            log.warn("Value {} not in allowed values {} for field {}", stringValue, field.getAllowedValues(), field.getFieldName());
            return false;
        }
        
        return true;
    }
    
    /**
     * Generate a value that passes validation
     */
    public Object generateValidValue(FieldDefinition field, int messageIndex) {
        Object value = generateValue(field, messageIndex);
        
        // If validation fails, try to fix the value
        if (!validateValue(value, field)) {
            value = generateCorrectedValue(field, messageIndex);
        }
        
        return value;
    }
    
    /**
     * Generate a corrected value that passes validation
     */
    private Object generateCorrectedValue(FieldDefinition field, int messageIndex) {
        String fieldName = field.getFieldName();
        String dataType = field.getDataType();
        
        // Apply corrections based on constraints
        if (field.getMaxLength() != null) {
            String baseValue = "CORRECTED_" + fieldName + "_" + messageIndex;
            if (baseValue.length() > field.getMaxLength()) {
                baseValue = baseValue.substring(0, field.getMaxLength());
            }
            return baseValue;
        }
        
        if (field.getAllowedValues() != null && !field.getAllowedValues().isEmpty()) {
            return field.getAllowedValues().get(messageIndex % field.getAllowedValues().size());
        }
        
        return generateDefaultValue(fieldName, dataType, messageIndex);
    }
    
    /**
     * Reset counters (useful for testing)
     */
    public void resetCounters() {
        messageCounter.set(1);
        instructionCounter.set(1);
        endToEndCounter.set(1);
        transactionCounter.set(1);
    }
    
    /**
     * Get current counter values
     */
    public Map<String, Long> getCounterValues() {
        Map<String, Long> counters = new HashMap<>();
        counters.put("message", messageCounter.get());
        counters.put("instruction", instructionCounter.get());
        counters.put("endToEnd", endToEndCounter.get());
        counters.put("transaction", transactionCounter.get());
        return counters;
    }
}
