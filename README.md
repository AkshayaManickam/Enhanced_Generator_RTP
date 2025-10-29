# Galaxy RTP Validator - XML Tag Selector

A modern web application for selecting and validating XML tags for FIToFICustomer Credit Transfer V08 (ISO 20022).

## 🚀 Features

### Backend (Java Spring Boot)
- ✅ RESTful API for tag management
- ✅ Hierarchical tag structure with nested elements
- ✅ Tag classification: Mandatory, Optional, Conditional
- ✅ Tag statistics and analytics
- ✅ Search functionality
- ✅ Auto-selection of mandatory tags
- ✅ Validation for conditional tags

### Frontend (Angular 19)
- ✅ Modern, responsive UI with beautiful design
- ✅ Hierarchical tree view with expand/collapse
- ✅ Real-time search across all tag properties
- ✅ Visual indicators for tag types (M/O/C)
- ✅ Tag statistics dashboard
- ✅ Auto-selection of mandatory fields
- ✅ Conditional tag blocking with user-friendly messages
- ✅ Persistent selection state
- ✅ XML generation capability

## 📊 Tag Statistics Display

The UI displays the following counts:
- **Total Tags**: {{ statistics.totalTags }} tags in the system
- **Mandatory Tags**: {{ statistics.mandatoryTags }} (always selected, cannot be deselected)
- **Optional Tags**: {{ statistics.optionalTags }} (user can select/deselect)
- **Conditional Tags**: {{ statistics.conditionalTags }} (blocked with message: "Conditional tags are not allowed for XML generation Limited in this version")
- **Selected Tags**: {{ statistics.selectedTags }} (currently selected count)

## 🛠️ Technology Stack

### Backend
- Java 17+
- Spring Boot 3.x
- Maven
- Lombok
- Jackson (JSON/XML processing)

### Frontend
- Angular 19.2.17
- TypeScript
- SCSS
- RxJS
- HttpClient

## 📋 Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.6 or higher
- **Node.js**: 20.x or higher
- **npm**: 10.x or higher
- **Angular CLI**: 19.x or higher

## 🚀 Getting Started

### 1. Backend Setup

```bash
# Navigate to the service directory
cd galaxy-rtp-validator-service

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 2. Frontend Setup

```bash
# Navigate to the UI directory
cd galaxy-rtp-validator-ui

# Install dependencies
npm install

# Start the development server
npm start
```

The frontend will start on `http://localhost:4200`

### 3. Access the Application

Open your browser and navigate to: `http://localhost:4200`

## 🔗 API Endpoints

### Tag Management
- `GET /api/tags` - Get all tags
- `GET /api/tags/search?query={searchTerm}` - Search tags
- `GET /api/tags/statistics` - Get tag statistics
- `POST /api/tags/selection` - Update tag selection
- `POST /api/tags/generate-xml` - Generate XML from selected tags

## 📁 Project Structure

```
galaxy-rtp-validator/
├── galaxy-rtp-validator-service/          # Java Spring Boot Backend
│   └── src/main/java/com/finzly/galaxy/rtp/validator/
│       ├── controller/                    # REST Controllers
│       ├── service/                       # Business Logic
│       ├── model/                         # Domain Models
│       └── dto/                           # Data Transfer Objects
│
├── galaxy-rtp-validator-client/           # Java Client Library
│
├── galaxy-rtp-validator-ui/               # Angular Frontend
│   └── src/app/
│       ├── models/                        # TypeScript Models
│       ├── services/                      # Angular Services
│       ├── app.component.ts               # Main Component
│       ├── app.component.html             # Main Template
│       └── app.component.scss             # Styles
│
└── README.md
```

## 🎨 UI Features

### Tag Tree View
- Hierarchical display with visual indentation
- Expand/collapse nodes with arrow buttons
- Color-coded by type:
  - 🟢 **Green**: Mandatory tags
  - 🔵 **Blue**: Optional tags
  - 🟡 **Yellow**: Conditional tags

### Search Functionality
- Search by XML tag name
- Search by element name
- Search by index number
- Real-time filtering

### Tag Selection
- Checkboxes for tag selection
- Auto-selected mandatory tags (disabled checkboxes)
- Conditional tags show warning when clicked
- Parent-child selection propagation for optional tags

### Statistics Dashboard
- Real-time count updates
- Visual color-coded stats
- Legend for tag types

## 🔒 Tag Rules

1. **Mandatory Tags (M)**
   - Always selected
   - Cannot be deselected
   - Automatically included in XML generation

2. **Optional Tags (O)**
   - Can be selected/deselected by user
   - Not included in XML by default
   - Included only when explicitly selected

3. **Conditional Tags (C)**
   - Cannot be selected in this version
   - Shows message: "Conditional tags are not allowed for XML generation Limited in this version"
   - Blocked from XML generation

## 📝 Usage

1. **Browse Tags**: The tree view displays all available tags with their properties
2. **Search**: Use the search bar to quickly find specific tags
3. **Select Tags**: Click checkboxes to select optional tags
4. **View Statistics**: Check the left sidebar for real-time statistics
5. **Save Selection**: Click "Save Selection" to persist your choices
6. **Generate XML**: Click "Generate XML" to create XML from selected tags
7. **Reset**: Click "Reset" to restore default selections (all mandatory tags)

## 🐛 Troubleshooting

### Backend won't start
- Ensure Java 17+ is installed: `java -version`
- Check if port 8080 is available
- Review logs for errors

### Frontend won't start
- Ensure Node.js is installed: `node -version`
   - Clear npm cache: `npm cache clean --force`
- Delete `node_modules` and reinstall: `rm -rf node_modules && npm install`

### CORS Issues
- Backend has CORS enabled for all origins during development
- For production, configure specific allowed origins in the controller

## 🔄 Future Enhancements

- [ ] XML validation
- [ ] Export selected tags to Excel
- [ ] Import tag selections from file
- [ ] User authentication and authorization
- [ ] Save multiple tag selection profiles
- [ ] Support for other ISO 20022 message types
- [ ] Conditional tag support (future version)

## 📄 License

Copyright © 2025 Finzly. All rights reserved.

## 👥 Support

For issues or questions, please contact the development team.

---

**Built with ❤️ using Java Spring Boot and Angular**
