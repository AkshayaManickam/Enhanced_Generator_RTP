# Galaxy RTP Validator - Quick Start Guide

## 🚀 Quick Start (Windows)

**Option 1: Using the startup script (Recommended)**
1. Double-click `start-application.bat`
2. Wait for both backend and frontend to start
3. Browser will open automatically at `http://localhost:4200`

**Option 2: Manual startup**

Terminal 1 - Backend:
```cmd
cd galaxy-rtp-validator-service
mvn spring-boot:run
```

Terminal 2 - Frontend:
```cmd
cd galaxy-rtp-validator-ui
npm install
npm start
```

## 🚀 Quick Start (Linux/Mac)

**Option 1: Using the startup script (Recommended)**
```bash
chmod +x start-application.sh
./start-application.sh
```

**Option 2: Manual startup**

Terminal 1 - Backend:
```bash
cd galaxy-rtp-validator-service
mvn spring-boot:run
```

Terminal 2 - Frontend:
```bash
cd galaxy-rtp-validator-ui
npm install
npm start
```

## 📊 Tag Count Summary

Based on the FIToFICustomer Credit Transfer V08 schema, the application includes:

### Approximate Tag Counts:
- **Total Tags**: ~200+ tags (including all nested levels)
- **Mandatory Tags**: ~50+ tags (M) - Always selected
- **Optional Tags**: ~130+ tags (O) - User selectable
- **Conditional Tags**: ~20+ tags (C) - Blocked in this version

*Note: Exact counts are displayed in real-time in the UI statistics panel*

## ✅ Features Implemented

### ✅ Backend Features
- [x] Complete tag hierarchy from your images (all levels)
- [x] Tag types: Mandatory (M), Optional (O), Conditional (C)
- [x] REST API for tag management
- [x] Search functionality
- [x] Statistics calculation
- [x] Tag selection management
- [x] XML generation endpoint

### ✅ Frontend Features
- [x] Modern, responsive UI
- [x] Hierarchical tree view with expand/collapse
- [x] Color-coded tag types:
  - 🟢 Green border: Mandatory tags
  - 🔵 Blue border: Optional tags
  - 🟡 Yellow border: Conditional tags
- [x] Real-time search (by index, XML tag, or element name)
- [x] Statistics dashboard with live counts
- [x] Auto-selection of mandatory tags
- [x] Conditional tag blocking with message
- [x] Visual feedback for all operations
- [x] Responsive design for mobile/tablet/desktop

## 🎯 Tag Structure Implemented

The application includes the complete structure from your images:

### Level 1: Root
- FIToFICstmrCdtTrf (1.0)
  - Group Header (1.0)
  - Credit Transfer Transaction Information (2.0)

### Level 2: Major Sections
- **Payment Identification** (2.1)
- **Payment Type Information** (2.7)
- **Interbank Settlement Amount** (2.19)
- **Previous Instructing Agents** (2.104, 2.187, 2.270)
- **Instructing/Instructed Agents** (2.353, 2.417)
- **Intermediary Agents** (2.481, 2.564, 2.647)
- **Debtor Information** (2.854, 2.916, 2.935)
- **Creditor Information** (2.1018, 2.1101, 2.1163)
- **Ultimate Parties** (2.730, 2.1182)
- **Additional Information** (2.1244, 2.1250, 2.1316, 2.1345)

### Level 3+: Nested Elements
All nested elements including:
- Financial Institution Identifications
- Account Identifications (IBAN/Other)
- Postal Addresses
- Organisation/Private Identifications
- And many more...

## 🔍 How to Use

1. **Browse Tags**: Expand nodes to see nested structure
2. **Search**: Type in search box to filter tags
3. **Select Tags**: 
   - Mandatory tags (M): Already selected, cannot uncheck
   - Optional tags (O): Click checkbox to select/deselect
   - Conditional tags (C): Will show error message if clicked
4. **View Stats**: Left sidebar shows real-time counts
5. **Save**: Click "Save Selection" to persist
6. **Generate XML**: Click "Generate XML" (validates no conditional tags selected)
7. **Reset**: Click "Reset" to restore defaults

## 🔒 Tag Rules

1. **Mandatory Tags**: 
   - Auto-selected on load
   - Checkbox disabled
   - Cannot be deselected
   - Always included in XML

2. **Optional Tags**:
   - Not selected by default
   - User can toggle on/off
   - Included in XML only when selected

3. **Conditional Tags**:
   - Shows warning: "Conditional tags are not allowed for XML generation Limited in this version"
   - Cannot be selected
   - Blocked from XML generation

## 📱 Browser Support

- Chrome/Edge (recommended)
- Firefox
- Safari
- Opera

## 🎨 UI Highlights

- Clean, modern design
- Color-coded tag types for easy identification
- Expandable tree structure
- Real-time search with filtering
- Responsive layout (works on all screen sizes)
- Visual feedback for all actions
- Smooth animations and transitions

## 📞 Support

For issues or questions:
1. Check the logs in terminal windows
2. Verify backend is running on port 8080
3. Verify frontend is running on port 4200
4. Check browser console for errors

---

**Enjoy using Galaxy RTP Validator!** 🚀

