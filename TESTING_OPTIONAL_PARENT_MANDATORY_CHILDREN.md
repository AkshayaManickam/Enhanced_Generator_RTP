# Testing Optional Parent with Mandatory Children

## Expected Behavior

When you select an **OPTIONAL parent tag**, only its **MANDATORY children** should be auto-selected. Any **OPTIONAL children** should remain unselected.

## Example Test Cases

### Test Case 1: PrvsInstgAgt1 (Previous Instructing Agent 1)
**Index:** 2.104  
**Type:** OPTIONAL (O)

**Children:**
- `FinInstnId` (2.104.1) - **MANDATORY (M)** ✅ Should auto-select
  - `BICFI` (2.104.2) - **CONDITIONAL (C)** 🚫 Blocked
  - `ClrSysMmbId` (2.104.3) - **MANDATORY (M)** ✅ Should auto-select

**Expected Result:**
1. Click checkbox for `PrvsInstgAgt1` → It becomes selected
2. Console shows: "Toggling OPTIONAL tag"
3. Console shows: "✅ Auto-selecting MANDATORY child: FinInstnId (2.104.1)"
4. Console shows: "✅ Auto-selecting MANDATORY child: ClrSysMmbId (2.104.3)"
5. Console shows: "🚫 Skipping CONDITIONAL child: BICFI (2.104.2)"
6. `FinInstnId` checkbox becomes checked ✅
7. `ClrSysMmbId` checkbox becomes checked ✅
8. `BICFI` checkbox remains unchecked and grayed out 🚫

### Test Case 2: PrvsInstgAgt1Acct (Previous Instructing Agent 1 Account)
**Index:** 2.168  
**Type:** OPTIONAL (O)

**Children:**
- `Id` (2.168.1) - **MANDATORY (M)** ✅ Should auto-select
  - `IBAN` (2.168.2) - **CONDITIONAL (C)** 🚫 Blocked
  - `Othr` (2.168.3) - **CONDITIONAL (C)** 🚫 Blocked

**Expected Result:**
1. Click checkbox for `PrvsInstgAgt1Acct` → It becomes selected
2. Console shows: "Toggling OPTIONAL tag"
3. Console shows: "✅ Auto-selecting MANDATORY child: Id (2.168.1)"
4. Console shows: "🚫 Skipping CONDITIONAL child: IBAN (2.168.2)"
5. Console shows: "🚫 Skipping CONDITIONAL child: Othr (2.168.3)"
6. `Id` checkbox becomes checked ✅
7. `IBAN` checkbox remains unchecked and grayed out 🚫
8. `Othr` checkbox remains unchecked and grayed out 🚫

### Test Case 3: Deselecting Optional Parent

**Initial State:**
- `PrvsInstgAgt1` is SELECTED
- Its mandatory child `FinInstnId` is SELECTED (auto-selected)
- Its mandatory child `ClrSysMmbId` is SELECTED (auto-selected)

**Action:** Click to deselect `PrvsInstgAgt1`

**Expected Result:**
1. Console shows: "Toggling OPTIONAL tag"
2. Console shows: "Deselecting all children of PrvsInstgAgt1"
3. Console shows: "❌ Deselecting child: FinInstnId (2.104.1) - Type: M"
4. Console shows: "❌ Deselecting child: ClrSysMmbId (2.104.3) - Type: M"
5. `PrvsInstgAgt1` checkbox becomes unchecked
6. `FinInstnId` checkbox becomes unchecked
7. `ClrSysMmbId` checkbox becomes unchecked
8. All descendants are also deselected

## How to Test

1. **Open browser** to `localhost:4200`
2. **Open Console** (F12)
3. **Scroll down** to find `PrvsInstgAgt1` (index 2.104)
4. **Click the checkbox** for `PrvsInstgAgt1`
5. **Observe:**
   - Console logs showing which children are selected/skipped
   - UI showing only mandatory children get checkmarks
   - Conditional children remain grayed out
6. **Click the checkbox again** to deselect
7. **Observe:**
   - Console logs showing all children being deselected
   - UI showing all children lose their checkmarks

## Current Code Implementation

The code in `app.component.ts` already implements this correctly:

```typescript
selectAllMandatoryChildren(tag: XmlTag): void {
  if (tag.children) {
    tag.children.forEach(child => {
      if (child.type === TagType.MANDATORY) {
        // ✅ Only MANDATORY children are selected
        child.selected = true;
        this.selectAllMandatoryChildren(child);
      } else if (child.type === TagType.OPTIONAL) {
        // ⏭️ OPTIONAL children are SKIPPED
        console.log(`  ⏭️  Skipping OPTIONAL child: ${child.xmlTag}`);
      } else if (child.type === TagType.CONDITIONAL) {
        // 🚫 CONDITIONAL children are SKIPPED
        console.log(`  🚫 Skipping CONDITIONAL child: ${child.xmlTag}`);
      }
    });
  }
}
```

## Verification Checklist

- [ ] Refresh browser (Ctrl+F5)
- [ ] Open console (F12)
- [ ] Click an optional tag with mandatory children
- [ ] Console shows "✅ Auto-selecting MANDATORY child" messages
- [ ] Console shows "⏭️ Skipping OPTIONAL child" for optional children
- [ ] Console shows "🚫 Skipping CONDITIONAL child" for conditional children
- [ ] UI shows only mandatory children get checkmarks
- [ ] Optional children remain unchecked
- [ ] Conditional children remain grayed out
- [ ] Deselect the optional parent
- [ ] Console shows "❌ Deselecting child" for all children
- [ ] UI shows all children lose checkmarks

---

**Status:** ✅ Code is correct - Test to verify UI behavior  
**Date:** 2025-10-29

