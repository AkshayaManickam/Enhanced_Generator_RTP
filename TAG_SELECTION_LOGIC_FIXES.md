# Tag Selection Logic - Refinement Summary

## 🎯 Objective Achieved
Fixed and refined the tag selection logic to strictly follow ISO 20022 specifications with proper parent-child relationships.

---

## ✅ Implemented Fixes

### 1. Mandatory Tags Behavior

#### ✓ Top-Level Mandatory Tags
- **Auto-selected by default** when the application loads
- **Cannot be deselected** - disabled checkboxes at root level (level 0)
- Includes all their mandatory descendants
- Visual feedback: Disabled with tooltip explaining they are required for ISO 20022 compliance

#### ✓ Nested Mandatory Tags (Under Optional Parents)
- **NOT auto-selected by default**
- **Only selected when their optional parent is selected**
- Automatically deselected when the optional parent is deselected
- Can be toggled as part of the optional parent's selection
- Visual feedback: Enabled checkboxes with tooltip explaining auto-selection behavior

**Implementation:**
```typescript
// Auto-select top-level mandatory tags on load
autoSelectTopLevelMandatoryTags(tags: XmlTag[]): void {
  tags.forEach(tag => {
    if (tag.type === TagType.MANDATORY) {
      tag.selected = true;
      this.selectAllMandatoryDescendants(tag);
    }
  });
}

// When optional tag is selected, auto-select mandatory children
selectAllMandatoryChildren(tag: XmlTag): void {
  if (tag.children) {
    tag.children.forEach(child => {
      if (child.type === TagType.MANDATORY) {
        child.selected = true;
        this.selectAllMandatoryChildren(child);
      }
    });
  }
}
```

---

### 2. Optional Tags Behavior

#### ✓ User Control
- **Only optional tags are user-selectable**
- Can be manually checked or unchecked
- When selected:
  - All mandatory children are **auto-selected**
  - Tree automatically **expands** to show children
  - Visual confirmation with expanded nodes

#### ✓ Selection Cascade
- When deselecting an optional tag:
  - **ALL children are deselected** (mandatory, optional, conditional)
  - Ensures clean state management
  - Prevents orphaned selections

**Implementation:**
```typescript
onTagToggle(tag: XmlTag, event: Event): void {
  if (tag.type !== TagType.OPTIONAL) {
    return; // Only optional tags can be toggled
  }

  tag.selected = !tag.selected;
  
  if (tag.selected) {
    this.selectAllMandatoryChildren(tag);
    this.expandedNodes.add(tag.index);
    this.expandAllChildren(tag);
  } else {
    this.deselectAllChildren(tag);
  }
}
```

---

### 3. Conditional Tags Behavior

#### ✓ Complete Blocking
- **Completely blocked** from user interaction
- Checkboxes are **always disabled**
- Show visual indication (grayed out, opacity: 0.6)
- Gray badge color (#6c757d) instead of blue
- Clear tooltip: "Conditional tags are blocked and cannot be modified"

#### ✓ Validation
- Before proceeding to Step 2, system checks for any selected conditional tags
- If found, shows error and prevents navigation
- Ensures no conditional tags enter the generation flow

**Implementation:**
```typescript
// Block conditional tags
if (tag.type === TagType.CONDITIONAL) {
  this.showMessage('Conditional tags are blocked and cannot be modified.', 'info');
  return;
}

// Validation before proceeding
const hasConditional = this.checkForConditionalTags(this.tags);
if (hasConditional) {
  this.showMessage('Please deselect all conditional tags before proceeding.', 'error');
  return;
}
```

**Styling:**
```scss
&.conditional {
  background: #f8f9fa;
  border-left: 3px solid #6c757d;
  opacity: 0.6;
  cursor: not-allowed;
}
```

---

### 4. Navigation Flow Fix

#### ✓ Step 1 → Step 2 Validation
- Validates that **at least one optional tag is selected**
- Validates that **no conditional tags are selected**
- Only allows progression when requirements are met
- Clear error messages guide the user

#### ✓ Smooth Flow
- After selecting optional tags → Can proceed to Configure step
- After configuration → Auto-generates combinations in Preview step
- After generation → Can view and download results

**Implementation:**
```typescript
nextStep(): void {
  if (this.currentStep === 1) {
    const selectedOptional = this.getSelectedOptionalTags();
    if (selectedOptional.length === 0) {
      this.showMessage('Please select at least one optional tag to proceed.', 'info');
      return;
    }
    
    const hasConditional = this.checkForConditionalTags(this.tags);
    if (hasConditional) {
      this.showMessage('Please deselect all conditional tags before proceeding.', 'error');
      return;
    }
  }
  
  this.currentStep++;
}
```

---

## 🎨 Visual Enhancements

### Legend Updates
```
M (Blue) - Mandatory (Auto-selected at root level)
O (Light Blue) - Optional (User selectable)
C (Gray) - Conditional (Blocked - not supported)
```

### Tooltip System
- **Top-level mandatory**: "Top-level mandatory tags cannot be deselected"
- **Nested mandatory**: "This mandatory tag is auto-selected with its parent"
- **Conditional**: "Conditional tags are blocked and cannot be modified"
- **Optional**: "Select/deselect this optional tag"

### Visual Feedback
- **Conditional tags**: Grayed out (opacity: 0.6), gray borders, gray badges
- **Disabled checkboxes**: Lower opacity (0.4), not-allowed cursor
- **Hover effects**: Scale transform on enabled checkboxes
- **Auto-expansion**: Tree expands when optional tags are selected

### Information Note
Added explanatory note in legend:
> **Note:** Mandatory tags nested under optional parents are only selected when the optional parent is selected.

---

## 📊 Behavior Matrix

| Tag Type | Location | Auto-Selected | User Can Toggle | Behavior |
|----------|----------|---------------|-----------------|----------|
| **Mandatory** | Root Level | ✅ Yes | ❌ No | Always selected, disabled checkbox |
| **Mandatory** | Under Optional | ❌ No | ❌ No | Only selected when parent selected |
| **Mandatory** | Under Conditional | ❌ No | ❌ No | Never selected (parent blocked) |
| **Optional** | Any | ❌ No | ✅ Yes | User selectable, auto-selects mandatory children |
| **Conditional** | Any | ❌ No | ❌ No | Completely blocked, grayed out |

---

## 🔄 Selection Flow Examples

### Example 1: Selecting Optional Tag with Mandatory Children

**Before Selection:**
```
□ 2.0 CdtTrfTxInf (Optional) - NOT SELECTED
  □ 2.1 PmtId (Mandatory) - NOT SELECTED
    □ 2.2 InstrId (Mandatory) - NOT SELECTED
```

**User clicks CdtTrfTxInf checkbox:**
```
☑ 2.0 CdtTrfTxInf (Optional) - SELECTED BY USER
  ☑ 2.1 PmtId (Mandatory) - AUTO-SELECTED
    ☑ 2.2 InstrId (Mandatory) - AUTO-SELECTED
  
✓ Tree auto-expands to show children
✓ All mandatory descendants selected
```

**User unchecks CdtTrfTxInf:**
```
□ 2.0 CdtTrfTxInf (Optional) - DESELECTED
  □ 2.1 PmtId (Mandatory) - AUTO-DESELECTED
    □ 2.2 InstrId (Mandatory) - AUTO-DESELECTED

✓ All children deselected
```

---

### Example 2: Top-Level Mandatory Tag

**On Page Load:**
```
☑ 1.0 FIToFICstmrCdtTrf (Mandatory) - AUTO-SELECTED
  ☑ 1.0 GrpHdr (Mandatory) - AUTO-SELECTED
    ☑ 1.1 MsgId (Mandatory) - AUTO-SELECTED

✓ All top-level mandatory tags pre-selected
✓ Checkboxes are disabled (cannot deselect)
✓ Tooltip explains they are required for ISO 20022
```

---

### Example 3: Conditional Tags

**Always:**
```
☒ 2.1244 InstrForCdtrAgt (Conditional) - BLOCKED
  ☒ 2.1245 Cd (Conditional) - BLOCKED

✓ Grayed out appearance (opacity 0.6)
✓ Gray badge instead of blue
✓ Cannot be clicked
✓ Tooltip: "Blocked - not supported"
```

---

## 🔧 Technical Implementation Details

### Component Changes
- `loadTags()` - Now calls `autoSelectTopLevelMandatoryTags()`
- `onTagToggle()` - Complete rewrite with proper blocking and validation
- `selectAllMandatoryChildren()` - New method for recursive mandatory selection
- `deselectAllChildren()` - Updated to deselect ALL children types
- `nextStep()` - Added validation for optional tag requirement and conditional blocking
- `resetWizard()` - Auto-selects mandatory tags on reset

### HTML Changes
- Checkbox `[disabled]` logic updated to check level and type
- Enhanced tooltip messages with specific context
- Updated step description text
- Enhanced legend with explanatory note

### SCSS Changes
- Conditional tag styling (gray, opacity, cursor)
- Disabled checkbox styling (lower opacity)
- Hover effects on enabled checkboxes only
- Legend note styling with blue accent
- Gray badge for conditional types

---

## ✅ Testing Checklist

### Mandatory Tags
- [x] Top-level mandatory tags auto-selected on load
- [x] Top-level mandatory tags cannot be deselected
- [x] Mandatory tags under optional parents not selected initially
- [x] Mandatory children auto-select when optional parent selected
- [x] Mandatory children deselect when optional parent deselected

### Optional Tags
- [x] Optional tags can be manually toggled
- [x] Selecting optional tag auto-selects mandatory children
- [x] Selecting optional tag expands the tree
- [x] Deselecting optional tag deselects all children
- [x] At least one optional tag required to proceed to Step 2

### Conditional Tags
- [x] Conditional tags are completely blocked
- [x] Checkboxes are disabled for conditional tags
- [x] Visual feedback shows they are blocked (gray, opacity)
- [x] Validation prevents proceeding with selected conditional tags
- [x] Tooltips explain they are not supported

### Navigation
- [x] Cannot proceed to Step 2 without optional tags
- [x] Cannot proceed to Step 2 with conditional tags selected
- [x] Can proceed smoothly after proper selection
- [x] Reset properly reinitializes mandatory selections

---

## 🎯 Key Improvements

1. **ISO 20022 Compliance**: Strict adherence to mandatory/optional/conditional rules
2. **Smart Auto-Selection**: Mandatory children only selected when parent is selected
3. **Clear Visual Feedback**: Color-coding, opacity, tooltips guide users
4. **Proper Validation**: Prevents invalid states from entering generation flow
5. **Intuitive UX**: Auto-expansion, clear messages, guided workflow
6. **Complete Blocking**: Conditional tags fully blocked as requested
7. **Fixed Navigation**: Smooth flow from selection → configuration → generation

---

## 📱 User Experience Flow

1. **User arrives at Step 1**
   - Top-level mandatory tags are pre-selected (blue checkboxes, disabled)
   - Optional tags are unchecked (enabled checkboxes)
   - Conditional tags are grayed out (disabled, gray badges)

2. **User selects optional tags**
   - Clicks checkbox on optional tag
   - Mandatory children automatically check
   - Tree expands to show what was auto-selected
   - Statistics update in real-time

3. **User proceeds to Step 2**
   - System validates at least one optional tag selected
   - System checks no conditional tags selected
   - If valid, proceeds to configuration step
   - If invalid, shows helpful error message

4. **Configuration and Generation**
   - User reviews selection
   - Sees 2^n combination count
   - Proceeds to generate XML combinations
   - Downloads results

---

**✅ All Requirements Met!**

The tag selection logic now properly implements ISO 20022 specifications with intelligent parent-child relationships and a smooth, guided user experience.

