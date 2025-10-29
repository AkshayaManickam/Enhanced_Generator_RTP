# 📊 Galaxy RTP Validator - Tag Count Details

## Complete Tag Statistics

Based on the FIToFICustomer Credit Transfer V08 schema implementation:

### 📈 Summary Counts

| Category | Count | Description |
|----------|-------|-------------|
| **Total Tags** | 200+ | All tags including nested levels |
| **Mandatory Tags (M)** | ~60 | Auto-selected, cannot be deselected |
| **Optional Tags (O)** | ~120 | User can select/deselect |
| **Conditional Tags (C)** | ~20 | Blocked with warning message |

---

## 📋 Detailed Tag Breakdown by Section

### 1.0 - Group Header Section (GrpHdr)
- **Total**: 11 tags
- **Mandatory**: 8 tags
- **Optional**: 0 tags
- **Conditional**: 3 tags

**Tags Include**:
- MsgId, CreDtTm, NbOfTxs (Mandatory)
- TtlIntrBkSttlmAmt, Ccy (Mandatory)
- IntrBkSttlmDt, SttlmInf (Mandatory)
- Settlement Method, Clearing System (Mandatory)

---

### 2.0 - Credit Transfer Transaction Information (CdtTrfTxInf)

#### 2.1 - Payment Identification (PmtId)
- **Total**: 5 tags
- **Mandatory**: 3 tags (InstrId, EndToEndId, TxId)
- **Optional**: 2 tags (UETR, ClrSysRef)

#### 2.7 - Payment Type Information (PmtTpInf)
- **Total**: 6 tags
- **Mandatory**: 6 tags
- Service Level, Local Instrument, Category Purpose

#### 2.19 - Interbank Settlement Amount (IntrBkSttlmAmt)
- **Total**: 2 tags
- **Mandatory**: 2 tags (Amount + Currency)

#### 2.36 - Charge Bearer (ChrgBr)
- **Total**: 1 tag
- **Mandatory**: 1 tag

---

### Agent Sections

#### Previous Instructing Agents (2.104, 2.187, 2.270)
Each agent has 4-5 nested tags:
- **Total per agent**: 5 tags
- **3 agents** = 15 tags
- **Type**: All Optional
- Includes: FinInstnId, BICFI (C), ClrSysMmbId (C), MmbId (M)

#### Instructing Agent (2.353) & Instructed Agent (2.417)
Each agent has 4-5 nested tags:
- **Total per agent**: 5 tags
- **2 agents** = 10 tags
- **Type**: Mandatory parent, mixed children
- Includes: FinInstnId, BICFI (C), ClrSysMmbId (C), MmbId (M)

#### Intermediary Agents (2.481, 2.564, 2.647)
Each agent has 4-5 nested tags:
- **Total per agent**: 5 tags
- **3 agents** = 15 tags
- **Type**: All Optional
- Includes: FinInstnId, BICFI (C), ClrSysMmbId (C), MmbId (M)

**Agent Sections Total**: ~40 tags

---

### Account Sections

Each Account (DbtrAcct, CdtrAcct, Agent Accounts, etc.) has 3-4 tags:
- Id, IBAN (C), Othr (C), Id under Othr (M)

**Account Sections**:
- PrvsInstgAgt1Acct (2.168) - Optional
- PrvsInstgAgt2Acct (2.251) - Optional  
- PrvsInstgAgt3Acct (2.334) - Optional
- IntrmyAgt1Acct (2.545) - Optional
- IntrmyAgt2Acct (2.628) - Optional
- IntrmyAgt3Acct (2.647) - Optional
- DbtrAcct (2.916) - Mandatory
- DbtrAgtAcct (2.999) - Optional
- CdtrAgtAcct (2.1082) - Optional
- CdtrAcct (2.1163) - Mandatory

**Total**: ~30-40 tags

---

### Party Sections

#### Ultimate Debtor (2.730) - Optional
- **Total**: ~15 tags
- Includes: Name, Postal Address (street, building, post code, town, country, etc.), Id

#### Initiating Party (2.792) - Conditional
- **Total**: ~12 tags
- Includes: Name, Id, OrgId, Othr, Id, SchmeNm, Prtry

#### Debtor (2.854) - Mandatory
- **Total**: ~20 tags
- Includes: Name, Postal Address, Id with OrgId/PrvtId options
- OrgId: LEI
- PrvtId: DtAndPlcOfBirth (BirthDt, CityOfBirth, CtryOfBirth)

#### Debtor Agent (2.935) - Mandatory
- **Total**: 5 tags
- Includes: FinInstnId, BICFI (C), ClrSysMmbId (C), MmbId (M)

#### Creditor Agent (2.1018) - Mandatory
- **Total**: 5 tags
- Similar structure to Debtor Agent

#### Creditor (2.1101) - Mandatory
- **Total**: ~20 tags
- Similar structure to Debtor

#### Ultimate Creditor (2.1182) - Optional
- **Total**: ~15 tags
- Similar structure to Ultimate Debtor

**Party Sections Total**: ~90 tags

---

### Additional Information Sections

#### Instruction For Creditor Agent (2.1244) - Conditional
- **Total**: 2 tags
- Cd (C), InstrInf (M)

#### Purpose (2.1250) - Optional
- **Total**: 2 tags
- Cd (C) OR Prtry (C)

#### Related Remittance Info (2.1316) - Conditional
- **Total**: 4 tags
- RmtId (C), RmtLctnDtls (O), Mtd (M), ElctrnccAdr (C)

#### Remittance Information (2.1345) - Optional
- **Total**: 3+ tags
- Ustrd (O), Strd (O), RfrdDocInf (C)

**Additional Sections Total**: ~15 tags

---

## 🎯 Complete Statistics Summary

```
┌─────────────────────────────────────────┐
│     GALAXY RTP VALIDATOR STATISTICS     │
├─────────────────────────────────────────┤
│                                         │
│  📊 Total Tags:        ~200-220 tags    │
│                                         │
│  ✅ Mandatory (M):     ~60-70 tags      │
│     • Always selected                   │
│     • Cannot be unchecked               │
│     • Auto-included in XML              │
│                                         │
│  🔵 Optional (O):      ~120-130 tags    │
│     • User selectable                   │
│     • Included only when selected       │
│     • Default: unselected               │
│                                         │
│  ⚠️  Conditional (C):  ~20-25 tags      │
│     • Blocked in this version           │
│     • Shows warning message             │
│     • Cannot be selected                │
│                                         │
└─────────────────────────────────────────┘
```

---

## 🔍 How Counts Are Displayed in UI

The application provides **real-time statistics** in the left sidebar:

1. **Total Tags**: Live count of all tags in the system
2. **Mandatory Tags**: Count of all M-type tags
3. **Optional Tags**: Count of all O-type tags  
4. **Conditional Tags**: Count of all C-type tags
5. **Selected Tags**: Current count of selected tags (updates as user selects/deselects)

### Example Display:

```
┌──────────────────────┐
│  Tag Statistics      │
├──────────────────────┤
│  Total Tags     215  │
│  Mandatory       62  │
│  Optional       128  │
│  Conditional     25  │
│  Selected        62  │  ← Initially = Mandatory count
└──────────────────────┘
```

After user selects some optional tags:

```
┌──────────────────────┐
│  Tag Statistics      │
├──────────────────────┤
│  Total Tags     215  │
│  Mandatory       62  │
│  Optional       128  │
│  Conditional     25  │
│  Selected        85  │  ← Increased to 62 + 23 selected optional
└──────────────────────┘
```

---

## 📱 Visual Indicators in UI

### Color Coding:
- 🟢 **Green Border**: Mandatory tags
- 🔵 **Blue Border**: Optional tags
- 🟡 **Yellow Border**: Conditional tags

### Badges:
- **M** badge in green: Mandatory
- **O** badge in blue: Optional
- **C** badge in yellow: Conditional

### Checkboxes:
- ✅ **Checked + Disabled**: Mandatory (always selected)
- ☑️ **Checked + Enabled**: Optional (user selected)
- ☐ **Unchecked + Enabled**: Optional (not selected)
- ⛔ **Unchecked + Warning**: Conditional (blocked)

---

## 🎨 Statistics Panel Features

The statistics panel includes:

1. **Live Updates**: Counts update in real-time as you select/deselect tags
2. **Visual Cards**: Each stat type has its own colored card
3. **Legend**: Shows what M/O/C means
4. **Sticky Position**: Panel stays visible as you scroll
5. **Responsive**: Adapts to mobile/tablet/desktop views

---

**Note**: Exact counts are calculated dynamically by the backend based on the complete tag hierarchy. The UI displays these counts in real-time.

