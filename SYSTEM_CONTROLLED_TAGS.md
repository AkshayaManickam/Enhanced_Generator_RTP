# System-Controlled Tag Behavior

## Overview
This document describes the finalized behavior of mandatory and conditional tags in the PACS.008 Message Generator.

## Tag Types and Behavior

### 1. Mandatory Tags (M)
**System Controlled - Auto-Selected**

- ✅ **Auto-selected by system** when the application loads
- ❌ **Cannot be deselected** by the user at any time
- 🔒 **Checkboxes are disabled** (grayed out)
- 🎯 **Always included** in XML generation

**Visual Indicators:**
- Checkbox is disabled with reduced opacity (0.4)
- Blue left border (`#0d6efd`)
- Light blue background (rgba(13, 110, 253, 0.02))
- Tooltip: "Mandatory field - Auto-selected by system (Cannot be deselected)"

**Examples:**
- `GrpHdr` (1.0) - Group Header
- `MsgId` (1.1) - Message Identification
- `CreDtTm` (1.2) - Creation Date Time
- `NbOfTxs` (1.4) - Number Of Transactions
- `CdtTrfTxInf` (2.0) - Credit Transfer Transaction Information
- `PmtId` (2.1) - Payment Identification

### 2. Optional Tags (O)
**User Controlled - Selectable**

- ✅ **Can be selected/deselected** by the user
- 🎯 **Only these tags** can be manually toggled
- 📦 **Selecting an optional tag** auto-selects all its mandatory children
- 📤 **Deselecting an optional tag** deselects all its children (mandatory, optional, conditional)
- 🔢 **Used for 2^n combination generation**

**Visual Indicators:**
- Checkbox is enabled and interactive
- White background
- Tooltip: "Click to select/deselect this optional tag"

**Examples:**
- `UETR` (2.2) - Unique End-to-End Transaction Reference
- `ClrSysRef` (2.3) - Clearing System Reference
- `PrvsInstgAgt1` (2.19) - Previous Instructing Agent 1
- `UltmtDbtr` (2.10) - Ultimate Debtor
- `UltmtCdtr` (2.16) - Ultimate Creditor

### 3. Conditional Tags (C)
**System Blocked - Not Supported**

- ❌ **Cannot be selected** by the user
- 🚫 **Completely blocked** from interaction
- 🔒 **Checkboxes are disabled** (grayed out)
- 👁️ **Visually distinct** with gray styling
- 🎯 **Never included** in XML generation

**Visual Indicators:**
- Checkbox is disabled with reduced opacity (0.3)
- Gray left border (`#adb5bd`)
- Gray background (`#f8f9fa`)
- Entire row has `pointer-events: none` (no interaction possible)
- Reduced row opacity (0.6)
- Tooltip: "Conditional field - Blocked by system (Cannot be selected)"
- Cursor shows "not-allowed" icon

**Examples:**
- All tags with type `CONDITIONAL` are blocked

## Parent-Child Relationships

### When Selecting an Optional Tag:
1. The optional tag itself is selected
2. **All mandatory children** are automatically selected (system-controlled)
3. The tag tree **auto-expands** to show children
4. Statistics are updated

### When Deselecting an Optional Tag:
1. The optional tag itself is deselected
2. **All children** (mandatory, optional, conditional) are automatically deselected
3. Statistics are updated

## Technical Implementation

### Frontend (TypeScript)
```typescript
onTagToggle(tag: XmlTag, event: Event): void {
  // MANDATORY tags - System controlled, user cannot deselect
  if (tag.type === TagType.MANDATORY) {
    return; // Silently block
  }

  // CONDITIONAL tags - Completely blocked by system
  if (tag.type === TagType.CONDITIONAL) {
    return; // Silently block
  }

  // Only OPTIONAL tags can be toggled by user
  if (tag.type === TagType.OPTIONAL) {
    tag.selected = !tag.selected;
    
    if (tag.selected) {
      this.selectAllMandatoryChildren(tag);
      this.expandAllChildren(tag);
    } else {
      this.deselectAllChildren(tag);
    }
  }
}
```

### Frontend (HTML)
```html
<input 
  type="checkbox" 
  [checked]="tag.selected"
  [disabled]="tag.type === TagType.MANDATORY || tag.type === TagType.CONDITIONAL"
  (change)="onTagToggle(tag, $event)"
/>
```

### Frontend (SCSS)
```scss
// Mandatory tags
&.mandatory {
  background: rgba(13, 110, 253, 0.02);
  border-left: 3px solid #0d6efd;
  
  .col-checkbox input[type="checkbox"] {
    cursor: not-allowed !important;
  }
}

// Conditional tags
&.conditional {
  background: #f8f9fa;
  border-left: 3px solid #adb5bd;
  opacity: 0.6;
  pointer-events: none !important;
  cursor: not-allowed !important;
}

// All disabled checkboxes
input[type="checkbox"]:disabled {
  opacity: 0.4;
  cursor: not-allowed !important;
  pointer-events: none !important;
}
```

## User Experience

### What Users Can Do:
✅ Select/deselect optional tags
✅ See mandatory tags (auto-selected, cannot modify)
✅ See conditional tags (grayed out, cannot interact)
✅ Generate XML combinations based on optional selections

### What Users Cannot Do:
❌ Deselect mandatory tags
❌ Select conditional tags
❌ Modify system-controlled selections

## ISO 20022 Compliance

This behavior strictly follows ISO 20022 specifications:
- **Mandatory elements** must always be present in the message
- **Optional elements** may be included based on business requirements
- **Conditional elements** are not supported in this implementation

## Color Scheme

- **Microsoft Blue**: `#0d6efd` (Primary color for branding and mandatory tags)
- **White**: `#ffffff` (Background for optional tags)
- **Gray**: `#adb5bd` (Border for conditional tags)
- **Light Gray**: `#f8f9fa` (Background for conditional tags)

---

**Last Updated:** 2025-10-29
**Status:** ✅ Implemented and Active

