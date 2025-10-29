# 📱 Visual Guide - XML Message Generator Wizard

## 🎯 Overview
A modern, user-friendly 4-step wizard for generating ISO 20022 compliant pacs.008 XML messages with intelligent combination logic.

---

## 🔄 Workflow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    STEP INDICATOR                            │
│  ● Step 1  ───  ○ Step 2  ───  ○ Step 3  ───  ○ Step 4    │
│  Select       Configure      Preview        Results          │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 Step 1: Select Tags

### Layout
```
┌────────────────┬──────────────────────────────────────────┐
│  STATISTICS    │          TAG TREE                        │
│                │                                          │
│  Total: 266    │  [Search Box] [🔍 Search] [Clear]      │
│  M: 147        │                                          │
│  O: 56         │  ┌────────────────────────────────────┐ │
│  C: 63         │  │ ▼ ☑ 1.0 FIToFICustomerCreditTr... │ │
│  Selected: 147 │  │   ▼ ☑ 1.0  GrpHdr                 │ │
│                │  │     ☐ 1.1  MsgId              [M]  │ │
│  LEGEND        │  │     ☐ 1.2  CreDtTm            [M]  │ │
│  [M] Mandatory │  │     ☐ 1.4  NbOfTxs            [M]  │ │
│  [O] Optional  │  │     ▶ ☐ 1.6  TtlIntrBk...     [M]  │ │
│  [C] Condition │  │   ▼ ☑ 2.0  CdtTrfTxInf         │ │
│                │  │     ▼ ☐ 2.1  PmtId             │ │
│                │  │       ☐ 2.5  UETR          [O] │ │
│                │  │       ☐ 2.6  ClrSysRef      [O] │ │
└────────────────┴──────────────────────────────────────────┘
           [Reset]              [Next: Configure →]
```

### Key Features
- **✅ Automatic Selection:** Select optional tag → auto-selects mandatory children
- **🔍 Smart Search:** Find tags by name, element, or index
- **📊 Live Statistics:** Real-time count updates
- **🎨 Color Coding:** Blue badges for M/O/C types

---

## ⚙️ Step 2: Configure

### Layout
```
┌─────────────────────────────────────────────────────────────┐
│             Review Your Selection                            │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  📊 Combination Logic                                  │ │
│  │  • Total combinations: 2^n = 2^2 = 4                  │ │
│  │  • Mandatory tags included in all                      │ │
│  │  • Each combination = different optional tag set       │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Selected Optional Tags (2)                            │ │
│  │  ┌──────────────────────────────────────────────────┐ │ │
│  │  │  2.5  │  UETR  │  Unique End-to-End Trans... │ │ │
│  │  │  2.6  │  ClrSysRef  │  Clearing System Ref... │ │ │
│  │  └──────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
└─────────────────────────────────────────────────────────────┘
       [← Back]                    [Next: Generate →]
```

### Information Displayed
- **Combination count** (2^n formula)
- **Selected tags list** with indices
- **Warning** if >10 tags selected

---

## 🔄 Step 3: Preview & Generate

### Layout (Generating)
```
┌─────────────────────────────────────────────────────────────┐
│                                                              │
│                    ⟳ GENERATING...                          │
│                                                              │
│          Generating XML Combinations...                      │
│          This may take a moment...                          │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Layout (Complete)
```
┌─────────────────────────────────────────────────────────────┐
│  ✓ Successfully generated 4 combinations in 142ms           │
│  Total: 4  |  Time: 142ms                                   │
│                                                              │
│  Generated Combinations (4)                                  │
│  ┌──────────────┬──────────────┬──────────────┬──────────┐ │
│  │   #1         │    #2        │     #3       │    #4    │ │
│  │ Base message │ With UETR    │ With         │ With     │ │
│  │              │              │ ClrSysRef    │ Both     │ │
│  │              │              │              │          │ │
│  │              │  [UETR]      │ [ClrSysRef]  │ [UETR]   │ │
│  │              │              │              │[ClrSysRef│ │
│  │              │              │              │          │ │
│  │[👁️ Preview] │[👁️ Preview] │[👁️ Preview] │[👁Preview│ │
│  │[💾 Download]│[💾 Download]│[💾 Download]│[💾Down.. │ │
│  └──────────────┴──────────────┴──────────────┴──────────┘ │
└─────────────────────────────────────────────────────────────┘
     [← Back]                    [Next: View Results →]
```

### Features
- **Real-time generation** with loading spinner
- **Success message** with timing
- **Grid view** of all combinations
- **Quick actions** per combination

---

## 📥 Step 4: Results

### Layout
```
┌─────────────────────────────────────────────────────────────┐
│  Download & Review                                           │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │   4 Files  │  2 Tags  │  142ms  │ [📥 Download All] │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌──────────┬─────────────────────────────────────────────┐ │
│  │ COMBOS   │         XML PREVIEW                         │ │
│  │          │                                             │ │
│  │ ● #1     │ Combination #1                              │ │
│  │   Base   │ Base message (mandatory tags only)          │ │
│  │          │ [💾 Download This File]                     │ │
│  │ ○ #2     │                                             │ │
│  │   UETR   │ <?xml version="1.0"...                     │ │
│  │          │ <Document xmlns="urn:iso:std...            │ │
│  │ ○ #3     │   <FIToFICstmrCdtTrf>                     │ │
│  │   ClrSys │     <GrpHdr>                               │ │
│  │          │       <MsgId>MSG12345678</MsgId>           │ │
│  │ ○ #4     │       <CreDtTm>2024-01-15T...</CreDtTm>   │ │
│  │   Both   │       ...                                   │ │
│  │          │                                             │ │
│  └──────────┴─────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
  [🔄 Start Over]              [← Back to Generation]
```

### Features
- **Summary stats** at the top
- **Sidebar navigation** through combinations
- **Full XML preview** with syntax formatting
- **Individual or batch download**

---

## 🎨 Color Scheme

```
Primary (Microsoft Blue): #0d6efd  ████████
Background:               #f8f9fa  ████████
Text Dark:                #212529  ████████
Text Medium:              #495057  ████████
Success:                  #28a745  ████████
Border:                   #e9ecef  ████████
```

---

## 🔄 Combination Logic Example

### 2 Optional Tags Selected

```
Selected: UETR (2.5), ClrSysRef (2.6)
Result: 2² = 4 combinations

┌────────────────────────────────────────────────────┐
│ Combination 1: BASE MESSAGE                        │
│ ✗ UETR        ✗ ClrSysRef                         │
│ (Only mandatory tags)                              │
└────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────┐
│ Combination 2: WITH UETR                           │
│ ✓ UETR        ✗ ClrSysRef                         │
│ (Mandatory + UETR)                                 │
└────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────┐
│ Combination 3: WITH ClrSysRef                      │
│ ✗ UETR        ✓ ClrSysRef                         │
│ (Mandatory + ClrSysRef)                            │
└────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────┐
│ Combination 4: WITH BOTH                           │
│ ✓ UETR        ✓ ClrSysRef                         │
│ (Mandatory + UETR + ClrSysRef)                     │
└────────────────────────────────────────────────────┘
```

---

## ✨ Interactive Elements

### Hover Effects
```
Buttons:     [Normal]  →  [Elevated + Shadow]
Cards:       [Flat]    →  [Lifted + Border Highlight]
Tags:        [Static]  →  [Highlighted Background]
Checkboxes:  [Normal]  →  [Scaled 1.1x]
```

### Animations
- **Page transitions:** Fade in from bottom (300ms)
- **Step changes:** Smooth fade (200ms)
- **Loading spinners:** Continuous rotation
- **Success messages:** Slide down from top

---

## 📱 Responsive Behavior

### Desktop (>1200px)
- Full sidebar visible
- Grid layout for combinations
- Side-by-side preview panel

### Tablet (768px - 1200px)
- Sidebar becomes horizontal bar
- Combination grid adjusts columns
- Preview panel stacks vertically

### Mobile (<768px)
- Statistics cards stack
- Single column layout
- Full-width preview
- Simplified step labels (numbers only)

---

## 🎯 User Journey

```
1. DISCOVER
   ↓
   Browse tag tree
   Read descriptions
   View statistics
   
2. SELECT
   ↓
   Check optional tags
   See auto-selection of children
   Search for specific tags
   
3. CONFIGURE
   ↓
   Review selection
   Understand 2^n logic
   Check warnings
   
4. GENERATE
   ↓
   Watch progress
   See results
   Preview combinations
   
5. DOWNLOAD
   ↓
   View XML content
   Download files
   Start over
```

---

## 💡 Tips & Best Practices

1. **Start Small:** Select 2-3 optional tags first to understand the pattern
2. **Use Search:** Find tags quickly using keywords
3. **Check Mandatory:** Mandatory children are auto-selected - check the tree expansion
4. **Preview Before Download:** Use the preview feature to verify XML structure
5. **Batch Download:** Use "Download All" for convenience with many combinations

---

**🎉 Enjoy generating compliant ISO 20022 pacs.008 XML messages!**

