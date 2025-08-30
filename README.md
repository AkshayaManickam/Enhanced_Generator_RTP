# Galaxy RTP Validator

A comprehensive enterprise-grade tool for generating and validating ISO 20022 RTP (Real-Time Payments) messages. This application provides both a robust backend API service and a modern Angular frontend interface, designed for financial institutions and payment processors.

## 🚀 Technology Stack

### **Backend Stack (Java/Spring Boot)**
- **Java 21** (OpenJDK 21) - Latest LTS version
- **Spring Boot** - Main application framework
- **Spring Security** - Authentication and authorization
- **Spring Cloud** - Microservices support (Config, OpenFeign)
- **MySQL** - Primary database
- **H2 Database** - In-memory database for testing
- **JPA/Hibernate** - ORM framework
- **Javers** - Database auditing and versioning
- **Spring Web MVC** - REST API framework
- **SpringDoc OpenAPI** - API documentation (Swagger)
- **Jackson** - JSON/XML serialization/deserialization
- **AWS SDK** - Cloud integration (SQS, S3, Parameter Store)
- **Lombok** - Code generation and boilerplate reduction
- **MapStruct** - Object mapping
- **JUnit 4** - Unit testing
- **Mockito** - Mocking framework
- **JaCoCo** - Code coverage
- **Maven** - Build tool and dependency management

### **Frontend Stack (Angular 19)**
- **Angular 19.2.0** - Latest version of Angular framework
- **TypeScript 5.7.2** - Programming language with strict mode
- **RxJS 7.8.0** - Reactive programming library
- **Tailwind CSS 3.4.0** - Utility-first CSS framework
- **SCSS** - CSS preprocessor
- **Lucide Angular 0.542.0** - Modern icon library
- **ngx-toastr 19.0.0** - Toast notification system
- **JSZip 3.10.1** - File compression library
- **Angular CLI 19.2.15** - Command-line interface
- **Jasmine 5.6.0** - Testing framework
- **Karma 6.4.0** - Test runner
- **PostCSS 8.5.6** - CSS processing
- **Node.js** - JavaScript runtime
- **npm** - Package manager

### **DevOps & Infrastructure**
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Jenkins** - Continuous Integration/Deployment
- **AWS** - Cloud infrastructure (SQS, S3, Parameter Store)

### **Architecture Pattern**
- **Microservices Architecture** - Modular service-based design
- **Multi-module Maven Project** - Backend organized in modules
- **Monorepo Structure** - Single repository with multiple components
- **RESTful APIs** - Service communication
- **Cloud-Native** - AWS integration and containerization

## ✨ Features

### **Core Functionality**
- **Message Generation**: Generate ISO 20022 compliant PACS.008 (Customer Credit Transfer) messages
- **Message Validation**: Validate RTP messages against schemas and business rules
- **Real-time Processing**: Instant message generation and validation
- **Download Support**: Download individual messages or bulk ZIP files

### **User Interface**
- **Modern UI**: Beautiful, responsive Angular frontend with Tailwind CSS
- **Dashboard**: Overview of system statistics and quick actions
- **Generator**: Configure and generate multiple RTP messages
- **Validator**: Paste XML content for validation with detailed results
- **Documentation**: Comprehensive API documentation and integration guides

### **Enterprise Features**
- **RESTful API**: Clean API endpoints for integration with existing systems
- **Security**: Spring Security integration with authentication and authorization
- **Audit Trail**: Complete activity tracking and logging
- **Cloud Integration**: AWS services integration (SQS, S3, Parameter Store)
- **Containerization**: Docker support for easy deployment
- **CI/CD**: Jenkins pipeline for automated builds and deployments

## 🏗️ Architecture

### **Backend (Spring Boot)**
- **Location**: `galaxy-rtp-validator-service/`
- **Port**: 8080
- **Context Path**: `/rtp-message`
- **Technology**: Spring Boot, Java 21, Maven
- **Database**: MySQL (Production), H2 (Testing)
- **Cloud**: AWS Integration (SQS, S3, Parameter Store)

### **Frontend (Angular 19)**
- **Location**: `galaxy-rtp-validator-ui/`
- **Port**: 4200
- **Technology**: Angular 19, TypeScript 5.7, Tailwind CSS
- **Build Tool**: Angular CLI 19.2.15
- **Package Manager**: npm

### **Project Structure**
```
galaxy-rtp-validator/
├── galaxy-rtp-validator-service/     # Backend Spring Boot application
├── galaxy-rtp-validator-client/      # Client library
├── galaxy-rtp-validator-ui/          # Frontend Angular application
├── output/                           # Generated XML files
├── pom.xml                          # Parent Maven configuration
└── Jenkinsfile                      # CI/CD pipeline
```

## 🚀 Quick Start

### **Prerequisites**
- **Java 21** (OpenJDK 21) or higher
- **Node.js 18** or higher
- **Maven 3.6** or higher
- **MySQL** (for production) or **H2** (for development)

### **Option 1: Using the Startup Script (Windows)**
1. Navigate to the project root directory
2. Double-click `start-galaxy-rtp.bat`
3. This will start both backend and frontend services automatically

### **Option 2: Manual Startup**

#### **Start Backend Service**
```bash
cd galaxy-rtp-validator-service
mvn clean install
mvn spring-boot:run
```

#### **Start Frontend Service**
```bash
cd galaxy-rtp-validator-ui
npm install
npm start
```

### **Option 3: Docker (Recommended for Production)**
```bash
cd galaxy-rtp-validator-service
docker-compose up -d
```

## 🌐 Accessing the Application

- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080/rtp-message
- **API Documentation**: http://localhost:8080/rtp-message/swagger-ui.html
- **Health Check**: http://localhost:8080/rtp-message/actuator/health

## 🔌 API Endpoints

### **Generate Messages**
```http
POST /rtp-message/rtp/messages
Content-Type: application/json

{
  "numberOfFiles": 5,
  "messageType": "pacs.008"
}
```

### **Validate Message**
```http
POST /rtp-message/rtp/validate
Content-Type: application/json

{
  "xmlContent": "<?xml version=\"1.0\"?>...",
  "messageType": "pacs.008"
}
```

### **Health Check**
```http
GET /rtp-message/actuator/health
```

### **API Documentation**
- **Swagger UI**: http://localhost:8080/rtp-message/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/rtp-message/v3/api-docs

## 🎨 Frontend Features

### **Dashboard**
- Overview of system statistics and metrics
- Quick action buttons for common tasks
- Feature highlights and system information
- Real-time status monitoring

### **Generator**
- Configure message type and number of files
- Generate multiple RTP messages with custom parameters
- Download individual messages or bulk ZIP files
- Copy messages to clipboard for easy sharing
- Preview generated XML content

### **Validator**
- Paste XML content for validation
- Select message type (PACS.008)
- View detailed validation results with error highlighting
- Load sample messages for testing
- Export validation reports

### **Documentation**
- Comprehensive API documentation with interactive examples
- Message type specifications and schemas
- Integration guidelines and best practices
- Code examples in multiple languages

## 📋 Message Types Supported

### **PACS.008 - Customer Credit Transfer**
- Used for customer credit transfers in real-time payment systems
- Contains transfer details, parties, and settlement information
- Supports ISO 20022 schema validation
- Includes business rule validation



## 💻 Development

### **Prerequisites**
- **Java 21** (OpenJDK 21) or higher
- **Node.js 18** or higher
- **Maven 3.6** or higher
- **MySQL** (for production) or **H2** (for development)

### **Backend Development**
```bash
cd galaxy-rtp-validator-service
mvn clean install
mvn spring-boot:run
```

### **Frontend Development**
```bash
cd galaxy-rtp-validator-ui
npm install
npm start
```

### **Building for Production**
```bash
# Backend
cd galaxy-rtp-validator-service
mvn clean package

# Frontend
cd galaxy-rtp-validator-ui
npm run build
```

### **Testing**
```bash
# Backend Tests
cd galaxy-rtp-validator-service
mvn test

# Frontend Tests
cd galaxy-rtp-validator-ui
npm run test
npm run test:watch
```

### **Code Quality**
```bash
# Frontend Linting
cd galaxy-rtp-validator-ui
npm run lint

# Backend Code Coverage
cd galaxy-rtp-validator-service
mvn jacoco:report
```

## ⚙️ Configuration

### **Backend Configuration**
The backend configuration is in `galaxy-rtp-validator-service/src/main/resources/application.properties`:
- **Server port**: 8080
- **Context path**: /rtp-message
- **Database configuration**: MySQL (production) / H2 (development)
- **AWS configuration**: SQS, S3, Parameter Store settings
- **Security settings**: Authentication and authorization

### **Frontend Configuration**
The frontend configuration files:
- **`proxy.config.json`**: API proxy to backend service
- **`angular.json`**: Angular CLI configuration
- **`tailwind.config.js`**: Tailwind CSS configuration
- **`tsconfig.json`**: TypeScript configuration

### **Environment Variables**
```bash
# Backend
SPRING_PROFILES_ACTIVE=local
AWS_REGION=us-east-1
DB_URL=jdbc:mysql://localhost:3306/galaxy_rtp

# Frontend
API_BASE_URL=http://localhost:8080/rtp-message
```

## 🔧 Troubleshooting

### **Common Issues**

1. **Port Already in Use**
   - Backend: Change port in `application.properties`
   - Frontend: Change port in `angular.json`

2. **Dependency Issues**
   - Frontend: Run `npm install` (no need for --legacy-peer-deps in Angular 19)
   - Backend: Run `mvn clean install`

3. **Backend Not Starting**
   - Check Java version (requires Java 21+)
   - Verify Maven installation
   - Check application.properties configuration
   - Ensure database is running (MySQL/H2)

4. **Frontend Not Loading**
   - Check Node.js version (requires 18+)
   - Clear npm cache: `npm cache clean --force`
   - Delete node_modules and reinstall: `rm -rf node_modules && npm install`

5. **AWS Integration Issues**
   - Verify AWS credentials and region configuration
   - Check SQS, S3, and Parameter Store permissions
   - Ensure proper IAM roles and policies

6. **Database Connection Issues**
   - Verify MySQL server is running
   - Check database credentials in application.properties
   - Ensure database schema is created

### **Logs**
- **Backend logs**: Check console output or application logs
- **Frontend logs**: Check browser developer console
- **Docker logs**: `docker-compose logs galaxy-rtp-validator`
- **Jenkins logs**: Check Jenkins pipeline console output

## 🤝 Contributing

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/your-feature-name`
3. **Make your changes** following the coding standards
4. **Test thoroughly**:
   - Run backend tests: `mvn test`
   - Run frontend tests: `npm run test`
   - Test manually in browser
5. **Submit a pull request** with detailed description

### **Coding Standards**
- **Backend**: Follow Java coding conventions and Spring Boot best practices
- **Frontend**: Follow Angular style guide and TypeScript best practices
- **Testing**: Maintain good test coverage (>80%)
- **Documentation**: Update README and API documentation as needed

## 📄 License

This project is proprietary software developed for internal use by Finzly.

## 🆘 Support

For support and questions, please contact the development team.

## 📊 Project Status

- **Version**: 6.2.0.0-SNAPSHOT
- **Status**: Active Development
- **Last Updated**: 2024
- **Maintainers**: Finzly Development Team

## 🔗 Related Links

- **API Documentation**: http://localhost:8080/rtp-message/swagger-ui.html
- **Frontend Application**: http://localhost:4200
- **Health Check**: http://localhost:8080/rtp-message/actuator/health