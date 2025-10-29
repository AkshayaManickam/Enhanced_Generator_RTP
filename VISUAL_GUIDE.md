# 🎨 Galaxy RTP Validator - Visual Guide

## Application Screenshots & Layout

### 🏠 Main Application Layout

```
┌────────────────────────────────────────────────────────────────────────┐
│                                                                        │
│  Galaxy RTP Validator - XML Tag Selector                             │
│  FIToFICustomer Credit Transfer V08                                   │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘
┌────────────────┬───────────────────────────────────────────────────────┐
│                │                                                       │
│ Tag Statistics │  🔍 Search: [________________] [Search] [Clear]       │
│                │                                                       │
│ Total: 215     │  [💾 Save] [📄 Generate XML] [🔄 Reset]               │
│ Mandatory: 62  │                                                       │
│ Optional: 128  │  ┌─────────────────────────────────────────────────┐ │
│ Conditional:25 │  │ ☐ Index  XML Tag    Element Name    Occ. Len  T │ │
│ Selected: 62   │  ├─────────────────────────────────────────────────┤ │
│                │  │ ☑ 1.0   GrpHdr      Group Header    [1..1]     M │ │
│ Legend:        │  │   ▼ 1.1  MsgId      Message ID      [1..1] 35  M │ │
│ [M] Mandatory  │  │     1.2  CreDtTm    Creation Date   [1..1] 19  M │ │
│ [O] Optional   │  │ ☑ 2.0   CdtTrfTxInf Credit Transfer [1..1]     M │ │
│ [C] Conditional│  │   ▼ 2.1  PmtId      Payment ID      [1..1]     M │ │
│                │  │   ☐ 2.104 PrvsInstg Previous Agent  [0..1]     O │ │
│                │  │   ☑ 2.353 InstgAgt  Instructing Agt [1..1]     M │ │
│                │  │   ☐ 2.792 InitgPty  Initiating Pty [0..1]     C │ │
│                │  └─────────────────────────────────────────────────┘ │
│                │                                                       │
└────────────────┴───────────────────────────────────────────────────────┘
```

---

## 🎯 Component Breakdown

### 1. Header Section
```
═══════════════════════════════════════════════════════════
  Galaxy RTP Validator - XML Tag Selector
  FIToFICustomer Credit Transfer V08
═══════════════════════════════════════════════════════════
```
- **Style**: Purple gradient background
- **Font**: Bold, large title
- **Color**: White text on purple (#667eea to #764ba2)

---

### 2. Statistics Sidebar (Left)

```
┌──────────────────────┐
│  Tag Statistics      │
├──────────────────────┤
│  📊 Total Tags       │
│      215             │
├──────────────────────┤
│  ✅ Mandatory        │
│      62              │
│  Background: Green   │
├──────────────────────┤
│  🔵 Optional         │
│      128             │
│  Background: Blue    │
├──────────────────────┤
│  ⚠️  Conditional     │
│      25              │
│  Background: Yellow  │
├──────────────────────┤
│  🎯 Selected         │
│      62              │
│  Background: Purple  │
├──────────────────────┤
│  Legend              │
│  [M] Mandatory       │
│  [O] Optional        │
│  [C] Conditional     │
└──────────────────────┘
```

**Features**:
- Sticky position (stays visible on scroll)
- Color-coded cards
- Real-time updates
- Legend with badges

---

### 3. Search & Action Bar

```
┌─────────────────────────────────────────────────────────────┐
│  🔍 Search: [Search tags by name, element, or index...]     │
│  [🔍 Search] [Clear]                                         │
│                                                              │
│  [💾 Save Selection] [📄 Generate XML] [🔄 Reset]           │
└─────────────────────────────────────────────────────────────┘
```

**Buttons**:
- **Search**: Purple (#8b5cf6)
- **Clear**: Light gray
- **Save Selection**: Blue (#2563eb)
- **Generate XML**: Green (#10b981)
- **Reset**: Light gray with border

---

### 4. Tag Tree Table

```
┌──────┬─────────┬────────────────┬─────────────────┬──────────┬────────┬──────┐
│  ☑️  │ Index   │ XML Tag        │ Element Name    │ Occurr.  │ Length │ Type │
├──────┼─────────┼────────────────┼─────────────────┼──────────┼────────┼──────┤
│  ☑️  │ 1.0     │ GrpHdr         │ Group Header    │ [1..1]   │   -    │  M   │
│  ▼☑️ │ 1.1     │  MsgId         │ Message ID      │ [1..1]   │  35    │  M   │
│   ☑️ │ 1.2     │  CreDtTm       │ Creation Date   │ [1..1]   │  19    │  M   │
│  ☑️  │ 2.0     │ CdtTrfTxInf    │ Credit Transfer │ [1..1]   │   -    │  M   │
│  ▼☑️ │ 2.1     │  PmtId         │ Payment ID      │ [1..1]   │   -    │  M   │
│  ☐   │ 2.104   │  PrvsInstgAgt1 │ Previous Agent  │ [0..1]   │   -    │  O   │
│  ☑️  │ 2.353   │  InstgAgt      │ Instructing Agt │ [1..1]   │   -    │  M   │
│  ⛔  │ 2.792   │  InitgPty      │ Initiating Pty  │ [0..1]   │   -    │  C   │
└──────┴─────────┴────────────────┴─────────────────┴──────────┴────────┴──────┘
```

**Row Colors**:
- **Green Left Border**: Mandatory tag
- **Blue Left Border**: Optional tag
- **Yellow Left Border**: Conditional tag
- **Hover**: Light gray background

**Checkboxes**:
- ☑️ **Checked + Enabled**: Selected optional tag
- ☑️ **Checked + Disabled**: Mandatory tag (always selected)
- ☐ **Unchecked + Enabled**: Unselected optional tag
- ⛔ **Disabled**: Conditional tag (shows warning)

**Type Badges**:
```
[M] - Green badge, white text
[O] - Blue badge, white text
[C] - Yellow badge, white text
```

---

## 🎨 Color Palette

### Primary Colors
```
Primary Blue:    #2563eb  ████
Success Green:   #10b981  ████
Warning Yellow:  #f59e0b  ████
Error Red:       #ef4444  ████
```

### Tag Type Colors
```
Mandatory:       #10b981  ████  (Green)
Optional:        #3b82f6  ████  (Blue)
Conditional:     #f59e0b  ████  (Yellow/Orange)
```

### Background Colors
```
Light:           #f9fafb  ████
Dark:            #1f2937  ████
White:           #ffffff  ████
Border:          #e5e7eb  ████
```

### Gradient
```
Header:          #667eea → #764ba2
                 (Purple gradient left to right)
```

---

## 🔔 Message Banners

### Success Message
```
┌─────────────────────────────────────────────────────────┐
│  ✅ Selection saved successfully!                  [×]  │
└─────────────────────────────────────────────────────────┘
```
- **Background**: Light green (#d1fae5)
- **Text**: Dark green (#065f46)

### Error Message
```
┌─────────────────────────────────────────────────────────┐
│  ❌ Conditional tags are not allowed for XML gene... [×]│
└─────────────────────────────────────────────────────────┘
```
- **Background**: Light red (#fee2e2)
- **Text**: Dark red (#991b1b)

### Info Message
```
┌─────────────────────────────────────────────────────────┐
│  ℹ️ Mandatory tags cannot be deselected.           [×]  │
└─────────────────────────────────────────────────────────┘
```
- **Background**: Light blue (#dbeafe)
- **Text**: Dark blue (#1e40af)

---

## 📱 Responsive Breakpoints

### Desktop (> 1200px)
```
┌─────────┬──────────────────────────┐
│ Sidebar │     Main Content         │
│  300px  │       Flexible           │
└─────────┴──────────────────────────┘
```

### Tablet (768px - 1200px)
```
┌──────────────────────────────────────┐
│          Sidebar (full width)        │
├──────────────────────────────────────┤
│          Main Content                │
└──────────────────────────────────────┘
```

### Mobile (< 768px)
```
┌────────────────────┐
│    Sidebar         │
├────────────────────┤
│  Simplified Table  │
│  (Stacked Layout)  │
└────────────────────┘
```

---

## 🔍 Interaction States

### Button States
```
Normal:    [  Button  ]  → Gray background
Hover:     [  Button  ]  → Slightly darker, lifted shadow
Active:    [  Button  ]  → Pressed appearance
Disabled:  [  Button  ]  → Gray, cursor: not-allowed
```

### Checkbox States
```
Unchecked:          ☐
Checked:            ☑️
Indeterminate:      ▣
Disabled:           ☐ (grayed out)
Disabled + Checked: ☑️ (grayed out)
```

### Tree Expand/Collapse
```
Collapsed:  ▶  (Right arrow)
Expanded:   ▼  (Down arrow)
```

---

## 🎭 Visual Indicators

### Tag Row Indentation
```
Level 0:  [No indent]
Level 1:  [20px indent]
Level 2:  [40px indent]
Level 3:  [60px indent]
Level 4:  [80px indent]
Level 5:  [100px indent]
```

### Loading State
```
┌─────────────────────────────┐
│                             │
│      🔄 (spinning)          │
│                             │
│    Loading tags...          │
│                             │
└─────────────────────────────┘
```

### No Results State
```
┌─────────────────────────────┐
│                             │
│      🔍                     │
│                             │
│  No tags found.             │
│  Try a different search.    │
│                             │
└─────────────────────────────┘
```

---

## 🖱️ User Interactions

### 1. Click on Row (Anywhere)
```
Before: [    Row Content    ]
After:  [  ▼ Row Content    ] (Expands children)
```

### 2. Click on Checkbox
```
Optional Tag:
☐ → ☑️ (Selects tag)
☑️ → ☐ (Deselects tag)

Mandatory Tag:
☑️ → Shows message "Mandatory tags cannot be deselected"

Conditional Tag:
☐ → Shows error "Conditional tags are not allowed..."
```

### 3. Search Input
```
Type: "Debtor"
→ Filters tree to show only matching tags
→ Auto-expands parent nodes of matches
→ Highlights matched terms
```

### 4. Hover Effects
```
Row Hover:     Background changes to light gray
Button Hover:  Slight lift with shadow
Link Hover:    Underline appears
```

---

## 📊 Statistics Animation

When counts update:
```
Old Value: 62
           ↓ (fade transition)
New Value: 85
```
- Smooth number transition
- Color pulse effect
- Duration: 300ms

---

## ✨ Special Effects

### 1. Smooth Scrolling
- Tree body has smooth scroll
- Sidebar sticky on scroll

### 2. Transitions
- All color changes: 0.2s ease
- Button hover: 0.2s ease
- Message banner: fade in/out

### 3. Shadows
- Cards: `0 1px 3px rgba(0, 0, 0, 0.1)`
- Buttons hover: `0 4px 6px rgba(0, 0, 0, 0.1)`
- Header: `0 4px 6px rgba(0, 0, 0, 0.1)`

---

## 🎨 Typography

```
Header Title:        2rem (32px), Bold, White
Subtitle:            1rem (16px), Regular, White
Section Headings:    1.25rem (20px), Bold, Dark Gray
Body Text:           0.875rem (14px), Regular, Dark Gray
Small Text:          0.75rem (12px), Regular, Light Gray
Code/Monospace:      Courier New, 0.8125rem (13px)
```

---

## 📋 Example Tag Row HTML Structure

```html
<div class="tag-row tag-mandatory" style="padding-left: 40px">
  <div class="tag-row-content">
    <div class="col-checkbox">
      <button class="expand-btn">▼</button>
      <input type="checkbox" checked disabled />
    </div>
    <div class="col-index">2.1</div>
    <div class="col-xmltag">
      <span class="xml-tag-name">PmtId</span>
    </div>
    <div class="col-element">Payment Identification</div>
    <div class="col-occurrence">[1..1]</div>
    <div class="col-length">-</div>
    <div class="col-type">
      <span class="type-badge tag-mandatory">M</span>
    </div>
  </div>
</div>
```

---

This visual guide provides a complete reference for the UI design and interaction patterns used in the Galaxy RTP Validator application.

