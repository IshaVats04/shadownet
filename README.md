# ShadowNet IDS - Intrusion Detection System

A comprehensive cybersecurity Intrusion Detection System built with Spring Boot and a cyberpunk-styled frontend. It monitors network traffic in real-time, detects malicious activities (brute force attacks, DoS attacks, honeypot access), generates alerts with threat scoring, and provides a dashboard for security analysts and administrators to monitor and respond to threats.

## Features

### Backend (Spring Boot)
- **Authentication & Authorization**
  - JWT-based authentication
  - Role-based access control (ROLE_ADMIN, ROLE_ANALYST)
  - Password encryption with BCrypt
- **Intrusion Detection**
  - Brute Force Detection (5 failed logins in 1 minute → HIGH alert)
  - DoS Detection (100+ requests/minute from same IP → HIGH alert)
  - Honeypot Detection (access to fake sensitive endpoints → HIGH/MEDIUM alert)
  - Traffic Logging (every request logged to database)
- **Threat Scoring**
  - LOW = 20 points
  - MEDIUM = 50 points
  - HIGH = 90 points
- **Admin Features (ROLE_ADMIN only)**
  - User management (view/delete users)
  - IP blocking/unblocking
  - System configuration (threshold adjustment)

### Frontend (HTML/JS/Tailwind)
- Single-page application with tabbed interface
- Real-time data fetching from backend APIs
- Chart.js for analytics visualization
- Cyberpunk UI styling (dark theme, neon colors)
- Search and filter functionality
- CSV export for reporting

## Technology Stack

**Backend**
- Java 17
- Spring Boot 4.0.6
- Spring Security
- Spring Data JPA
- Hibernate
- MySQL 8.0
- JWT (jjwt 0.11.5)
- BCrypt
- Maven

**Frontend**
- HTML5
- JavaScript (ES6+)
- Tailwind CSS (via CDN)
- Chart.js
- Fetch API

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    ATTACKER / USER                       │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP Request
                     ▼
┌─────────────────────────────────────────────────────────┐
│              TrafficLoggingFilter                        │
│  • Logs all requests to traffic_logs table              │
│  • Triggers DoS detection                               │
│  • Extracts IP address                                  │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│              JwtAuthenticationFilter                      │
│  • Validates JWT token                                  │
│  • Checks user role (ADMIN/ANALYST)                     │
│  • Allows/denies access based on permissions            │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                    Controllers                           │
│  • AuthController (login/register)                       │
│  • HoneypotController (fake endpoints)                   │
│  • IdsController (dashboard data)                        │
│  • AdminController (admin features)                      │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        ▼                         ▼
┌───────────────┐        ┌────────────────┐
│   Services    │        │ Detection Logic │
│               │        │                │
│ • AuthService │        │ • Brute Force  │
│ • AlertService│        │ • DoS Detection│
│ • ThreatScore │        │ • Honeypot     │
└───────────────┘        └────────────────┘
        │                         │
        └────────────┬────────────┘
                     ▼
┌─────────────────────────────────────────────────────────┐
│                  Database (MySQL)                        │
│  • users table (credentials, roles)                     │
│  • alerts table (detected threats)                       │
│  • traffic_logs table (all requests)                    │
└─────────────────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│              Frontend Dashboard                          │
│  • Real-time statistics                                  │
│  • Alerts table with severity                           │
│  • Traffic logs viewer                                  │
│  • Analytics charts (Chart.js)                          │
│  • Search & filter                                      │
│  • CSV export                                           │
└─────────────────────────────────────────────────────────┘
```

## Database Design

### Users Table
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
```

### Alerts Table
```sql
CREATE TABLE alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(50) NOT NULL,
    attack_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    threat_score INT NOT NULL,
    timestamp DATETIME NOT NULL
);
```

### Traffic Logs Table
```sql
CREATE TABLE traffic_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_ip VARCHAR(50) NOT NULL,
    destination_ip VARCHAR(255) NOT NULL,
    request_type VARCHAR(10) NOT NULL,
    status VARCHAR(10) NOT NULL,
    timestamp DATETIME NOT NULL
);
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### IDS (ROLE_ADMIN, ROLE_ANALYST)
- `GET /api/analyst/dashboard-stats` - Get dashboard statistics
- `GET /api/analyst/alerts` - Get all alerts
- `GET /api/analyst/traffic-logs` - Get all traffic logs

### Admin (ROLE_ADMIN only)
- `GET /api/admin/users` - Get all users
- `DELETE /api/admin/users/{id}` - Delete user
- `POST /api/admin/block-ip` - Block IP address
- `POST /api/admin/unblock-ip` - Unblock IP address
- `GET /api/admin/blocked-ips` - Get blocked IPs
- `GET /api/admin/config` - Get system configuration
- `POST /api/admin/config` - Update system configuration

### Honeypot (Public but logged)
- `GET /honeypot/admin-secret` - Fake admin endpoint
- `GET /honeypot/internal-panel` - Fake internal panel
- `GET /honeypot/config-backup` - Fake config backup
- `GET /honeypot/database-dump` - Fake database dump

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- IDE (IntelliJ IDEA or VS Code)

### Database Setup
1. Create a MySQL database:
```sql
CREATE DATABASE shadownet;
```

2. Update database credentials in `shadownet/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/shadownet
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Running the Application
1. Clone the repository
2. Navigate to the project directory:
```bash
cd shadownet
```

3. Run the application:
```bash
./mvnw spring-boot:run
```

4. Access the application at `http://localhost:8081`

### Default Users
Register a new user through the UI with:
- **Admin**: username `admin`, password `admin123`, role `ROLE_ADMIN`
- **Analyst**: username `analyst`, password `analyst123`, role `ROLE_ANALYST`

## Role-Based Access Control

### ROLE_ADMIN
- Full access to all features
- User management (view/delete users)
- IP blocking/unblocking
- System configuration (threshold adjustment)
- Access to all analyst features

### ROLE_ANALYST
- Read-only access to alerts, traffic logs, and analytics
- Cannot modify system settings
- Cannot manage users or block IPs

## Security Features

- **Authentication**: BCrypt password hashing, JWT tokens with expiration
- **Authorization**: Role-based access control with @PreAuthorize
- **CORS**: Configured for development
- **CSRF**: Disabled for stateless API (JWT provides protection)
- **SQL Injection Prevention**: JPA parameterized queries

## Attack Simulation

### Brute Force Attack
```bash
for i in {1..6}; do
  curl -X POST http://localhost:8081/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"wrongpass"}'
  echo "Attempt $i"
  sleep 5
done
```

### Honeypot Access
Visit these endpoints in your browser:
- `http://localhost:8081/honeypot/admin-secret`
- `http://localhost:8081/honeypot/internal-panel`
- `http://localhost:8081/honeypot/config-backup`
- `http://localhost:8081/honeypot/database-dump`

### DoS Attack (Optional)
Requires Apache Bench or similar tool:
```bash
ab -n 150 -c 10 http://localhost:8081/api/analyst/dashboard-stats
```

## Project Structure

```
shadownet/
├── src/main/java/com/shadownet/
│   ├── config/           # Security configuration
│   ├── controller/       # REST controllers
│   ├── dto/              # Data transfer objects
│   ├── filter/           # Custom filters (JWT, traffic logging)
│   ├── model/            # JPA entities
│   ├── Repository/       # JPA repositories
│   ├── service/          # Business logic
│   ├── util/             # Utility classes (JWT)
│   └── ShadownetApplication.java
├── src/main/resources/
│   ├── static/           # Frontend files (HTML, JS, CSS)
│   └── application.properties
└── pom.xml
```

## Key Implementation Details

### Brute Force Detection
Uses in-memory ConcurrentHashMap to track failed login attempts per IP with a 1-minute time window. After 5 failed attempts, a HIGH severity alert is generated.

### DoS Detection
Tracks request counts per IP in memory. If an IP exceeds 100 requests in 1 minute, a HIGH severity alert is generated.

### JWT Authentication Flow
1. User sends credentials to `/api/auth/login`
2. Server validates and generates JWT token with role claim
3. Client stores token in localStorage
4. Client sends token in Authorization header: `Bearer {token}`
5. JwtAuthenticationFilter validates token before each request
6. Security context set with user authorities
7. Controller checks @PreAuthorize annotations

### Traffic Logging
Every HTTP request is logged to the traffic_logs table via TrafficLoggingFilter. This filter also triggers DoS detection checks.

## Future Enhancements

- Real-time updates with WebSocket integration
- Actual firewall integration for IP blocking
- Geo-IP visualization with map-based attack origin display
- Machine learning for anomaly detection
- SIEM integration (Splunk, ELK Stack)
- Multi-factor authentication for admin users
- Audit logging for admin actions
- PDF report generation with scheduling
- React Native mobile application
- Microservices architecture

## License

This project is for educational and demonstration purposes.
