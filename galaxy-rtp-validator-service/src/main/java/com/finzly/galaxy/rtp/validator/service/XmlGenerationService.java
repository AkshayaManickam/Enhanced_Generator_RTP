package com.finzly.galaxy.rtp.validator.service;

import com.finzly.galaxy.rtp.validator.dto.XmlCombination;
import com.finzly.galaxy.rtp.validator.dto.XmlGenerationResult;
import com.finzly.galaxy.rtp.validator.model.XmlTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

@Service
public class XmlGenerationService {

    @Autowired
    private TagDataService tagDataService;

    @Autowired
    private SampleValuesService sampleValuesService;

    @Autowired
    private RtpValueValidationService rtpValueValidationService;

    // Cache for tag lookups to avoid recursive searches
    private Map<String, XmlTag> tagCache;

    // Virtual Thread Executor for parallel processing
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public XmlGenerationResult generateXmlCombinations(List<String> selectedIndices) {
        long startTime = System.currentTimeMillis();

        try {
            // Get all tags
            List<XmlTag> allTags = tagDataService.getAllTags();

            // Build tag cache for O(1) lookups
            tagCache = buildTagCache(allTags);
            System.out.println("Built tag cache with " + tagCache.size() + " entries");

            // Build a hierarchical structure of selected optional tags
            List<OptionalTagNode> optionalTagHierarchy = buildOptionalTagHierarchy(allTags, selectedIndices);

            System.out.println("=== Optional Tag Hierarchy ===");
            for (OptionalTagNode node : optionalTagHierarchy) {
                printNodeHierarchy(node, 0);
            }

            // Generate all combinations considering parent-child relationships
            List<Set<String>> allCombinations = generateNestedCombinations(optionalTagHierarchy);

            System.out.println("\n=== Generated " + allCombinations.size() + " combinations ===");

            // Limit combinations if too many
            int maxCombinations = 5000; // Safety limit
            if (allCombinations.size() > maxCombinations) {
                return XmlGenerationResult.builder()
                        .success(false)
                        .message("Too many combinations (" + allCombinations.size() + "). Maximum allowed is "
                                + maxCombinations + ". Please select fewer optional tags.")
                        .totalCombinations(0)
                        .combinations(new ArrayList<>())
                        .generationTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }

            // Use ConcurrentHashMap for thread-safe collection
            ConcurrentLinkedQueue<XmlCombination> combinations = new ConcurrentLinkedQueue<>();

            // Process combinations in parallel using virtual threads
            List<CompletableFuture<XmlCombination>> futures = new ArrayList<>();
            int combinationNumber = 1;

            for (Set<String> combinationIndices : allCombinations) {
                final int currentCombinationNumber = combinationNumber++;

                CompletableFuture<XmlCombination> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return processSingleCombination(allTags, combinationIndices, selectedIndices,
                                currentCombinationNumber);
                    } catch (Exception e) {
                        System.err.println(
                                "Error processing combination " + currentCombinationNumber + ": " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                }, virtualThreadExecutor);

                futures.add(future);
            }

            // Wait for all combinations to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // Collect results
            for (CompletableFuture<XmlCombination> future : futures) {
                try {
                    XmlCombination combo = future.get();
                    if (combo != null) {
                        combinations.add(combo);
                    }
                } catch (Exception e) {
                    System.err.println("Error retrieving combination: " + e.getMessage());
                }
            }

            // Sort by combination number
            List<XmlCombination> sortedCombinations = new ArrayList<>(combinations);
            sortedCombinations.sort(Comparator.comparingInt(XmlCombination::getCombinationNumber));

            long endTime = System.currentTimeMillis();

            // Clear cache to free memory
            tagCache.clear();

            return XmlGenerationResult.builder()
                    .success(true)
                    .message("Successfully generated " + sortedCombinations.size() + " XML message combinations")
                    .totalCombinations(sortedCombinations.size())
                    .combinations(sortedCombinations)
                    .generationTimeMs(endTime - startTime)
                    .build();

        } catch (OutOfMemoryError e) {
            long endTime = System.currentTimeMillis();
            e.printStackTrace();
            // Clear cache on OOM
            if (tagCache != null) {
                tagCache.clear();
            }
            return XmlGenerationResult.builder()
                    .success(false)
                    .message("Out of memory error. Please select fewer optional tags (current: "
                            + selectedIndices.size() + "). Try selecting 10-15 tags at a time.")
                    .totalCombinations(0)
                    .combinations(new ArrayList<>())
                    .generationTimeMs(endTime - startTime)
                    .build();
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            e.printStackTrace();
            // Clear cache on error
            if (tagCache != null) {
                tagCache.clear();
            }
            return XmlGenerationResult.builder()
                    .success(false)
                    .message("Error generating XML: " + e.getMessage())
                    .totalCombinations(0)
                    .combinations(new ArrayList<>())
                    .generationTimeMs(endTime - startTime)
                    .build();
        }
    }

    /**
     * Build a cache map for O(1) tag lookups
     */
    private Map<String, XmlTag> buildTagCache(List<XmlTag> tags) {
        Map<String, XmlTag> cache = new HashMap<>();
        buildTagCacheRecursive(tags, cache);
        return cache;
    }

    private void buildTagCacheRecursive(List<XmlTag> tags, Map<String, XmlTag> cache) {
        for (XmlTag tag : tags) {
            cache.put(tag.getIndex(), tag);
            if (tag.getChildren() != null && !tag.getChildren().isEmpty()) {
                buildTagCacheRecursive(tag.getChildren(), cache);
            }
        }
    }

    /**
     * Process a single combination
     */
    private XmlCombination processSingleCombination(List<XmlTag> allTags, Set<String> combinationIndices,
            List<String> selectedIndices, int combinationNumber) {
        // Get the actual tags for this combination using cached lookups
        List<XmlTag> includedOptionalTags = new ArrayList<>();
        List<String> includedTagNames = new ArrayList<>();

        for (String index : combinationIndices) {
            XmlTag tag = tagCache.get(index); // O(1) lookup
            if (tag != null) {
                includedOptionalTags.add(tag);
                includedTagNames.add(tag.getXmlTag());
            }
        }

        // Generate XML for this combination
        String xmlContent = generateXmlForCombination(allTags, includedOptionalTags, selectedIndices);

        String description = includedTagNames.isEmpty()
                ? "Base message (mandatory tags only)"
                : "With: " + String.join(", ", includedTagNames);

        return XmlCombination.builder()
                .combinationNumber(combinationNumber)
                .description(description)
                .includedOptionalTags(includedTagNames)
                .xmlContent(xmlContent)
                .build();
    }

    // Helper class to represent optional tag hierarchy
    private static class OptionalTagNode {
        String index;
        String name;
        List<OptionalTagNode> children = new ArrayList<>();

        OptionalTagNode(String index, String name) {
            this.index = index;
            this.name = name;
        }
    }

    /**
     * Build a hierarchical structure of selected optional tags
     * This identifies parent-child relationships among optional tags using the
     * actual tag hierarchy
     */
    private List<OptionalTagNode> buildOptionalTagHierarchy(List<XmlTag> allTags, List<String> selectedIndices) {
        List<OptionalTagNode> rootNodes = new ArrayList<>();
        Map<String, OptionalTagNode> nodeMap = new HashMap<>();
        Map<String, XmlTag> tagMap = new HashMap<>();

        // First pass: Create nodes for all selected optional tags and map them
        for (String index : selectedIndices) {
            XmlTag tag = findTagByIndex(allTags, index);
            if (tag != null && tag.isOptional()) {
                OptionalTagNode node = new OptionalTagNode(index, tag.getXmlTag());
                nodeMap.put(index, node);
                tagMap.put(index, tag);
            }
        }

        // Second pass: Build parent-child relationships using actual tag hierarchy
        // For each selected tag, check if its actual parent (from tag structure) is
        // also selected
        for (Map.Entry<String, OptionalTagNode> entry : nodeMap.entrySet()) {
            String index = entry.getKey();
            OptionalTagNode node = entry.getValue();
            XmlTag tag = tagMap.get(index);

            // Find the actual parent of this tag in the tag hierarchy
            XmlTag parentTag = findParentTag(allTags, tag);

            boolean hasParent = false;
            if (parentTag != null && parentTag.isOptional()) {
                // Check if this parent is in our selected nodes
                OptionalTagNode parentNode = nodeMap.get(parentTag.getIndex());
                if (parentNode != null) {
                    // Parent is selected - add this node as a child
                    parentNode.children.add(node);
                    hasParent = true;
                }
            }

            // If no parent found in selected nodes, it's a root node
            if (!hasParent) {
                rootNodes.add(node);
            }
        }

        return rootNodes;
    }

    /**
     * Find the parent tag of a given tag in the tag hierarchy
     */
    private XmlTag findParentTag(List<XmlTag> allTags, XmlTag targetTag) {
        for (XmlTag tag : allTags) {
            XmlTag parent = findParentTagRecursive(tag, targetTag, null);
            if (parent != null) {
                return parent;
            }
        }
        return null;
    }

    /**
     * Recursively find the parent of a target tag
     */
    private XmlTag findParentTagRecursive(XmlTag currentTag, XmlTag targetTag, XmlTag potentialParent) {
        if (currentTag.getIndex().equals(targetTag.getIndex())) {
            return potentialParent;
        }

        for (XmlTag child : currentTag.getChildren()) {
            XmlTag parent = findParentTagRecursive(child, targetTag, currentTag);
            if (parent != null) {
                return parent;
            }
        }

        return null;
    }

    /**
     * Generate all combinations considering parent-child relationships
     * For each optional parent with optional children:
     * - Include parent with all combinations of children
     * - Exclude parent (and implicitly all children)
     */
    private List<Set<String>> generateNestedCombinations(List<OptionalTagNode> nodes) {
        List<Set<String>> allCombinations = new ArrayList<>();

        // Start with empty combination (base message with mandatory tags only)
        allCombinations.add(new HashSet<>());

        // For each root optional tag
        for (OptionalTagNode node : nodes) {
            List<Set<String>> newCombinations = new ArrayList<>();

            // Get all combinations for this node (with its children)
            List<Set<String>> nodeCombinations = generateNodeCombinations(node);

            System.out.println("\n=== Node: " + node.name + " (" + node.index + ") ===");
            System.out.println("Generated " + nodeCombinations.size() + " combinations:");
            for (Set<String> combo : nodeCombinations) {
                System.out.println("  " + combo);
            }

            // For each existing combination, create new ones
            for (Set<String> existingCombo : allCombinations) {
                // Add combination without this node (keep existing combo as is)
                newCombinations.add(new HashSet<>(existingCombo));

                // Add combinations with this node in various child configurations
                for (Set<String> nodeCombo : nodeCombinations) {
                    Set<String> newCombo = new HashSet<>(existingCombo);
                    newCombo.addAll(nodeCombo);
                    newCombinations.add(newCombo);
                }
            }

            allCombinations = newCombinations;
        }

        return allCombinations;
    }

    /**
     * Generate combinations for a single node and its children - RECURSIVE
     * 
     * KEY INSIGHT: When user selects nested optional tags, the parent should only
     * appear
     * in combinations where at least one of its selected children is also present.
     * 
     * For each node:
     * 1. If no children (leaf node): return just the node
     * 2. If has children:
     * - Node with all combinations of selected children
     * - Do NOT include "parent only" because children were selected by user
     * 
     * Example: UltimateCreditor -> PostalAddress -> [BuildingNo, AddressLine]
     * When user selects all 4 tags, returns 4 combinations from the deepest level:
     * - { UC, PA, BuildingNo } // UC + PA + BuildingNo only
     * - { UC, PA, AddressLine } // UC + PA + AddressLine only
     * - { UC, PA, BuildingNo, AddressLine } // UC + PA + both grandchildren
     * - { UC, PA } // UC + PA only (when PA has no grandchildren)
     * 
     * Note: { UC } alone is NOT included because PA was selected by user
     */
    private List<Set<String>> generateNodeCombinations(OptionalTagNode node) {
        List<Set<String>> combinations = new ArrayList<>();

        if (node.children.isEmpty()) {
            // Leaf node - just include the node itself
            Set<String> combo = new HashSet<>();
            combo.add(node.index);
            combinations.add(combo);
        } else {
            // Node has children - generate combinations recursively

            // Get all child combinations first (each child recursively expands)
            List<List<Set<String>>> childCombinationsList = new ArrayList<>();

            for (OptionalTagNode child : node.children) {
                // Recursively get combinations for this child
                List<Set<String>> childCombos = generateNodeCombinations(child);
                childCombinationsList.add(childCombos);
            }

            // Generate power set of children indices (2^n - 1 combinations, excluding
            // empty)
            // We exclude the empty set (i=0) because if children are selected,
            // at least one must be present
            int n = node.children.size();
            int totalChildCombos = (int) Math.pow(2, n);

            for (int i = 1; i < totalChildCombos; i++) { // Start from 1 to exclude parent-only
                List<Integer> selectedChildIndices = new ArrayList<>();
                for (int j = 0; j < n; j++) {
                    if ((i & (1 << j)) != 0) {
                        selectedChildIndices.add(j);
                    }
                }

                // Generate Cartesian product of selected children's combinations
                List<Set<String>> cartesianProduct = generateCartesianProduct(
                        selectedChildIndices, childCombinationsList, node.index);
                combinations.addAll(cartesianProduct);
            }
        }

        return combinations;
    }

    /**
     * Generate Cartesian product of selected children's combinations
     * 
     * Example: If child0 has combos [{A}, {A,B}] and child1 has combos [{C}, {C,D}]
     * Result: [{parent,A,C}, {parent,A,C,D}, {parent,A,B,C}, {parent,A,B,C,D}]
     */
    private List<Set<String>> generateCartesianProduct(
            List<Integer> selectedChildIndices,
            List<List<Set<String>>> childCombinationsList,
            String parentIndex) {

        List<Set<String>> result = new ArrayList<>();

        // Start with a single empty set containing just the parent
        result.add(new HashSet<>(Collections.singleton(parentIndex)));

        // For each selected child, expand the combinations
        for (Integer childIndex : selectedChildIndices) {
            List<Set<String>> childCombos = childCombinationsList.get(childIndex);
            List<Set<String>> newResult = new ArrayList<>();

            // For each existing combination, combine with each child combo
            for (Set<String> existingCombo : result) {
                for (Set<String> childCombo : childCombos) {
                    Set<String> merged = new HashSet<>(existingCombo);
                    merged.addAll(childCombo);
                    newResult.add(merged);
                }
            }

            result = newResult;
        }

        return result;
    }

    /**
     * Print node hierarchy for debugging
     */
    private void printNodeHierarchy(OptionalTagNode node, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + "- " + node.name + " (" + node.index + ")");
        for (OptionalTagNode child : node.children) {
            printNodeHierarchy(child, depth + 1);
        }
    }

    private String generateXmlForCombination(List<XmlTag> allTags, List<XmlTag> includedOptionalTags,
            List<String> selectedIndices) {
        StringBuilder xml = new StringBuilder();

        // Debug: Print which tags should be highlighted
        System.out.println("=== Generating XML for combination ===");
        System.out.println("Included optional tags in this combination:");
        for (XmlTag tag : includedOptionalTags) {
            System.out.println("  - " + tag.getXmlTag() + " (index: " + tag.getIndex() + ")");
        }

        // XML Header with ct namespace prefix
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append(
                "<Document xmlns:ct=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");

        // Generate content recursively
        for (XmlTag rootTag : allTags) {
            generateTagXml(xml, rootTag, includedOptionalTags, selectedIndices, 1);
        }

        xml.append("</Document>");

        return xml.toString();
    }

    private void generateTagXml(StringBuilder xml, XmlTag tag, List<XmlTag> includedOptionalTags,
            List<String> selectedIndices, int indentLevel) {
        generateTagXml(xml, tag, includedOptionalTags, selectedIndices, indentLevel, null);
    }

    private void generateTagXml(StringBuilder xml, XmlTag tag, List<XmlTag> includedOptionalTags,
            List<String> selectedIndices, int indentLevel, XmlTag parentTag) {
        boolean shouldInclude = false;
        boolean isIncludedOptional = false;

        // Include if mandatory
        if (tag.isMandatory()) {
            shouldInclude = true;
        }
        // Include if optional and in this specific combination
        else if (tag.isOptional()) {
            // Check if this tag is in the included optional tags for THIS combination by
            // comparing indices
            boolean inCombination = includedOptionalTags.stream()
                    .anyMatch(t -> t.getIndex().equals(tag.getIndex()));

            if (inCombination) {
                shouldInclude = true;
                isIncludedOptional = true; // Mark this as an included optional tag
                System.out.println("✓ Marking optional tag for highlighting: " + tag.getXmlTag() + " (index: "
                        + tag.getIndex() + ")");
            }
        }
        // Handle conditional tags - they need special treatment
        // For mandatory parent tags with OR conditions, we must include at least one
        // child
        else if (tag.isConditional()) {
            // Check if parent is mandatory with OR condition - we need to include at least
            // one option
            if (parentTag != null && parentTag.isMandatory() && parentTag.isOrCondition()) {
                // For mandatory parent with OR condition, include the preferred option for
                // standard RTP
                // For standard RTP: Use Othr (not IBAN) for account IDs, ClrSysMmbId (not
                // BICFI) for agent IDs
                String parentTagName = parentTag.getXmlTag().toUpperCase();
                String tagName = tag.getXmlTag().toUpperCase();

                // For account IDs (Id with OR: IBAN or Othr) - always use Othr for standard RTP
                if (parentTagName.equals("ID") && (tagName.equals("OTHR") || tagName.equals("IBAN"))) {
                    // Prefer Othr for standard RTP messages (not IBAN)
                    if (tagName.equals("OTHR")) {
                        shouldInclude = true;
                    }
                }
                // For agent IDs (FinInstnId with OR: BICFI or ClrSysMmbId) - always use
                // ClrSysMmbId for standard RTP
                else if (parentTagName.equals("FININSTNID")
                        && (tagName.equals("CLRSYSMMBID") || tagName.equals("BICFI"))) {
                    // Prefer ClrSysMmbId for standard RTP messages (not BICFI)
                    if (tagName.equals("CLRSYSMMBID")) {
                        shouldInclude = true;
                    }
                }
            }
            // For other conditional tags, skip by default
            // They will be included only if explicitly selected
        }

        if (shouldInclude) {
            String indent = "  ".repeat(indentLevel);

            // Add ct: prefix to all tags
            String tagWithPrefix = "ct:" + tag.getXmlTag();
            String tagName = tag.getXmlTag().toUpperCase();

            // Check if tag has children
            if (tag.getChildren() != null && !tag.getChildren().isEmpty()) {
                // Special handling for TtlIntrBkSttlmAmt and IntrBkSttlmAmt - they should have
                // amount value and Ccy attribute
                if (tagName.equals("TTLINTRBKSTTLMAMT") || tagName.equals("INTRBKSTTLMAMT")) {
                    String amount = generateSampleValue(tag, parentTag);
                    String ccy = "USD";
                    // Try to get Ccy from child or sample values
                    for (XmlTag child : tag.getChildren()) {
                        if (child.getXmlTag().equals("Ccy")) {
                            ccy = generateSampleValue(child, tag);
                            break;
                        }
                    }
                    if (isIncludedOptional) {
                        xml.append(indent).append("<!--OPTIONAL_START--><").append(tagWithPrefix)
                                .append(" Ccy=\"").append(ccy).append("\">").append(amount)
                                .append("</").append(tagWithPrefix).append("><!--OPTIONAL_END-->\n");
                    } else {
                        xml.append(indent).append("<").append(tagWithPrefix)
                                .append(" Ccy=\"").append(ccy).append("\">").append(amount)
                                .append("</").append(tagWithPrefix).append(">\n");
                    }
                } else {
                    // Mark entire tag (opening to closing) if it's an included optional tag
                    if (isIncludedOptional) {
                        xml.append(indent).append("<!--OPTIONAL_START--><").append(tagWithPrefix).append(">\n");
                    } else {
                        xml.append(indent).append("<").append(tagWithPrefix).append(">\n");
                    }

                    // Process children (they will NOT be marked as optional even if parent is)
                    for (XmlTag child : tag.getChildren()) {
                        generateTagXml(xml, child, includedOptionalTags, selectedIndices, indentLevel + 1, tag);
                    }

                    // Close the highlight after closing tag if it's optional
                    if (isIncludedOptional) {
                        xml.append(indent).append("</").append(tagWithPrefix).append("><!--OPTIONAL_END-->\n");
                    } else {
                        xml.append(indent).append("</").append(tagWithPrefix).append(">\n");
                    }
                }
            } else {
                // Leaf node - generate sample value
                String sampleValue = generateSampleValue(tag, parentTag);

                // CRITICAL: Ensure no tag is ever empty - this violates RTP specification
                // According to RTP Message Specification v5.0, all mandatory tags MUST have
                // values
                if (sampleValue == null || sampleValue.trim().isEmpty()) {
                    // Log warning and use fallback
                    System.err
                            .println("WARNING: Empty value for tag " + tag.getXmlTag() + " (index: " + tag.getIndex() +
                                    "). Using fallback value.");
                    sampleValue = generateFallbackValue(tag, parentTag);

                    // Double-check: ensure fallback also provides a value
                    // Note: Container tags (Othr, ClrSysMmbId) may return null - that's OK, they
                    // have children
                    if ((sampleValue == null || sampleValue.trim().isEmpty()) &&
                            tag.getChildren() == null || tag.getChildren().isEmpty()) {
                        // This is a leaf node with no children - must have a value
                        System.err.println("ERROR: Fallback value is also empty for tag " + tag.getXmlTag() +
                                " (index: " + tag.getIndex() + "). Using final default.");
                        sampleValue = generateFinalFallbackValue(tag, parentTag);
                    }
                }

                // CRITICAL: Validate value against RTP specification BEFORE writing to XML
                // This ensures all values comply with RTP format, length, and content
                // requirements
                sampleValue = rtpValueValidationService.validateValue(
                        tag.getXmlTag().toUpperCase(),
                        sampleValue,
                        tag.getIndex());

                if (isIncludedOptional) {
                    // Highlight entire tag from opening to closing
                    xml.append(indent).append("<!--OPTIONAL_START--><").append(tagWithPrefix).append(">")
                            .append(sampleValue)
                            .append("</").append(tagWithPrefix).append("><!--OPTIONAL_END-->\n");
                } else {
                    xml.append(indent).append("<").append(tagWithPrefix).append(">")
                            .append(sampleValue)
                            .append("</").append(tagWithPrefix).append(">\n");
                }
            }
        }
    }

    /**
     * Determine context from parent tag hierarchy (e.g., Dbtr, Cdtr, DbtrAgt,
     * CdtrAgt, etc.)
     */
    private String determineContext(XmlTag tag, XmlTag parentTag) {
        if (parentTag == null) {
            return null;
        }

        String parentTagName = parentTag.getXmlTag().toUpperCase();

        // Check parent tag for context
        if (parentTagName.equals("DBTR")) {
            return "Dbtr";
        } else if (parentTagName.equals("CDTR")) {
            return "Cdtr";
        } else if (parentTagName.equals("DBTRAGT")) {
            return "DbtrAgt";
        } else if (parentTagName.equals("CDTRAGT")) {
            return "CdtrAgt";
        } else if (parentTagName.equals("INSTGAGT")) {
            return "InstgAgt";
        } else if (parentTagName.equals("INSTDAGT")) {
            return "InstdAgt";
        } else if (parentTagName.equals("DBTRACCT")) {
            return "DbtrAcct";
        } else if (parentTagName.equals("CDTRACCT")) {
            return "CdtrAcct";
        } else if (parentTagName.equals("FININSTNID") || parentTagName.equals("CLRSYSMMBID")) {
            // If parent is FinInstnId or ClrSysMmbId, use index-based detection
            // to determine the agent context (InstgAgt, InstdAgt, DbtrAgt, CdtrAgt)
            String index = tag.getIndex();
            if (index != null) {
                // Check for InstgAgt context (Index 2.360)
                if (index.equals("2.360")) {
                    return "InstgAgt";
                }
                // Check for InstdAgt context (Index 2.424)
                else if (index.equals("2.424")) {
                    return "InstdAgt";
                }
                // Check for DbtrAgt context (Index 2.942)
                else if (index.equals("2.942")) {
                    return "DbtrAgt";
                }
                // Check for CdtrAgt context (Index 2.1025)
                else if (index.equals("2.1025")) {
                    return "CdtrAgt";
                }
                // Check for Previous Instructing Agent contexts
                else if (index.startsWith("2.111")) {
                    return "PrvsInstgAgt1";
                } else if (index.startsWith("2.194")) {
                    return "PrvsInstgAgt2";
                } else if (index.startsWith("2.277")) {
                    return "PrvsInstgAgt3";
                }
                // Check for Intermediary Agent contexts
                else if (index.startsWith("2.488")) {
                    return "IntrmyAgt1";
                } else if (index.startsWith("2.571")) {
                    return "IntrmyAgt2";
                } else if (index.startsWith("2.654")) {
                    return "IntrmyAgt3";
                }
                // Fallback: broader index range checks for InstgAgt MmbId (Index 2.353-2.360
                // range)
                if (index.startsWith("2.35") || index.startsWith("2.36")) {
                    return "InstgAgt";
                }
                // Fallback: broader index range checks for InstdAgt MmbId (Index 2.417-2.424
                // range)
                else if (index.startsWith("2.41") || index.startsWith("2.42")) {
                    return "InstdAgt";
                }
                // Fallback: broader index range checks for DbtrAgt MmbId (Index 2.935-2.942
                // range)
                else if (index.startsWith("2.93") || index.startsWith("2.94")) {
                    return "DbtrAgt";
                }
                // Fallback: broader index range checks for CdtrAgt MmbId (Index 2.1018-2.1025
                // range)
                else if (index.startsWith("2.10")) {
                    // Need to distinguish between CdtrAgt (2.10xx) and other 2.10xx indices
                    if (index.startsWith("2.102") || index.startsWith("2.101")) {
                        return "CdtrAgt";
                    }
                }
            }
        }

        // Fallback: use index-based detection for backward compatibility
        String index = tag.getIndex();
        if (index != null) {
            // Debtor context (2.854-2.916 range)
            if (index.startsWith("2.85") || index.startsWith("2.86") || index.startsWith("2.87") ||
                    index.startsWith("2.88") || index.startsWith("2.89") || index.startsWith("2.90") ||
                    index.startsWith("2.91")) {
                return "Dbtr";
            }
            // Creditor context (2.1101-2.1163 range)
            else if (index.startsWith("2.11") || index.startsWith("2.12") || index.startsWith("2.13") ||
                    index.startsWith("2.14") || index.startsWith("2.15") || index.startsWith("2.16")) {
                return "Cdtr";
            }
        }

        return null;
    }

    /**
     * Normalize tag name for JSON lookup (e.g., "ID" -> "Id", "MMBID" -> "MmbId")
     */
    private String normalizeTagNameForLookup(String tagName) {
        if (tagName == null || tagName.isEmpty()) {
            return tagName;
        }
        // Convert to proper case: first letter uppercase, rest lowercase
        // But handle special cases like "ID" -> "Id", "MMBID" -> "MmbId"
        if (tagName.equals("ID")) {
            return "Id";
        } else if (tagName.equals("MMBID")) {
            return "MmbId";
        } else if (tagName.equals("NM")) {
            return "Nm";
        } else if (tagName.equals("PSTCD") || tagName.equals("PSTLCD")) {
            return "PstCd";
        } else if (tagName.equals("STRTNM")) {
            return "StrtNm";
        } else if (tagName.equals("BLDGNB")) {
            return "BldgNb";
        } else if (tagName.equals("TWNNM")) {
            return "TwnNm";
        } else if (tagName.equals("CTRYSUBDVSN")) {
            return "CtrySubDvsn";
        } else if (tagName.equals("BIRTHDT")) {
            return "BirthDt";
        } else if (tagName.equals("CITYOFBIRTH")) {
            return "CityOfBirth";
        } else if (tagName.equals("CTRYOFBIRTH")) {
            return "CtryOfBirth";
        } else if (tagName.length() > 1) {
            return tagName.substring(0, 1).toUpperCase() + tagName.substring(1).toLowerCase();
        }
        return tagName;
    }

    private String generateSampleValue(XmlTag tag, XmlTag parentTag) {
        String tagName = tag.getXmlTag().toUpperCase();
        String normalizedTagName = normalizeTagNameForLookup(tagName);
        String index = tag.getIndex();

        // Determine context from parent tag hierarchy
        String context = determineContext(tag, parentTag);

        // First, try to get value from sample_values.json with context-aware lookup
        String jsonValue = null;

        // Try context-specific keys first (e.g., Dbtr_Nm, Cdtr_StrtNm, DbtrAcct_Id,
        // etc.)
        if (context != null) {
            jsonValue = sampleValuesService.getValueWithContext(normalizedTagName, context, index);
        }

        // If not found with context, try direct lookup with normalized name
        if (jsonValue == null) {
            jsonValue = sampleValuesService.getValue(normalizedTagName, index);
        }

        // If still not found, try with uppercase tag name
        if (jsonValue == null) {
            jsonValue = sampleValuesService.getValue(tagName, index);
        }

        if (jsonValue != null && !jsonValue.trim().isEmpty()) {
            return jsonValue;
        }

        // Generate contextual sample data based on ISO 20022 specifications

        // MsgId - Message Identification (Index 1.1)
        // Format: MYYYYMMDDbbbbbbbbbbbbBAAAnnnnnnnnnnn (35 chars)
        if (tagName.equals("MSGID")) {
            return sampleValuesService.generateMsgId();
        }
        // CreDtTm - Creation Date Time (Index 1.2)
        // Format: YYYY-MM-DDThh:mm:ss (19 chars, Eastern Time)
        else if (tagName.equals("CREDTTM")) {
            String creDtTm = sampleValuesService.getValue("CreDtTm", index);
            return creDtTm != null ? creDtTm : generateCreationDateTime();
        }
        // NbOfTxs - Number Of Transactions (Index 1.4)
        // Must always be '1' for RTP pacs.008 messages
        else if (tagName.equals("NBOFTXS")) {
            String nbOfTxs = sampleValuesService.getValue("NbOfTxs", index);
            return nbOfTxs != null ? nbOfTxs : "1";
        }
        // TtlIntrBkSttlmAmt & IntrBkSttlmAmt - Settlement Amounts (Index 1.6, 2.19)
        // Format: Decimal with max 2 fractional digits, max 18 total digits
        // Must be greater than zero, validated against limits
        // TtlIntrBkSttlmAmt must equal IntrBkSttlmAmt
        else if (tagName.equals("TTLINTRBKSTTLMAMT") || tagName.equals("INTRBKSTTLMAMT")) {
            String amount = sampleValuesService
                    .getValue(tagName.equals("TTLINTRBKSTTLMAMT") ? "TtlIntrBkSttlmAmt" : "IntrBkSttlmAmt", index);
            return amount != null ? amount : generateSettlementAmount();
        }
        // Ccy - Currency (Index 1.7, 2.20)
        // Only USD supported by RTP
        // Validated with code '650' if not valid
        else if (tagName.equals("CCY")) {
            String ccy = sampleValuesService
                    .getValue(tag.getIndex() != null && tag.getIndex().startsWith("1.") ? "TtlIntrBkSttlmAmt_Ccy"
                            : "IntrBkSttlmAmt_Ccy", index);
            return ccy != null ? ccy : "USD";
        }
        // IntrBkSttlmDt - Interbank Settlement Date (Index 1.8)
        // Format: YYYY-MM-DD (10 chars)
        else if (tagName.equals("INTRBKSTTLMDT")) {
            String date = sampleValuesService.getValue("IntrBkSttlmDt", index);
            return date != null ? date : generateSettlementDate();
        }
        // SttlmMtd - Settlement Method (Index 1.10)
        // Only 'CLRG' allowed for RTP
        else if (tagName.equals("STTLMMTD")) {
            String sttlmMtd = sampleValuesService.getValue("SttlmMtd", index);
            return sttlmMtd != null ? sttlmMtd : "CLRG";
        }
        // Cd - Clearing System Code (Index 1.31)
        // Only 'TCH' allowed for RTP (The Clearing House)
        else if (tagName.equals("CD") && tag.getIndex() != null && tag.getIndex().equals("1.31")) {
            String cd = sampleValuesService.getValue("ClrSys_Cd", index);
            return cd != null ? cd : "TCH";
        }
        // ClrSysRef - Clearing System Reference (Index 2.6)
        // Unique reference assigned by a clearing system (max 35 chars, optional)
        else if (tagName.equals("CLRSYSREF")) {
            String clrSysRef = sampleValuesService.getValue("ClrSysRef", index);
            return clrSysRef != null ? clrSysRef : generateClearingSystemReference();
        }
        // Cd - Service Level Code (Index 2.10.1)
        // Only 'SDVA' allowed for RTP (Same Day Value)
        // Mandatory ISO field required for message type, but not used by RTP
        else if (tagName.equals("CD") && tag.getIndex() != null && tag.getIndex().startsWith("2.10")) {
            String svcLvl = sampleValuesService.getValue("SvcLvl_Cd", index);
            if (svcLvl == null || svcLvl.trim().isEmpty()) {
                // Try alternative key format
                svcLvl = sampleValuesService.getValue("CD", index);
            }
            if (svcLvl == null || svcLvl.trim().isEmpty()) {
                svcLvl = "SDVA"; // Default per RTP spec
            }
            return svcLvl;
        }
        // Prtry - Proprietary Local Instrument Code (Index 2.15)
        // Identifies special use or arrangement that applies to the RTP message
        // Permitted codes: STANDARD, INDIRECT DOMESTIC, INTERMEDIARY, IXB, OLD, ZELLE
        else if (tagName.equals("PRTRY") && tag.getIndex() != null && tag.getIndex().equals("2.15")) {
            String prtry = sampleValuesService.getValue("LclInstrm_Prtry", index);
            return prtry != null ? prtry : "STANDARD";
        }
        // Prtry - Proprietary Category Purpose Code (Index 2.18)
        // Identifies Debtor/Sender as business or consumer customer of the Debtor FI
        // Permitted codes: BUSINESS, CONSUMER
        else if (tagName.equals("PRTRY") && tag.getIndex() != null && tag.getIndex().equals("2.18")) {
            String prtry = sampleValuesService.getValue("CtgyPurp_Prtry", index);
            return prtry != null ? prtry : "CONSUMER";
        }
        // ChrgBr - Charge Bearer (Index 2.36)
        // Only 'SLEV' allowed for RTP (Following Service Level - no charging for RTP)
        // Mandatory field, reject with code '650' if not valid
        else if (tagName.equals("CHRGBR")) {
            String chrgBr = sampleValuesService.getValue("ChrgBr", index);
            return chrgBr != null ? chrgBr : "SLEV";
        }
        // InstrId - Instruction Identification (Index 2.2)
        // Format: YYYYMMDDbbbbbbbbbbbbBAAAAnnnnnnnnnnn (35 chars)
        else if (tagName.equals("INSTRID")) {
            return sampleValuesService.generateInstrId();
        }
        // EndToEndId - End To End Identification (Index 2.3)
        // Customer reference for the transaction (max 35 chars)
        else if (tagName.equals("ENDTOENDID")) {
            String endToEndId = sampleValuesService.getValue("EndToEndId", index);
            return endToEndId != null ? endToEndId : generateEndToEndIdentification();
        }
        // TxId - Transaction Identification (Index 2.4)
        // Format: YYYYMMDDbbbbbbbbbbbbBAAAAnnnnnnnnnnn (35 chars)
        // Should equal InstrId when Credit Transfer is first message in transaction
        // event
        else if (tagName.equals("TXID")) {
            return sampleValuesService.generateTxId();
        }
        // UETR - Universally Unique Identifier (Index 2.5)
        // UUID format for end-to-end reference (optional, 36 chars)
        else if (tagName.equals("UETR")) {
            return generateUETR();
        }
        // General ID fields (not MsgId, InstrId, EndToEndId, TxId, UETR)
        else if (tagName.contains("ID") && !tagName.equals("MSGID") && !tagName.equals("INSTRID") &&
                !tagName.equals("ENDTOENDID") && !tagName.equals("TXID") && !tagName.equals("MMBID")) {
            // Use context-aware lookup for account IDs
            // Note: Account IDs are nested: DbtrAcct/CdtrAcct -> Id -> Othr -> Id
            // The actual value is in the nested Id inside Othr (Index ending with .4)
            // The direct Id (Index ending with .1) is a parent with OR condition (IBAN or
            // Othr)
            if (context != null) {
                if (context.equals("DbtrAcct")) {
                    // This is an Id inside DbtrAcct - check if it's the nested Id inside Othr
                    // The nested Id inside Othr has index ending with .4
                    // Structure: 2.917 (DbtrAcct) -> 2.917.1 (Id parent) -> 2.917.1.3 (Othr) ->
                    // 2.917.1.3.4 (Id with value)
                    if (tag.getIndex() != null && tag.getIndex().endsWith(".4")) {
                        // Try context-specific lookup first
                        String dbtrAcctId = sampleValuesService.getValueWithContext("Id", "DbtrAcct", index);
                        if (dbtrAcctId != null && !dbtrAcctId.trim().isEmpty()) {
                            return dbtrAcctId;
                        }
                        // Fallback: direct lookup
                        dbtrAcctId = sampleValuesService.getValue("DbtrAcct_Id", index);
                        if (dbtrAcctId != null && !dbtrAcctId.trim().isEmpty()) {
                            return dbtrAcctId;
                        }
                        // Final fallback: use default from sample_values.json
                        return "US88664715164441";
                    }
                } else if (context.equals("CdtrAcct")) {
                    // This is an Id inside CdtrAcct - check if it's the nested Id inside Othr
                    // The nested Id inside Othr has index ending with .4
                    // Structure: 2.1164 (CdtrAcct) -> 2.1164.1 (Id parent) -> 2.1164.1.3 (Othr) ->
                    // 2.1164.1.3.4 (Id with value)
                    if (tag.getIndex() != null && tag.getIndex().endsWith(".4")) {
                        // Try context-specific lookup first
                        String cdtrAcctId = sampleValuesService.getValueWithContext("Id", "CdtrAcct", index);
                        if (cdtrAcctId != null && !cdtrAcctId.trim().isEmpty()) {
                            return cdtrAcctId;
                        }
                        // Fallback: direct lookup
                        cdtrAcctId = sampleValuesService.getValue("CdtrAcct_Id", index);
                        if (cdtrAcctId != null && !cdtrAcctId.trim().isEmpty()) {
                            return cdtrAcctId;
                        }
                        // Final fallback: use default from sample_values.json
                        return "112277";
                    }
                }
            }

            // Fallback: check for account-related IDs by index
            // This handles cases where context might not be detected correctly
            if (tag.getIndex() != null) {
                // Debtor Account ID - nested Id inside Othr (Index ending with .4)
                // Structure: 2.917 (DbtrAcct) -> 2.917.1 (Id) -> 2.917.1.3 (Othr) ->
                // 2.917.1.3.4 (Id with value)
                if (tag.getIndex().startsWith("2.916") || tag.getIndex().startsWith("2.917") ||
                        tag.getIndex().startsWith("2.918") || tag.getIndex().startsWith("2.919") ||
                        tag.getIndex().startsWith("2.920")) {
                    // Check if this is the nested Id inside Othr (ends with .4)
                    // This is the actual Id value that should be populated
                    if (tag.getIndex().endsWith(".4")) {
                        String dbtrAcctId = sampleValuesService.getValue("DbtrAcct_Id", index);
                        if (dbtrAcctId != null && !dbtrAcctId.trim().isEmpty()) {
                            return dbtrAcctId;
                        }
                        return "US88664715164441"; // Default from sample_values.json
                    }
                }
                // Creditor Account ID - nested Id inside Othr (Index ending with .4)
                // Structure: 2.1164 (CdtrAcct) -> 2.1164.1 (Id) -> 2.1164.1.3 (Othr) ->
                // 2.1164.1.3.4 (Id with value)
                else if (tag.getIndex().startsWith("2.1163") || tag.getIndex().startsWith("2.1164") ||
                        tag.getIndex().startsWith("2.1165") || tag.getIndex().startsWith("2.1166") ||
                        tag.getIndex().startsWith("2.1167")) {
                    // Check if this is the nested Id inside Othr (ends with .4)
                    // This is the actual Id value that should be populated
                    if (tag.getIndex().endsWith(".4")) {
                        String cdtrAcctId = sampleValuesService.getValue("CdtrAcct_Id", index);
                        if (cdtrAcctId != null && !cdtrAcctId.trim().isEmpty()) {
                            return cdtrAcctId;
                        }
                        return "112277"; // Default from sample_values.json
                    }
                }
            }
            // Default: generate a simple ID
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
        // Name fields - use standard test data
        else if (tagName.contains("NM") || tagName.equals("NM")) {
            // Use context-aware lookup (already tried above with normalizedTagName)
            // Fallback: check specific contexts
            if (context != null && (context.equals("Dbtr") || context.equals("Cdtr"))) {
                String nameValue = sampleValuesService.getValue(context + "_Nm", index);
                if (nameValue != null && !nameValue.trim().isEmpty()) {
                    return nameValue;
                }
            }
            // Default fallback
            return "Sample Name";
        }
        // BIC/SWIFT codes (Financial Institution Identification)
        // Used in tags like FinInstnId for identifying financial institutions
        // Applied to: Index 2.106 (PrvsInstgAgt1), Index 2.189 (PrvsInstgAgt2),
        // Index 2.272 (PrvsInstgAgt3), and other FI identifications
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
            // Use context-aware lookup (already tried above with normalizedTagName)
            // Fallback: check specific contexts
            if (context != null) {
                if (context.contains("Agt")) {
                    // Try context-specific lookup (e.g., InstgAgt_MmbId, InstdAgt_MmbId,
                    // DbtrAgt_MmbId, CdtrAgt_MmbId)
                    String mmbId = sampleValuesService.getValue(context + "_MmbId", index);
                    if (mmbId != null && !mmbId.trim().isEmpty()) {
                        return mmbId;
                    }
                }
            }

            // Fallback: check by index for specific agent contexts
            if (tag.getIndex() != null) {
                // InstgAgt MmbId (Index 2.360)
                if (tag.getIndex().equals("2.360") || tag.getIndex().startsWith("2.35")) {
                    String mmbId = sampleValuesService.getValue("InstgAgt_MmbId", index);
                    if (mmbId != null && !mmbId.trim().isEmpty()) {
                        return mmbId;
                    }
                    return "234567891"; // Default from sample_values.json
                }
                // InstdAgt MmbId (Index 2.424)
                else if (tag.getIndex().equals("2.424") || tag.getIndex().startsWith("2.41")) {
                    String mmbId = sampleValuesService.getValue("InstdAgt_MmbId", index);
                    if (mmbId != null && !mmbId.trim().isEmpty()) {
                        return mmbId;
                    }
                    return "071212128"; // Default from sample_values.json
                }
                // DbtrAgt MmbId (Index 2.942)
                else if (tag.getIndex().equals("2.942") || tag.getIndex().startsWith("2.93")
                        || tag.getIndex().startsWith("2.94")) {
                    String mmbId = sampleValuesService.getValue("DbtrAgt_MmbId", index);
                    if (mmbId != null && !mmbId.trim().isEmpty()) {
                        return mmbId;
                    }
                    return "234567891"; // Default from sample_values.json
                }
                // CdtrAgt MmbId (Index 2.1025)
                else if (tag.getIndex().equals("2.1025")
                        || (tag.getIndex().startsWith("2.10") && tag.getIndex().contains("25"))) {
                    String mmbId = sampleValuesService.getValue("CdtrAgt_MmbId", index);
                    if (mmbId != null && !mmbId.trim().isEmpty()) {
                        return mmbId;
                    }
                    return "071212128"; // Default from sample_values.json
                }
            }

            // Default fallback
            String mmbId = sampleValuesService.getValue("InstgAgt_MmbId", index);
            if (mmbId != null && !mmbId.trim().isEmpty()) {
                return mmbId;
            }
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
        // Postal code - use standard test data
        else if (tagName.contains("PSTCD") || tagName.contains("PSTLCD")) {
            // Use context-aware lookup (already tried above with normalizedTagName)
            return "10001";
        }
        // Street Name - use standard test data
        else if (tagName.contains("STRTNM")) {
            // Use context-aware lookup (already tried above with normalizedTagName)
            return "Main Street";
        }
        // Building Number - use standard test data
        else if (tagName.contains("BLDGNB")) {
            // Use context-aware lookup (already tried above with normalizedTagName)
            return "100";
        }
        // Town Name - use standard test data
        else if (tagName.contains("TWNNM")) {
            // Use context-aware lookup (already tried above with normalizedTagName)
            return "City";
        }
        // Country Subdivision (State) - use standard test data
        else if (tagName.contains("CTRYSUBDVSN")) {
            // Use context-aware lookup (already tried above with normalizedTagName)
            return "NY";
        }
        // Birth Date - use standard test data
        else if (tagName.contains("BIRTHDT")) {
            // Use context-aware lookup (already tried above with normalizedTagName)
            return "1990-01-01";
        }
        // City of Birth - use standard test data
        else if (tagName.contains("CITYOFBIRTH")) {
            // Use context-aware lookup (already tried above with normalizedTagName)
            return "New York";
        }
        // Country of Birth - use standard test data
        else if (tagName.contains("CTRYOFBIRTH")) {
            // Use context-aware lookup (already tried above with normalizedTagName)
            return "US";
        }
        // Generic code fields (max 4 chars) - but not the specific CD for clearing
        // system
        else if (tagName.contains("CD") && tag.getLength() != null && tag.getLength() <= 4) {
            return "SALA";
        }
        // Default fallback - ensure we never return empty
        else {
            String fallback = "Sample" + tag.getXmlTag();
            if (fallback == null || fallback.trim().isEmpty()) {
                return "DEFAULT_VALUE";
            }
            return fallback;
        }
    }

    /**
     * Generate fallback value when primary lookup fails - ensures no empty tags
     * 
     * This method is called as a last resort to ensure RTP specification
     * compliance.
     * According to the RTP Message Specification v5.0:
     * - All mandatory tags ([1..1]) MUST have values - they cannot be empty
     * - Optional tags ([0..1]), if included, should have values
     * - Conditional tags that are mandatory within their context must have values
     * - Empty tags violate the RTP specification and will cause validation failures
     * 
     * This method provides specification-compliant default values for all mandatory
     * fields based on the RTP Message Specification document to prevent empty tag
     * generation.
     */
    private String generateFallbackValue(XmlTag tag, XmlTag parentTag) {
        String tagName = tag.getXmlTag().toUpperCase();
        String index = tag.getIndex();
        String context = determineContext(tag, parentTag);

        // Check if parent is PstlAdr - if so, address fields are mandatory
        boolean isAddressField = (parentTag != null && parentTag.getXmlTag().equalsIgnoreCase("PstlAdr"));

        // Check if parent is DtAndPlcOfBirth - if so, birth fields are mandatory
        boolean isBirthField = (parentTag != null && parentTag.getXmlTag().equalsIgnoreCase("DtAndPlcOfBirth"));

        // For mandatory fields, use specification-compliant defaults
        if (tag.isMandatory() || (tag.isConditional() && (isAddressField || isBirthField))) {
            // Mandatory tags must never be empty - use spec-compliant values

            // MmbId - Member Identification (Index 2.111, 2.194, 2.277, 2.360, 2.424,
            // 2.488, 2.571, 2.654, 2.942, 2.1025)
            // Format: 9 character Routing and Transit Number
            if (tagName.equals("MMBID")) {
                // MmbId is mandatory when present - must be 9 characters
                if (context != null) {
                    if (context.equals("InstgAgt")) {
                        return "234567891"; // Default Instructing Agent MmbId
                    } else if (context.equals("InstdAgt")) {
                        return "071212128"; // Default Instructed Agent MmbId
                    } else if (context.equals("DbtrAgt")) {
                        return "234567891"; // Default Debtor Agent MmbId
                    } else if (context.equals("CdtrAgt")) {
                        return "071212128"; // Default Creditor Agent MmbId
                    }
                }
                return "234567891"; // Default RTP routing number
            }
            // ID fields - Account IDs and Identification fields
            else if (tagName.equals("ID") || (tagName.contains("ID") && !tagName.equals("MMBID"))) {
                // Account IDs are mandatory when present
                if (context != null) {
                    if (context.equals("DbtrAcct")) {
                        // Index 2.920 - nested Id inside Othr for DbtrAcct
                        if (index != null && index.endsWith(".4")) {
                            return "US88664715164441"; // Default debtor account ID
                        }
                        return "US88664715164441"; // Default debtor account
                    } else if (context.equals("CdtrAcct")) {
                        // Index 2.1167 - nested Id inside Othr for CdtrAcct
                        if (index != null && index.endsWith(".4")) {
                            return "112277"; // Default creditor account ID
                        }
                        return "112277"; // Default creditor account
                    }
                }
                // Check by index for account IDs
                if (index != null) {
                    // Debtor Account ID (Index 2.920)
                    if (index.endsWith(".4") && (index.startsWith("2.916") || index.startsWith("2.917") ||
                            index.startsWith("2.918") || index.startsWith("2.919") || index.startsWith("2.920"))) {
                        return "US88664715164441"; // Default debtor account
                    }
                    // Creditor Account ID (Index 2.1167)
                    else if (index.endsWith(".4") && (index.startsWith("2.1163") || index.startsWith("2.1164") ||
                            index.startsWith("2.1165") || index.startsWith("2.1166") || index.startsWith("2.1167"))) {
                        return "112277"; // Default creditor account
                    }
                }
                // For MsgId, InstrId, TxId - use proper generation methods
                if (index != null) {
                    if (index.equals("1.1")) {
                        // MsgId - use proper format
                        return sampleValuesService.generateMsgId();
                    } else if (index.equals("2.2")) {
                        // InstrId - use proper format
                        return sampleValuesService.generateInstrId();
                    } else if (index.equals("2.4")) {
                        // TxId - use proper format
                        return sampleValuesService.generateTxId();
                    }
                }
                // For other IDs, generate a valid format (not "ID" + timestamp)
                // Use a proper format based on context
                return String.format("%035d", System.currentTimeMillis() % 10000000000000000L);
            }
            // Name fields - Nm (Index 2.855, 2.1102, etc.)
            else if (tagName.equals("NM") || tagName.contains("NM")) {
                // Names are mandatory when present - max 140 characters
                if (context != null) {
                    if (context.equals("Dbtr")) {
                        return "MRS. GREEN"; // Default debtor name
                    } else if (context.equals("Cdtr")) {
                        return "JENNIFER MARTINEZ"; // Default creditor name (production-ready)
                    } else if (context.equals("UltmtDbtr")) {
                        return "Ultimate Debtor Name"; // Default ultimate debtor name
                    } else if (context.equals("UltmtCdtr")) {
                        return "Ultimate Creditor Name"; // Default ultimate creditor name
                    } else if (context.equals("InitgPty")) {
                        return "Initiating Party Name"; // Default initiating party name
                    }
                }
                return "Sample Name";
            }
            // Currency - Ccy (Index 1.7, 2.20)
            else if (tagName.equals("CCY")) {
                return "USD"; // RTP only supports USD per specification
            }
            // Code fields - Cd (Index 1.31, 2.11)
            else if (tagName.contains("CD") && !tagName.equals("PSTCD")) {
                // Code fields - check which type by index
                if (index != null && index.equals("1.31")) {
                    return "TCH"; // Clearing system code - only 'TCH' allowed
                } else if (index != null && index.startsWith("2.11")) {
                    return "SDVA"; // Service level code - only 'SDVA' allowed
                }
                // For other codes, return TCH (most common)
                return "TCH";
            }
            // Postal Address fields - mandatory when PstlAdr is present
            else if (isAddressField) {
                if (tagName.equals("STRTNM")) {
                    // Street Name - Index 2.865, 2.1112, etc. - max 70 characters
                    if (context != null) {
                        if (context.equals("Dbtr")) {
                            return "Broadway"; // Default debtor street
                        } else if (context.equals("Cdtr")) {
                            return "NORTH AVE"; // Default creditor street
                        }
                    }
                    return "Main Street";
                } else if (tagName.equals("PSTCD") || tagName.equals("PSTLCD")) {
                    // Post Code - Index 2.871, 2.1118, etc. - max 16 characters
                    if (context != null) {
                        if (context.equals("Dbtr")) {
                            return "NY 12345"; // Default debtor postal code
                        } else if (context.equals("Cdtr")) {
                            return "12344"; // Default creditor postal code
                        }
                    }
                    return "10001";
                } else if (tagName.equals("TWNNM")) {
                    // Town Name - Index 2.872, 2.1119, etc. - max 35 characters
                    if (context != null) {
                        if (context.equals("Dbtr")) {
                            return "New York"; // Default debtor town
                        } else if (context.equals("Cdtr")) {
                            return "LOS ANGELES"; // Default creditor town
                        }
                    }
                    return "City";
                } else if (tagName.equals("CTRYSUBDVSN")) {
                    // Country Subdivision - Index 2.875, 2.1122, etc. - max 35 characters
                    if (context != null) {
                        if (context.equals("Dbtr")) {
                            return "NY"; // Default debtor state
                        } else if (context.equals("Cdtr")) {
                            return "LA"; // Default creditor state
                        }
                    }
                    return "NY";
                } else if (tagName.equals("CTRY")) {
                    // Country - Index 2.876, 2.1123, etc. - 2 characters (ISO 3166 Alpha-2)
                    return "US"; // Default country code
                } else if (tagName.equals("BLDGNB")) {
                    // Building Number - Index 2.866, 2.1113, etc. - max 16 characters (optional
                    // even when PstlAdr present)
                    if (context != null) {
                        if (context.equals("Dbtr")) {
                            return "1500"; // Default debtor building number
                        } else if (context.equals("Cdtr")) {
                            return "1123"; // Default creditor building number
                        }
                    }
                    return "100";
                }
            }
            // Birth Date fields - mandatory when DtAndPlcOfBirth is present
            else if (isBirthField) {
                if (tagName.equals("BIRTHDT")) {
                    // Birth Date - Index 2.890, 2.1137, etc. - Format: YYYY-MM-DD (10 characters)
                    if (context != null) {
                        if (context.equals("Dbtr")) {
                            return "1984-01-01"; // Default debtor birth date
                        } else if (context.equals("Cdtr")) {
                            return "1989-01-09"; // Default creditor birth date
                        }
                    }
                    return LocalDateTime.now().minusYears(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } else if (tagName.equals("CITYOFBIRTH")) {
                    // City Of Birth - Index 2.892, 2.1139, etc. - max 35 characters
                    if (context != null) {
                        if (context.equals("Dbtr")) {
                            return "New York"; // Default debtor city of birth
                        } else if (context.equals("Cdtr")) {
                            return "LOS ANGELES"; // Default creditor city of birth
                        }
                    }
                    return "Birth City";
                } else if (tagName.equals("CTRYOFBIRTH")) {
                    // Country Of Birth - Index 2.893, 2.1140, etc. - 2 characters (ISO 3166
                    // Alpha-2)
                    return "US"; // Default country of birth
                }
            }
            // Prtry - Proprietary fields (Index 2.15, 2.18, etc.) - max 35 characters
            else if (tagName.equals("PRTRY")) {
                if (index != null) {
                    if (index.equals("2.15")) {
                        return "STANDARD"; // Local Instrument Prtry - only 'STANDARD' allowed for standard RTP
                    } else if (index.equals("2.18")) {
                        return "CONSUMER"; // Category Purpose Prtry - 'BUSINESS' or 'CONSUMER'
                    }
                }
                return "STANDARD";
            }
            // Other mandatory fields
            else if (tagName.equals("STTLMMTD")) {
                return "CLRG"; // Settlement Method - only 'CLRG' allowed
            } else if (tagName.equals("CHRGBR")) {
                return "SLEV"; // Charge Bearer - only 'SLEV' allowed
            } else if (tagName.equals("NBOFTXS")) {
                return "1"; // Number Of Transactions - always '1' for RTP
            }
        }

        // For conditional tags that might be mandatory in context
        if (tag.isConditional()) {
            // Conditional tags that are mandatory when their parent is present
            if (parentTag != null && parentTag.isMandatory()) {
                // If parent is mandatory and this conditional tag is selected, it needs a value
                if (tagName.equals("OTHR")) {
                    // Othr is a container - its children will have values
                    return null; // Let children handle it
                } else if (tagName.equals("CLRSYSMMBID")) {
                    // ClrSysMmbId is a container - its children will have values
                    return null; // Let children handle it
                }
            }
        }

        // For all other tags, return a meaningful default based on tag type
        if (tagName.contains("AMT")) {
            return "1000.00"; // Amount fields
        } else if (tagName.contains("DT") && !tagName.contains("BIRTHDT")) {
            // Date fields (not birth date)
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else if (tagName.contains("TM")) {
            // Time fields
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        } else if (tagName.contains("PRTRY")) {
            return "DEFAULT"; // Proprietary fields
        }

        // Final fallback - return a meaningful default based on tag type
        // Generate final fallback based on tag type
        String finalTagName = tag.getXmlTag().toUpperCase();
        String finalIndex = tag.getIndex();

        // For IDs, use proper generation methods
        if (finalTagName.equals("MSGID") || (finalIndex != null && finalIndex.equals("1.1"))) {
            return sampleValuesService.generateMsgId();
        } else if (finalTagName.equals("INSTRID") || (finalIndex != null && finalIndex.equals("2.2"))) {
            return sampleValuesService.generateInstrId();
        } else if (finalTagName.equals("TXID") || (finalIndex != null && finalIndex.equals("2.4"))) {
            return sampleValuesService.generateTxId();
        } else if (finalTagName.equals("CD") && finalIndex != null && finalIndex.startsWith("2.11")) {
            return "SDVA"; // Service Level Code must be SDVA
        } else if (finalTagName.equals("CD") && finalIndex != null && finalIndex.equals("1.31")) {
            return "TCH"; // Clearing System Code must be TCH
        } else if (finalTagName.equals("CCY")) {
            return "USD"; // Currency must be USD
        } else if (finalTagName.equals("STTLMTD")) {
            return "CLRG"; // Settlement Method must be CLRG
        } else if (finalTagName.equals("CHRGBR")) {
            return "SLEV"; // Charge Bearer must be SLEV
        } else if (finalTagName.equals("NBOFTXS")) {
            return "1"; // Number of Transactions must be 1
        }

        // Last resort - use tag name with default prefix
        return "DEFAULT_" + tag.getXmlTag().toUpperCase();
    }

    /**
     * Generate final fallback value when all other methods fail
     * This ensures we never return truly invalid values
     */
    private String generateFinalFallbackValue(XmlTag tag, XmlTag parentTag) {
        // Use the same logic as generateFallbackValue but with spec-compliant defaults
        return generateFallbackValue(tag, parentTag);
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
     * Generate Creation Date Time according to ISO 20022 pacs.008 specification
     * (Index 1.2)
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
     * Generate Settlement Amount according to ISO 20022 pacs.008 specification
     * (Index 1.6, 2.19)
     * 
     * Using standard test data: $92,663.51
     * 
     * Requirements:
     * - Maximum 2 decimal (fractional) digits allowed
     * - Maximum 18 total digits (including decimal digits)
     * - Must be greater than zero
     * - Currency must be USD (handled separately)
     * - TtlIntrBkSttlmAmt (Index 1.6) must equal IntrBkSttlmAmt (Index 2.19)
     * 
     * Example: 92663.51
     */
    private String generateSettlementAmount() {
        // Use standard test amount from your template
        return "92663.51";
    }

    /**
     * Generate Interbank Settlement Date according to ISO 20022 pacs.008
     * specification (Index 1.8)
     * Format: YYYY-MM-DD (10 characters)
     * 
     * Requirements:
     * - Must be a valid reconciliation window date
     * - Date must be the date of the Reconciliation Window within which the
     * transaction is processed
     * - If provided date from Debtor FI doesn't match actual Reconciliation Window
     * date,
     * RTP will replace it with the system's Reconciliation Window date
     * - Reject with code '650' if invalid date structure
     * 
     * Note: For sample generation, we use today's date as the reconciliation window
     * date
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
     * Generate Instruction Identification according to ISO 20022 pacs.008
     * specification (Index 2.2)
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
     * Generate End To End Identification according to ISO 20022 pacs.008
     * specification (Index 2.3)
     * 
     * This is the customer reference assigned to the transaction by the initiating
     * party.
     * It must be passed on throughout the entire payment chain without being
     * changed.
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
     * Generate Transaction Identification according to ISO 20022 pacs.008
     * specification (Index 2.4)
     * Format: YYYYMMDDbbbbbbbbbbbbBAAAAnnnnnnnnnnn (35 characters)
     * 
     * Requirements:
     * - Used to reference the first message within a transaction event
     * - When Credit Transfer is the first message in a transaction event,
     * TxId should equal InstrId
     * - When responding to a Request for Payment (pain.013), TxId must carry
     * the Payment Information Identification from the original pain.013
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
     * Generate UETR (Universally Unique Identifier) according to ISO 20022 pacs.008
     * specification (Index 2.5)
     * 
     * UUID v4 format for end-to-end reference of a payment transaction.
     * This is optional and used to link RTP messages to services/arrangements
     * outside RTP network.
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
     * Generate Clearing System Reference according to ISO 20022 pacs.008
     * specification (Index 2.6)
     * 
     * Unique reference assigned by a clearing system to unambiguously identify the
     * instruction.
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
     * Generate BICFI (BIC Financial Institution) according to ISO 20022 pacs.008
     * specification (Index 2.106)
     * 
     * Code allocated to a financial institution by the ISO 9362 Registration
     * Authority.
     * BIC = Banking telecommunication messages - Business identifier code.
     * 
     * Requirements:
     * - Length: 8 or 11 contiguous characters
     * - RegEx: [A-Z0-9]{4,4}[A-Z]{2,2}[A-Z0-9]{2,2}[A-Z0-9]{3,3}{0,1}
     * - Only allowed when Local Instrument is "OLO" or "IXB"
     * - Rejected with code '650' if used with INTERMEDIARY, INDIRECT DOMESTIC,
     * STANDARD, or ZELLE
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
     * Generate Member Identification according to ISO 20022 pacs.008 specification
     * (Index 2.111)
     * 
     * Using standard test routing numbers:
     * - Instructing Agent (Debtor): 234567891
     * - Instructed Agent (Creditor): 071212128
     * 
     * Requirements:
     * - Length: 9 characters (Routing and Transit number)
     * - Only allowed when Local Instrument is "OLO", "INDIRECT DOMESTIC", or "IXB"
     * 
     * Format: 9-digit US Routing Transit Number (RTN)
     */
    private String generateMemberIdentification() {
        // Use standard test routing number (Debtor's bank)
        return "234567891";
    }

    /**
     * Generate IBAN (International Bank Account Number) according to ISO 20022
     * pacs.008 specification (Index 2.170)
     * 
     * International Bank Account Number (IBAN) - identifier used internationally by
     * financial institutions
     * to uniquely identify the account of a customer.
     * 
     * Requirements:
     * - Length: Maximum 34 characters
     * - RegEx: [A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}
     * - Format: Country Code (2 letters) + Check digits (2 numbers) + BBAN (up to
     * 30 alphanumeric)
     * - Only allowed when Local Instrument is "OLO" or "IXB"
     * - Rejected with code '650' if used with INTERMEDIARY, INDIRECT DOMESTIC,
     * STANDARD, or ZELLE
     * 
     * Format breakdown:
     * - Position 1-2: ISO 3166-1 alpha-2 country code (2 letters)
     * - Position 3-4: Check digits (2 numbers)
     * - Position 5-34: Basic Bank Account Number (BBAN) - country specific (up to
     * 30 characters)
     * 
     * Standard: ISO 13616 "Banking and related financial services - International
     * Bank Account Number (IBAN)"
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
