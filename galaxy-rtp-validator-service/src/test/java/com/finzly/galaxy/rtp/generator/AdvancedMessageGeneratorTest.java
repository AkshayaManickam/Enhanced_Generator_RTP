package com.finzly.galaxy.rtp.generator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AdvancedMessageGenerator
 */
@SpringBootTest
public class AdvancedMessageGeneratorTest {
    
    @Autowired
    private Pacs008FieldRegistry fieldRegistry;
    
    @Autowired
    private CombinatorialGenerator combinatorialGenerator;
    
    @Autowired
    private ValueGenerator valueGenerator;
    
    @Test
    public void testFieldRegistryInitialization() {
        assertNotNull(fieldRegistry);
        assertTrue(fieldRegistry.getTotalFieldCount() > 0);
        assertTrue(fieldRegistry.getMandatoryFieldCount() > 0);
        
        System.out.println("Total fields: " + fieldRegistry.getTotalFieldCount());
        System.out.println("Mandatory fields: " + fieldRegistry.getMandatoryFieldCount());
        System.out.println("Optional fields: " + fieldRegistry.getOptionalFieldCount());
        System.out.println("Conditional fields: " + fieldRegistry.getConditionalFieldCount());
    }
    
    @Test
    public void testCombinatorialGeneration() {
        // Test with a small set of fields
        Set<String> selectedFields = Set.of(
            "MsgId", "CreDtTm", "NbOfTxs", "TtlIntrBkSttlmAmt",
            "IntrBkSttlmDt", "SttlmInf", "PmtId", "InstrId"
        );
        
        var combinations = combinatorialGenerator.generateFieldCombinations(selectedFields, 10);
        
        assertNotNull(combinations);
        assertTrue(combinations.size() > 0);
        assertTrue(combinations.size() <= 10);
        
        System.out.println("Generated " + combinations.size() + " combinations");
        combinations.forEach(combination -> {
            System.out.println("- " + combination.getDescription() + 
                             " (Complexity: " + combination.calculateComplexityScore() + ")");
        });
    }
    
    @Test
    public void testValueGeneration() {
        // Test generating values for different field types
        String messageId = (String) valueGenerator.generateValue("MsgId", 1);
        assertNotNull(messageId);
        assertTrue(messageId.length() > 0);
        
        String instructionId = (String) valueGenerator.generateValue("InstrId", 1);
        assertNotNull(instructionId);
        assertTrue(instructionId.length() > 0);
        
        String endToEndId = (String) valueGenerator.generateValue("EndToEndId", 1);
        assertNotNull(endToEndId);
        assertTrue(endToEndId.length() > 0);
        
        System.out.println("Generated values:");
        System.out.println("- MessageId: " + messageId);
        System.out.println("- InstructionId: " + instructionId);
        System.out.println("- EndToEndId: " + endToEndId);
    }
    
    @Test
    public void testFieldValidation() {
        // Test field validation
        FieldDefinition field = fieldRegistry.getField("MsgId");
        assertNotNull(field);
        assertEquals("MsgId", field.getFieldName());
        assertEquals(FieldDefinition.FieldType.MANDATORY, field.getFieldType());
        
        // Test value validation
        String validValue = "MSG123456789";
        String invalidValue = "This is a very long message ID that exceeds the maximum length of 35 characters";
        
        assertTrue(valueGenerator.validateValue(validValue, field));
        assertFalse(valueGenerator.validateValue(invalidValue, field));
        
        System.out.println("Field validation test passed for: " + field.getFieldName());
    }
}
