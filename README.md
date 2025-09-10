###🛡️ Oxyn Sentinel Backend

Secure backend platform for organizations, users, and projects — powered by Keycloak, PostgreSQL, and automated security scanning (Nmap, ZAP, OSINT, Semgrep).

###🎯 Project Goal

The Oxyn Sentinel Backend provides a secure and centralized platform to manage organizations, users, and security projects.
It integrates advanced security scanning tools — from network reconnaissance to code analysis — into a unified system, enabling real-time detection, collaboration, and vulnerability management.

By combining role-based access control, JWT authentication with Keycloak, and automated scans, Oxyn Sentinel empowers admins, developers, and cyber engineers to collaborate efficiently and build a proactive security posture.

###🚀 Features
🔐 Security & Authentication

Centralized authentication via Keycloak (JWT)

Role-based access: admin, developer, cyber_engineer

Organization and project-based access control

👥 User & Organization Management

Manage users, organizations, and their relationships

Assign users to organizations

Create and manage security projects

🧪 Security Scanning Microservice

Nmap → Network scanning & port discovery

OWASP ZAP → Web vulnerability scanning

OSINT (crt.sh, DNS, metadata) → Subdomain enumeration

Semgrep → Code security analysis (GitHub, GitLab, or local zip uploads)

📡 Integration & Automation

REST API endpoints for orchestration

Swagger UI for testing endpoints

WebSocket notifications for real-time scan results

Results stored in PostgreSQL

###🗂️ Project Structure
Management Service

oxynsentinel/
├── controller/        # API REST
├── dto/               # Input/Output models
├── model/             # JPA entities
├── repository/        # JPA interfaces
├── service/           # Business logic
├── security/          # JWT + Keycloak filters
└── OxynSentinelApplication.java

Scan Service

src/
 ├── main/java/oxynsentinel/
 │    ├── controller/   # REST Controllers
 │    ├── entity/       # Entities
 │    ├── event/        # Custom Events
 │    ├── listener/     # WebSocket Event Publisher
 │    ├── repository/   # JPA Repositories
 │    ├── security/     # JWT & Security Config
 │    └── service/      # Services (Nmap, ZAP, OSINT, CodeScan)
 ├── resources/
 │    ├── static/       # WebSocket test page
 │    └── application.properties


###🛠️ Prerequisites

Before running, make sure you have:

Java 17+

Maven 3.8+

PostgreSQL (or update application.properties)

Nmap installed globally
sudo apt install nmap


Semgrep installed globally
sudo apt install python3 python3-pip
pip install semgrep
semgrep --version


OWASP ZAP Proxy
- Download installer
wget https://github.com/zaproxy/zaproxy/releases/download/v2.16.1/ZAP_2_16_1_unix.sh

- Make executable
chmod +x ZAP_2_16_1_unix.sh

- Run installer (choose /opt/zaproxy)
./ZAP_2_16_1_unix.sh

- Start in daemon mode
cd /opt/zaproxy
./zap.sh -daemon -port 8080 -config api.disablekey=true

###⚙️ Configuration

Edit src/main/resources/application.properties:

server.port=8089
server.servlet.context-path=/oxynsentinel

spring.datasource.url=jdbc:postgresql://localhost:5432/oxynsentinel
spring.datasource.username=postgres
spring.datasource.password=yourpassword

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect


###▶️ How to Run

#Clone the project :

-git clone https://github.com/saifhamdi123/OxynSentinel.git 
-cd OxynSentinel


there will be 2 backend , one for Management and other for Scan service , choose one of them and do 

#Build & run:
-mvn clean install
-mvn spring-boot:run


## Swagger UI :(use this Url for testing)
👉 http://localhost:8089/oxynsentinel/swagger-ui.html


### IMPORTANT : 
before run the work u need a DB Server and Keycloak Server then update those parameters in application.properties , also generate a JWT 

## DB Config
-spring.datasource.url
-spring.datasource.username
-spring.datasource.password

## Keycloak Config 
-keycloak.jwk-set-uri
-keycloak.issuer-uri
-keycloak.client-id
-keycloak.admin-client-id
-keycloak.admin-client-secret
-keycloak.base-url

## JWT Config
-jwt.secret










