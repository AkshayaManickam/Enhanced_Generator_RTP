# XML Generation Flow - Complete Explanation

## Overview
This document explains how values are populated into XML tags during the RTP message generation process.

---

## 📁 Key Files and Their Roles

### 1. **sample_values.json**
**Location:** `galaxy-rtp-validator-service/src/main/resources/sample_values.json`

**Purpose:** Central configuration file storing all tag values and dynamic generation rules.

**Contains:**
- **Static Values:** Direct values like `"NbOfTxs": { "value": "1" }`
- **Dynamic Templates:** Templates with placeholders like `"MsgId": { "template": "M{YYYYMMDD}101010101MTBOTS{RANDOM_4}" }`
- **Context-Aware Values:** Values that depend on context (e.g., `Dbtr_Nm`, `Cdtr_StrtNm`)
- **Dynamic Generation Rules:** Rules for generating complex IDs

**Example:**
```json
{
  "MsgId": {
    "template": "M{YYYYMMDD}101010101MTBOTS{RANDOM_4}",
    "description": "Message ID with dynamic date and random suffix"
  },
  "Dbtr_Nm": {
    "value": "MRS. GREEN",
    "description": "Debtor Name"
  },
  "NbOfTxs": {
    "value": "1",
    "description": "Number Of Transactions - Always 1 for RTP"
  }
}
```

---

### 2. **SampleValuesService.java**
**Location:** `galaxy-rtp-validator-service/src/main/java/com/finzly/galaxy/rtp/validator/service/SampleValuesService.java`

**Purpose:** Service that loads and provides values from `sample_values.json`.

**Key Methods:**

#### `loadSampleValues()` - Called at startup (@PostConstruct)
```java
@PostConstruct
public void loadSampleValues() {
    // Loads sample_values.json from classpath
    // Stores it in memory as JsonNode
}
```

**Flow:**
1. Application starts → Spring initializes `SampleValuesService`
2. `@PostConstruct` triggers → `loadSampleValues()` executes
3. Reads `sample_values.json` from `src/main/resources/`
4. Parses JSON into `JsonNode` object
5. Stores in memory for fast lookups

#### `getValue(String tagKey, String index)` - Get static or dynamic value
```java
public String getValue(String tagKey, String index) {
    // 1. Try exact match: sampleValues.get(tagKey)
    // 2. Try case-insensitive match
    // 3. If found, check for "value" (static) or "template" (dynamic)
    // 4. If template, process it (replace placeholders)
    // 5. Return the value
}
```

#### `getValueWithContext(String baseTag, String context, String index)` - Context-aware lookup
```java
public String getValueWithContext(String baseTag, String context, String index) {
    // Example: baseTag="Nm", context="Dbtr" → Looks for "Dbtr_Nm"
    // If not found, falls back to baseTag lookup
}
```

#### `processTemplate(String tagKey, String template)` - Process dynamic templates
```java
private String processTemplate(String tagKey, String template) {
    // Replaces placeholders:
    // - {YYYYMMDD} → Current date (20251104)
    // - {CURRENT_DATE} → Current date (2025-11-04)
    // - {CURRENT_DATETIME} → Current datetime (2025-11-04T15:30:45)
    // - {RANDOM_4} → 4 random digits (7439)
    // - {RANDOM_11} → 11 random digits (01167076705)
}
```

**Example:**
- Input: `"M{YYYYMMDD}101010101MTBOTS{RANDOM_4}"`
- Output: `"M20251104101010101MTBOTS7439"`

---

### 3. **TagDataService.java**
**Location:** `galaxy-rtp-validator-service/src/main/java/com/finzly/galaxy/rtp/validator/service/TagDataService.java`

**Purpose:** Defines the static XML tag structure (hierarchy, indexes, types).

**Key Methods:**

#### `initializeTagData()` - Called at startup (@PostConstruct)
```java
@PostConstruct
public void initializeTagData() {
    // Creates the complete XML tag hierarchy
    // Example structure:
    // - FIToFICstmrCdtTrf (1.0)
    //   - GrpHdr (1.0)
    //     - MsgId (1.1) [MANDATORY]
    //     - CreDtTm (1.2) [MANDATORY]
    //     - NbOfTxs (1.4) [MANDATORY]
    //     - TtlIntrBkSttlmAmt (1.6) [MANDATORY]
    //       - Ccy (1.7) [MANDATORY]
    //     - IntrBkSttlmDt (1.8) [MANDATORY]
    //     - SttlmInf (1.9) [MANDATORY]
    //       - SttlmMtd (1.10) [MANDATORY]
    //       - ClrSys (1.30) [MANDATORY]
    //         - Cd (1.31) [MANDATORY]
    //   - CdtTrfTxInf (2.0) [MANDATORY]
    //     - ... (all transaction tags)
}
```

**Creates:**
- Complete XML tag tree structure
- Tag types (MANDATORY, OPTIONAL, CONDITIONAL)
- Tag indexes (e.g., "1.1", "2.855")
- Parent-child relationships

---

### 4. **XmlGenerationService.java**
**Location:** `galaxy-rtp-validator-service/src/main/java/com/finzly/galaxy/rtp/validator/service/XmlGenerationService.java`

**Purpose:** Main service that generates XML from tag structure and values.

**Key Methods:**

#### `generateXmlCombinations(List<String> selectedIndices)` - Entry Point
```java
public XmlGenerationResult generateXmlCombinations(List<String> selectedIndices) {
    // 1. Get all tags from TagDataService
    // 2. Build tag cache for fast lookups
    // 3. Build optional tag hierarchy
    // 4. Generate all combinations
    // 5. For each combination, generate XML
    // 6. Return results
}
```

#### `generateXmlForCombination(...)` - Generate XML for one combination
```java
private String generateXmlForCombination(...) {
    // 1. Create XML header
    // 2. For each root tag, call generateTagXml() recursively
    // 3. Return complete XML string
}
```

#### `generateTagXml(...)` - Recursively generate XML for each tag
```java
private void generateTagXml(StringBuilder xml, XmlTag tag, ..., XmlTag parentTag) {
    // 1. Check if tag should be included (mandatory/optional/conditional)
    // 2. If tag has children:
    //    - Open tag: <ct:TagName>
    //    - Recursively process children
    //    - Close tag: </ct:TagName>
    // 3. If tag is leaf node:
    //    - Call generateSampleValue() to get value
    //    - Check if value is empty → call generateFallbackValue()
    //    - Write: <ct:TagName>value</ct:TagName>
}
```

#### `generateSampleValue(XmlTag tag, XmlTag parentTag)` - Get value for a tag
```java
private String generateSampleValue(XmlTag tag, XmlTag parentTag) {
    // STEP 1: Determine context (Dbtr, Cdtr, DbtrAcct, etc.)
    String context = determineContext(tag, parentTag);
    
    // STEP 2: Try context-aware lookup first
    // Example: For "Nm" tag with context="Dbtr" → Look for "Dbtr_Nm"
    if (context != null) {
        jsonValue = sampleValuesService.getValueWithContext("Nm", "Dbtr", index);
    }
    
    // STEP 3: Try direct lookup
    if (jsonValue == null) {
        jsonValue = sampleValuesService.getValue("Nm", index);
    }
    
    // STEP 4: If found, return it
    if (jsonValue != null) {
        return jsonValue;
    }
    
    // STEP 5: Special handling for specific tags
    // - MsgId → call generateMsgId()
    // - InstrId → call generateInstrId()
    // - CreDtTm → get from sample_values or generate
    // - etc.
    
    // STEP 6: Return generated value or null (will trigger fallback)
}
```

#### `generateFallbackValue(XmlTag tag, XmlTag parentTag)` - Fallback when no value found
```java
private String generateFallbackValue(XmlTag tag, XmlTag parentTag) {
    // Ensures NO tag is ever empty
    // Provides specification-compliant defaults based on:
    // - Tag name (e.g., "MmbId" → "234567891")
    // - Tag index (e.g., "1.31" → "TCH")
    // - Context (e.g., Dbtr → "MRS. GREEN")
    // - Parent tag (e.g., PstlAdr → address fields are mandatory)
    
    // Returns appropriate default value based on RTP specification
}
```

---

## 🔄 Complete Generation Flow

### Step-by-Step Process:

```
1. USER REQUEST
   ↓
   Frontend (Angular) → POST /api/tags/generate-xml-combinations
   {
     "selectedTagIndices": ["2.104", "2.187", ...]
   }
   ↓

2. CONTROLLER (TagController.java)
   ↓
   @PostMapping("/generate-xml-combinations")
   public ResponseEntity<XmlGenerationResult> generateXmlCombinations(...) {
       return xmlGenerationService.generateXmlCombinations(request.getSelectedTagIndices());
   }
   ↓

3. XML GENERATION SERVICE (XmlGenerationService.java)
   ↓
   generateXmlCombinations(List<String> selectedIndices) {
       
       A. Get Tag Structure
          ↓
          List<XmlTag> allTags = tagDataService.getAllTags();
          // Returns complete XML tag hierarchy from TagDataService
       
       B. Build Tag Cache
          ↓
          tagCache = buildTagCache(allTags);
          // Creates Map<Index, XmlTag> for O(1) lookups
       
       C. Build Optional Tag Hierarchy
          ↓
          List<OptionalTagNode> hierarchy = buildOptionalTagHierarchy(...);
          // Identifies parent-child relationships among optional tags
       
       D. Generate All Combinations
          ↓
          List<Set<String>> combinations = generateNestedCombinations(...);
          // Creates all valid combinations of optional tags
       
       E. For Each Combination:
          ↓
          processSingleCombination(...) {
              ↓
              generateXmlForCombination(...) {
                  ↓
                  For each root tag:
                      generateTagXml(...) {
                          // RECURSIVE PROCESSING
                      }
              }
          }
   }
   ↓

4. FOR EACH TAG (generateTagXml method):
   ↓
   A. Check if tag should be included
      - Mandatory → Always include
      - Optional → Include if in selected combination
      - Conditional → Include based on parent OR condition
   ↓
   B. If tag has children:
      - Open tag: <ct:TagName>
      - Recursively process each child
      - Close tag: </ct:TagName>
   ↓
   C. If tag is leaf node (no children):
      - Call generateSampleValue(tag, parentTag)
      - If value is empty → Call generateFallbackValue(tag, parentTag)
      - Write: <ct:TagName>value</ct:TagName>
   ↓

5. VALUE RETRIEVAL (generateSampleValue method):
   ↓
   A. Determine Context
      - Traverse parent hierarchy
      - Identify context (Dbtr, Cdtr, DbtrAcct, etc.)
   ↓
   B. Try Context-Aware Lookup
      - Example: "Nm" with context="Dbtr"
      - Look for: "Dbtr_Nm" in sample_values.json
      - If found → Return value
   ↓
   C. Try Direct Lookup
      - Look for: "Nm" in sample_values.json
      - If found → Return value
   ↓
   D. Try Special Tag Handling
      - MsgId → sampleValuesService.generateMsgId()
      - InstrId → sampleValuesService.generateInstrId()
      - CreDtTm → sampleValuesService.getValue("CreDtTm", index)
      - etc.
   ↓
   E. If still null → Return null (triggers fallback)
   ↓

6. FALLBACK VALUE (generateFallbackValue method):
   ↓
   A. Check if tag is mandatory or conditional in mandatory context
   ↓
   B. Provide specification-compliant default based on:
      - Tag name (MmbId → "234567891")
      - Tag index (1.31 → "TCH")
      - Context (Dbtr → "MRS. GREEN")
      - Parent tag (PstlAdr → address fields)
   ↓
   C. Return default value
   ↓

7. SAMPLE VALUES SERVICE (SampleValuesService.java):
   ↓
   A. getValue(String tagKey, String index):
      - Look in sample_values.json (loaded at startup)
      - Try exact match
      - Try case-insensitive match
      - If has "value" → Return static value
      - If has "template" → Process template and return
      - Return null if not found
   ↓
   B. processTemplate(String tagKey, String template):
      - Replace {YYYYMMDD} → Current date (20251104)
      - Replace {CURRENT_DATE} → Current date (2025-11-04)
      - Replace {CURRENT_DATETIME} → Current datetime (2025-11-04T15:30:45)
      - Replace {RANDOM_4} → 4 random digits (7439)
      - Replace {RANDOM_11} → 11 random digits (01167076705)
      - Return processed template
   ↓

8. RESULT
   ↓
   XmlGenerationResult {
       success: true,
       totalCombinations: 5,
       combinations: [
           {
               combinationNumber: 1,
               description: "Base message (mandatory tags only)",
               xmlContent: "<?xml version=\"1.0\" encoding=\"UTF-8\"?>..."
           },
           ...
       ]
   }
   ↓
   Returned to Frontend
```

---

## 📊 Value Retrieval Priority

When generating a value for a tag, the system tries in this order:

### Priority 1: Context-Aware Lookup
```
Tag: <Nm> (Name)
Context: "Dbtr" (from parent tag hierarchy)
Lookup: "Dbtr_Nm" in sample_values.json
Result: "MRS. GREEN" ✅
```

### Priority 2: Direct Lookup
```
Tag: <Nm> (Name)
Lookup: "Nm" in sample_values.json
Result: "Sample Name" ✅
```

### Priority 3: Special Tag Handling
```
Tag: <MsgId>
Special Handler: sampleValuesService.generateMsgId()
Result: "M20251104101010101MTBOTS7439" ✅
```

### Priority 4: Fallback Value
```
Tag: <Nm> (Name)
No value found in any lookup
Fallback: generateFallbackValue(tag, parentTag)
Result: "Sample Name" ✅ (based on context and spec)
```

---

## 🎯 Example: Complete Flow for One Tag

### Example: Generating `<Dbtr><Nm>MRS. GREEN</Nm></Dbtr>`

```
1. generateTagXml() called for <Dbtr> tag
   - Tag is mandatory → shouldInclude = true
   - Tag has children → Process children recursively
   
2. Open tag: <ct:Dbtr>
   
3. Process child: <Nm> tag
   - generateTagXml() called for <Nm> tag
   - Tag is mandatory → shouldInclude = true
   - Tag is leaf node → Need to get value
   
4. Call generateSampleValue(<Nm>, <Dbtr>)
   
5. determineContext(<Nm>, <Dbtr>)
   - Parent is "Dbtr"
   - Context = "Dbtr" ✅
   
6. Try context-aware lookup:
   - sampleValuesService.getValueWithContext("Nm", "Dbtr", "2.855")
   - Looks for "Dbtr_Nm" in sample_values.json
   - Found: { "value": "MRS. GREEN" } ✅
   
7. Return value: "MRS. GREEN"
   
8. Write to XML: <ct:Nm>MRS. GREEN</ct:Nm>
   
9. Close parent: </ct:Dbtr>
```

---

## 🔧 Dynamic Value Generation

### Example: MsgId Generation

```
1. Template in sample_values.json:
   {
     "MsgId": {
       "template": "M{YYYYMMDD}101010101MTBOTS{RANDOM_4}"
     }
   }

2. generateSampleValue() called for <MsgId>
   - Calls: sampleValuesService.generateMsgId()
   
3. generateMsgId() in SampleValuesService:
   - Gets current date: "20251104"
   - Generates 4 random digits: "7439"
   - Constructs: "M" + "20251104" + "101010101MTBOTS" + "3159162" + "7439"
   - Returns: "M20251104101010101MTBOTS31591627439"

4. Value written to XML:
   <ct:MsgId>M20251104101010101MTBOTS31591627439</ct:MsgId>
```

---

## 📝 Key Points

1. **No Empty Tags:** The system ensures NO tag is ever empty:
   - `generateSampleValue()` tries multiple lookup methods
   - If all fail → `generateFallbackValue()` provides spec-compliant default
   - Final fallback: "DEFAULT_VALUE" for leaf nodes

2. **Context-Aware:** Values can be different based on context:
   - `Dbtr_Nm` = "MRS. GREEN"
   - `Cdtr_Nm` = "Participant Valid model"
   - Same tag name, different values based on parent

3. **Dynamic Generation:** Some values are generated dynamically:
   - Dates: Current date/time
   - Random numbers: For IDs
   - Templates: Processed at runtime

4. **Specification Compliance:** All values follow RTP Message Specification v5.0:
   - Mandatory tags always have values
   - Conditional tags have values when their context is mandatory
   - Values match format requirements (length, format, etc.)

---

## 🗂️ File Structure Summary

```
galaxy-rtp-validator-service/
├── src/main/
│   ├── resources/
│   │   └── sample_values.json          ← All tag values and templates
│   └── java/com/finzly/galaxy/rtp/validator/
│       ├── controller/
│       │   └── TagController.java      ← API endpoint entry point
│       └── service/
│           ├── TagDataService.java     ← Defines XML tag structure
│           ├── SampleValuesService.java ← Loads and provides values
│           └── XmlGenerationService.java ← Generates XML from tags + values
```

---

## 🎬 Complete Request Flow Diagram

```
[Frontend] 
    ↓ POST /api/tags/generate-xml-combinations
[TagController]
    ↓ generateXmlCombinations()
[XmlGenerationService]
    ↓ generateXmlCombinations()
    ├─→ TagDataService.getAllTags() → Get tag structure
    ├─→ SampleValuesService (injected) → Get values
    └─→ generateXmlForCombination()
        └─→ generateTagXml() [RECURSIVE]
            ├─→ generateSampleValue()
            │   ├─→ determineContext()
            │   ├─→ SampleValuesService.getValueWithContext()
            │   ├─→ SampleValuesService.getValue()
            │   └─→ Special handlers (generateMsgId, etc.)
            └─→ generateFallbackValue() [if value is empty]
                └─→ Provides spec-compliant defaults
```

---

This completes the explanation of how values flow into XML tags during generation!

