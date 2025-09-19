package com.finzly.galaxy.rtp.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates all possible combinations of fields for pacs.008 messages
 * Implements combinatorial logic to maximize test coverage
 */
@Component
@Slf4j
public class CombinatorialGenerator {
    
    @Autowired
    private Pacs008FieldRegistry fieldRegistry;
    
    /**
     * Generate all possible field combinations based on selected fields
     * 
     * @param selectedFields Set of field names selected by user
     * @param maxCombinations Maximum number of combinations to generate
     * @return List of field combinations
     */
    public List<FieldCombination> generateFieldCombinations(Set<String> selectedFields, int maxCombinations) {
        log.info("Generating field combinations for {} selected fields with max {} combinations", 
                selectedFields.size(), maxCombinations);
        
        // Always include mandatory fields
        Set<String> mandatoryFields = fieldRegistry.getMandatoryFields().stream()
                .map(FieldDefinition::getFieldName)
                .collect(Collectors.toSet());
        
        // Get optional fields from selected fields
        Set<String> optionalFields = selectedFields.stream()
                .filter(fieldName -> {
                    FieldDefinition field = fieldRegistry.getField(fieldName);
                    return field != null && field.getFieldType() == FieldDefinition.FieldType.OPTIONAL;
                })
                .collect(Collectors.toSet());
        
        // Get conditional fields from selected fields
        Set<String> conditionalFields = selectedFields.stream()
                .filter(fieldName -> {
                    FieldDefinition field = fieldRegistry.getField(fieldName);
                    return field != null && field.getFieldType() == FieldDefinition.FieldType.CONDITIONAL;
                })
                .collect(Collectors.toSet());
        
        log.info("Field breakdown: {} mandatory, {} optional, {} conditional", 
                mandatoryFields.size(), optionalFields.size(), conditionalFields.size());
        
        // Generate combinations
        List<FieldCombination> combinations = new ArrayList<>();
        
        // Strategy 1: Generate all possible combinations of optional fields
        combinations.addAll(generateOptionalFieldCombinations(mandatoryFields, optionalFields, maxCombinations / 2));
        
        // Strategy 2: Generate combinations with conditional fields
        combinations.addAll(generateConditionalFieldCombinations(mandatoryFields, optionalFields, conditionalFields, maxCombinations / 2));
        
        // Strategy 3: Generate edge cases and special combinations
        combinations.addAll(generateSpecialCombinations(mandatoryFields, optionalFields, conditionalFields, maxCombinations / 4));
        
        // Sort by complexity and priority
        combinations.sort((c1, c2) -> {
            int complexityCompare = Integer.compare(c1.calculateComplexityScore(), c2.calculateComplexityScore());
            if (complexityCompare != 0) return complexityCompare;
            return Integer.compare(c1.getPriority(), c2.getPriority());
        });
        
        // Limit to maxCombinations
        if (combinations.size() > maxCombinations) {
            combinations = combinations.subList(0, maxCombinations);
        }
        
        log.info("Generated {} field combinations", combinations.size());
        return combinations;
    }
    
    /**
     * Generate combinations focusing on optional fields
     */
    private List<FieldCombination> generateOptionalFieldCombinations(Set<String> mandatoryFields, 
                                                                    Set<String> optionalFields, 
                                                                    int maxCombinations) {
        List<FieldCombination> combinations = new ArrayList<>();
        
        if (optionalFields.isEmpty()) {
            // Only mandatory fields
            combinations.add(createCombination(mandatoryFields, Collections.emptySet(), 1, "Mandatory fields only"));
            return combinations;
        }
        
        // Generate all possible subsets of optional fields
        List<Set<String>> optionalSubsets = generateSubsets(optionalFields);
        
        int count = 0;
        for (Set<String> optionalSubset : optionalSubsets) {
            if (count >= maxCombinations) break;
            
            Set<String> includedFields = new HashSet<>(mandatoryFields);
            includedFields.addAll(optionalSubset);
            
            Set<String> excludedFields = new HashSet<>(optionalFields);
            excludedFields.removeAll(optionalSubset);
            
            String description = String.format("Mandatory + %d optional fields", optionalSubset.size());
            combinations.add(createCombination(includedFields, excludedFields, count + 1, description));
            count++;
        }
        
        return combinations;
    }
    
    /**
     * Generate combinations including conditional fields
     */
    private List<FieldCombination> generateConditionalFieldCombinations(Set<String> mandatoryFields,
                                                                       Set<String> optionalFields,
                                                                       Set<String> conditionalFields,
                                                                       int maxCombinations) {
        List<FieldCombination> combinations = new ArrayList<>();
        
        if (conditionalFields.isEmpty()) {
            return combinations;
        }
        
        // Generate combinations with different conditional field scenarios
        List<Set<String>> conditionalSubsets = generateSubsets(conditionalFields);
        List<Set<String>> optionalSubsets = generateSubsets(optionalFields);
        
        int count = 0;
        for (Set<String> conditionalSubset : conditionalSubsets) {
            if (count >= maxCombinations) break;
            
            for (Set<String> optionalSubset : optionalSubsets) {
                if (count >= maxCombinations) break;
                
                Set<String> includedFields = new HashSet<>(mandatoryFields);
                includedFields.addAll(optionalSubset);
                includedFields.addAll(conditionalSubset);
                
                Set<String> excludedFields = new HashSet<>(optionalFields);
                excludedFields.removeAll(optionalSubset);
                excludedFields.addAll(conditionalFields);
                excludedFields.removeAll(conditionalSubset);
                
                String description = String.format("Mandatory + %d optional + %d conditional fields", 
                        optionalSubset.size(), conditionalSubset.size());
                combinations.add(createCombination(includedFields, excludedFields, count + 1, description));
                count++;
            }
        }
        
        return combinations;
    }
    
    /**
     * Generate special combinations for edge cases and testing scenarios
     */
    private List<FieldCombination> generateSpecialCombinations(Set<String> mandatoryFields,
                                                              Set<String> optionalFields,
                                                              Set<String> conditionalFields,
                                                              int maxCombinations) {
        List<FieldCombination> combinations = new ArrayList<>();
        
        // Special combination 1: Minimal message (only mandatory fields)
        combinations.add(createCombination(mandatoryFields, 
                new HashSet<>(optionalFields), 1, "Minimal message (mandatory only)"));
        
        // Special combination 2: Maximum message (all selected fields)
        Set<String> allFields = new HashSet<>(mandatoryFields);
        allFields.addAll(optionalFields);
        allFields.addAll(conditionalFields);
        combinations.add(createCombination(allFields, Collections.emptySet(), 2, "Maximum message (all fields)"));
        
        // Special combination 3: Address variations
        Set<String> addressFields = optionalFields.stream()
                .filter(field -> field.contains("Address") || field.contains("PstlAdr"))
                .collect(Collectors.toSet());
        if (!addressFields.isEmpty()) {
            Set<String> withAddress = new HashSet<>(mandatoryFields);
            withAddress.addAll(addressFields);
            Set<String> withoutAddress = new HashSet<>(optionalFields);
            withoutAddress.removeAll(addressFields);
            combinations.add(createCombination(withAddress, withoutAddress, 3, "With address fields"));
        }
        
        // Special combination 4: Agent variations
        Set<String> agentFields = optionalFields.stream()
                .filter(field -> field.contains("Agent") || field.contains("Agt"))
                .collect(Collectors.toSet());
        if (!agentFields.isEmpty()) {
            Set<String> withAgent = new HashSet<>(mandatoryFields);
            withAgent.addAll(agentFields);
            Set<String> withoutAgent = new HashSet<>(optionalFields);
            withoutAgent.removeAll(agentFields);
            combinations.add(createCombination(withAgent, withoutAgent, 4, "With agent fields"));
        }
        
        // Special combination 5: Account variations
        Set<String> accountFields = optionalFields.stream()
                .filter(field -> field.contains("Account") || field.contains("Acct"))
                .collect(Collectors.toSet());
        if (!accountFields.isEmpty()) {
            Set<String> withAccount = new HashSet<>(mandatoryFields);
            withAccount.addAll(accountFields);
            Set<String> withoutAccount = new HashSet<>(optionalFields);
            withoutAccount.removeAll(accountFields);
            combinations.add(createCombination(withAccount, withoutAccount, 5, "With account fields"));
        }
        
        return combinations;
    }
    
    /**
     * Generate all possible subsets of a set
     */
    private List<Set<String>> generateSubsets(Set<String> inputSet) {
        List<Set<String>> subsets = new ArrayList<>();
        List<String> elements = new ArrayList<>(inputSet);
        
        // Generate all possible combinations using bit manipulation
        int n = elements.size();
        for (int i = 0; i < (1 << n); i++) {
            Set<String> subset = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) > 0) {
                    subset.add(elements.get(j));
                }
            }
            subsets.add(subset);
        }
        
        return subsets;
    }
    
    /**
     * Create a field combination with the given parameters
     */
    private FieldCombination createCombination(Set<String> includedFields, 
                                             Set<String> excludedFields, 
                                             int priority, 
                                             String description) {
        return FieldCombination.builder()
                .combinationId("COMB_" + System.currentTimeMillis() + "_" + priority)
                .includedFields(new HashSet<>(includedFields))
                .excludedFields(new HashSet<>(excludedFields))
                .fieldValues(new HashMap<>())
                .priority(priority)
                .description(description)
                .valid(true)
                .validationErrors(new ArrayList<>())
                .tags(new HashSet<>())
                .complexityScore(0)
                .build();
    }
    
    /**
     * Generate combinations for specific field categories
     */
    public List<FieldCombination> generateCategoryCombinations(FieldDefinition.FieldCategory category, int maxCombinations) {
        List<FieldDefinition> categoryFields = fieldRegistry.getFieldsByCategory(category);
        Set<String> fieldNames = categoryFields.stream()
                .map(FieldDefinition::getFieldName)
                .collect(Collectors.toSet());
        
        return generateFieldCombinations(fieldNames, maxCombinations);
    }
    
    /**
     * Generate combinations with specific complexity levels
     */
    public List<FieldCombination> generateComplexityCombinations(Set<String> selectedFields, 
                                                               int minComplexity, 
                                                               int maxComplexity, 
                                                               int maxCombinations) {
        List<FieldCombination> allCombinations = generateFieldCombinations(selectedFields, maxCombinations * 2);
        
        return allCombinations.stream()
                .filter(combination -> {
                    int complexity = combination.calculateComplexityScore();
                    return complexity >= minComplexity && complexity <= maxComplexity;
                })
                .limit(maxCombinations)
                .collect(Collectors.toList());
    }
    
    /**
     * Get statistics about generated combinations
     */
    public CombinationStatistics getCombinationStatistics(List<FieldCombination> combinations) {
        if (combinations.isEmpty()) {
            return CombinationStatistics.builder().build();
        }
        
        int totalCombinations = combinations.size();
        int minComplexity = combinations.stream().mapToInt(FieldCombination::calculateComplexityScore).min().orElse(0);
        int maxComplexity = combinations.stream().mapToInt(FieldCombination::calculateComplexityScore).max().orElse(0);
        double avgComplexity = combinations.stream().mapToInt(FieldCombination::calculateComplexityScore).average().orElse(0.0);
        
        Map<String, Long> fieldFrequency = combinations.stream()
                .flatMap(combination -> combination.getIncludedFields().stream())
                .collect(Collectors.groupingBy(field -> field, Collectors.counting()));
        
        return CombinationStatistics.builder()
                .totalCombinations(totalCombinations)
                .minComplexity(minComplexity)
                .maxComplexity(maxComplexity)
                .avgComplexity(avgComplexity)
                .fieldFrequency(fieldFrequency)
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CombinationStatistics {
        private int totalCombinations;
        private int minComplexity;
        private int maxComplexity;
        private double avgComplexity;
        private Map<String, Long> fieldFrequency;
    }
}
