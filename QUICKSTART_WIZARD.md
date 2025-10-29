# 🚀 Quick Start Guide - XML Message Generator

## Prerequisites
- Java 17 or higher
- Node.js 18+ and npm
- Maven 3.6+

## 1. Start the Backend

```bash
cd galaxy-rtp-validator-service
mvn clean install
mvn spring-boot:run
```

Backend will start on `http://localhost:8080`

## 2. Start the Frontend

```bash
cd galaxy-rtp-validator-ui
npm install
npm start
```

Frontend will start on `http://localhost:4200`

## 3. Using the Application

### Step 1: Select Tags
1. Browse the hierarchical tag tree
2. Select optional tags you want to include
3. **Automatic behavior:** When you select an optional tag, its mandatory children are auto-selected
4. Use search to find specific tags
5. View real-time statistics in the sidebar
6. Click "Next: Configure →"

### Step 2: Configure
1. Review your selected optional tags
2. See the total number of combinations (2^n where n = number of selected optional tags)
3. View warnings if you selected many tags (>10)
4. Click "Next: Generate →"

### Step 3: Generate
1. System automatically generates all XML combinations
2. View generation progress
3. See result summary with generation time
4. Browse through generated combinations in grid view
5. Click "Preview" to see XML content
6. Click "Download" to save individual files
7. Click "Next: View Results →"

### Step 4: Results
1. View summary statistics
2. Click "Download All" to get all XML files at once
3. Browse combinations in the sidebar
4. Click any combination to preview its full XML content
5. Download individual files from the preview panel
6. Click "Start Over" to create a new message set

## 📊 Example Usage

### Example 1: Simple Selection
- Select 2 optional tags: **UETR** and **ClrSysRef**
- Result: **4 combinations** (2²)
  1. Base message (no optional tags)
  2. Message with UETR only
  3. Message with ClrSysRef only
  4. Message with both tags

### Example 2: Complex Selection
- Select 3 optional tags: **PrvsInstgAgt1**, **UETR**, **Purpose**
- Result: **8 combinations** (2³)
- PrvsInstgAgt1's mandatory children are automatically included when it's selected

## 🎯 Key Features

### ✅ Automatic Mandatory Child Selection
When you select an optional tag that has mandatory children, they are automatically included in the XML generation.

**Example:**
```
✓ PrvsInstgAgt1 (Optional - YOU SELECT THIS)
  ✓ FinInstnId (Mandatory - AUTO-SELECTED)
    ✓ ClrSysMmbId (Mandatory - AUTO-SELECTED)
      ✓ MmbId (Mandatory - AUTO-SELECTED)
```

### 🔍 Search Functionality
- Search by XML tag name, element name, or index
- Results highlight matching tags
- Clear button to reset search

### 📈 Real-time Statistics
- Total tags count
- Mandatory, Optional, Conditional breakdown
- Selected tags count
- Visual color-coding

### 💾 Download Options
- Download individual XML files
- Batch download all combinations
- Files named: `pacs008_combination_[number].xml`

## 🎨 UI Legend

| Badge | Type | Behavior |
|-------|------|----------|
| **M** (Blue) | Mandatory | Always selected, cannot be deselected |
| **O** (Light Blue) | Optional | User can select/deselect |
| **C** (Lightest Blue) | Conditional | Auto-included based on rules |

## ⚠️ Important Notes

1. **Mandatory Tags:** Always included in every combination, shown pre-selected and disabled
2. **Conditional Tags:** Not currently supported for XML generation
3. **Performance:** Generation time increases with more optional tags:
   - 5 tags = 32 combinations (~100ms)
   - 10 tags = 1,024 combinations (~500ms)
   - 15 tags = 32,768 combinations (~5s)

## 🐛 Troubleshooting

### Backend won't start
```bash
# Check Java version
java -version  # Should be 17+

# Clean and rebuild
mvn clean install
```

### Frontend won't start
```bash
# Clear node modules
rm -rf node_modules package-lock.json
npm install
npm start
```

### Port already in use
```bash
# Backend (8080)
lsof -i :8080
kill -9 <PID>

# Frontend (4200)
lsof -i :4200
kill -9 <PID>
```

### CORS errors
Make sure backend is running on `localhost:8080` and frontend on `localhost:4200`. The backend is configured to allow all origins.

## 📝 API Testing (Optional)

Test the API directly using curl or Postman:

```bash
# Get all tags
curl http://localhost:8080/api/tags

# Get statistics
curl http://localhost:8080/api/tags/statistics

# Search tags
curl http://localhost:8080/api/tags/search?query=UETR

# Generate XML combinations
curl -X POST http://localhost:8080/api/tags/generate-xml-combinations \
  -H "Content-Type: application/json" \
  -d '{"selectedTagIndices":["2.5","2.6"]}'
```

## 🎉 Success!

Your XML Message Generator is now running! Navigate to `http://localhost:4200` and start creating ISO 20022 compliant pacs.008 messages.

---

**Need Help?** Check `IMPLEMENTATION_COMPLETE.md` for detailed technical documentation.

