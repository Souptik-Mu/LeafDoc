# 🌿 LeafDoc

LeafDoc is a full-stack web application consisting of a React + TypeScript frontend and a Spring Boot backend. The application uses MySQL for database management and is configured to support JWT authentication, an ML service, email functionality, and OTP-based operations.

---

## 📁 Project Structure

```text
LeafDoc/
│
├── LeafDocFrontend/
│   ├── public/
│   ├── src/
│   ├── package.json
│   ├── vite.config.ts
│   └── ...
│
├── LeafDocBackend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   ├── test/
│   │   └── ...
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
└── README.md
```

---

## 🛠️ Technologies Used

### Frontend

* React
* TypeScript
* Vite
* HTML5
* CSS3

### Backend

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* Maven

### Database

* MySQL

### Additional Services

* JWT Authentication
* Machine Learning Service
* Email Service
* OTP Verification

---

## ⚙️ Prerequisites

Install the following software before setting up the project:

| Software        | Purpose                         |
| --------------- | ------------------------------- |
| Java 21         | Run the Spring Boot backend     |
| Node.js and npm | Run the React frontend          |
| MySQL Server    | Store application data          |
| Git             | Clone and manage the repository |
| Maven           | Build and run the backend       |

Verify the installations:

```bash
java -version
node -v
npm -v
git --version
```

---

# 🚀 Project Setup

## 1. Clone the Repository

Clone the project from GitHub:

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

Navigate to the project directory:

```bash
cd LeafDoc
```

---

# 🗄️ Database Configuration

LeafDoc uses MySQL as its database.

### Step 1: Start MySQL

Make sure MySQL Server is running on your system.

### Step 2: Create the Database

Open MySQL Workbench or the MySQL command line and execute:

```sql
CREATE DATABASE leafdoc;
```

> **Important:** The database name must match the name configured in `application.properties`. If your team uses another database name, update the connection URL accordingly.

### Step 3: Configure Database Credentials

Open:

```text
LeafDocBackend/src/main/resources/application.properties
```

Update the following properties:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/leafdoc
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

Replace `YOUR_MYSQL_PASSWORD` with your local MySQL password.

---

# ☕ Backend Setup

Navigate to the backend directory:

```bash
cd LeafDocBackend
```

### Windows

Run the following command in PowerShell or Command Prompt:

```bash
mvnw.cmd spring-boot:run
```

### Linux / macOS

```bash
./mvnw spring-boot:run
```

If Maven is installed globally, you can also use:

```bash
mvn spring-boot:run
```

### Backend Configuration

The backend is configured with the following settings:

| Property         | Value          |
| ---------------- | -------------- |
| Application name | LeafDocBackend |
| Server port      | 8080           |
| Server address   | 0.0.0.0        |
| Context path     | `/api`         |
| Database         | MySQL          |
| Database port    | 3306           |
| ML service port  | 8000           |

The backend base URL is:

```text
http://localhost:8080/api
```

> The `/api` context path is automatically added to backend routes. For example, if a controller defines `/users`, the complete URL will be `/api/users`.

---

# 🖥️ Frontend Setup

Open another terminal and navigate to the frontend directory:

```bash
cd LeafDocFrontend
```

Install the required dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend will normally be available at:

```text
http://localhost:5173
```

---

# 🔗 Frontend–Backend Connection

The frontend runs on port `5173`, while the Spring Boot backend runs on port `8080` with the `/api` context path.

```text
┌──────────────────────────────┐
│       React Frontend         │
│    http://localhost:5173     │
└──────────────┬───────────────┘
               │
               │ HTTP Requests
               ▼
┌──────────────────────────────┐
│      Spring Boot Backend     │
│  http://localhost:8080/api   │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│        MySQL Database        │
│       localhost:3306         │
└──────────────────────────────┘
```

### Frontend API URL

Configure the frontend API base URL as:

```env
VITE_API_URL=http://localhost:8080/api
```

If your frontend uses a different environment variable name, configure the URL according to the existing frontend API service.

---

# 🔐 Environment Configuration

The backend currently uses configuration values for authentication, email, OTP, and ML services.

For local development, update the following properties in `application.properties`:

```properties
# JWT
app.jwt.secret=YOUR_JWT_SECRET
app.jwt.expiration=86400000

# CORS
app.cors.allowed-origin=http://localhost:5173

# ML Service
app.ml.service.host=0.0.0.0
app.ml.service.port=8000
app.ml.service.key=YOUR_ML_SERVICE_KEY

# Mail
app.mail.from=YOUR_EMAIL_ADDRESS

# OTP
app.otp.expiration=300
```

### Configuration Details

| Configuration             | Description                                 |
| ------------------------- | ------------------------------------------- |
| `app.jwt.secret`          | Secret key used for JWT operations          |
| `app.jwt.expiration`      | JWT validity period in milliseconds         |
| `app.cors.allowed-origin` | Allowed frontend origin                     |
| `app.ml.service.host`     | Host address of the ML service              |
| `app.ml.service.port`     | Port of the ML service                      |
| `app.ml.service.key`      | Key used to communicate with the ML service |
| `app.mail.from`           | Email address used as the sender            |
| `app.otp.expiration`      | OTP validity period in seconds              |

The configured JWT expiration is `86400000` milliseconds, which is 24 hours. The OTP expiration is `300` seconds, which is 5 minutes.

---

# 🤖 Machine Learning Service

LeafDoc is configured to communicate with a separate ML service.

| Setting     | Value                                   |
| ----------- | --------------------------------------- |
| Host        | `0.0.0.0`                               |
| Port        | `8000`                                  |
| Service key | Configured through `app.ml.service.key` |

The ML service must be running separately if the backend requires it for any application functionality.

> The exact ML framework, endpoints, and model setup should be documented here once the ML service implementation is finalized.

---

# 📧 Email Configuration

The backend supports an email sender configuration:

```properties
app.mail.from=${MAIL_FROM}
```

Before running email-related features, configure the `MAIL_FROM` environment variable with the required sender email address.

Do not commit email passwords, API keys, or other confidential credentials to GitHub.

---

# 🧪 Useful Commands

## Frontend

Start the development server:

```bash
npm run dev
```

Build the frontend:

```bash
npm run build
```

Run linting:

```bash
npm run lint
```

Preview the production build:

```bash
npm run preview
```

## Backend

Run the backend:

```bash
mvnw.cmd spring-boot:run
```

Build the backend:

```bash
mvnw.cmd clean package
```

Run backend tests:

```bash
mvnw.cmd test
```

For Linux/macOS, replace `mvnw.cmd` with `./mvnw`.

---

# 🧑‍💻 Git Workflow

Create a separate branch for each feature:

```bash
git checkout -b feature/your-feature-name
```

Example:

```bash
git checkout -b feature/leaf-diagnosis
```

After making changes:

```bash
git add .
git commit -m "Implement leaf diagnosis feature"
git push origin feature/leaf-diagnosis
```

Create a Pull Request after pushing your branch.

Before starting new work:

```bash
git pull origin main
```

---

# 🐛 Common Issues

## 1. MySQL Connection Error

Check the following:

* MySQL Server is running.
* Database name is correct.
* Username and password are correct.
* MySQL is running on port `3306`.
* The database exists.

## 2. Frontend Cannot Connect to Backend

Verify that the frontend is using:

```text
http://localhost:8080/api
```

Also check that the backend is running and that the frontend origin is allowed by the CORS configuration.

## 3. Port 8080 Already in Use

Change the backend port in `application.properties`:

```properties
server.port=8081
```

Then update the frontend API URL:

```env
VITE_API_URL=http://localhost:8081/api
```

## 4. JWT or OTP Errors

Check that the JWT secret, expiration values, and any required email or OTP configuration are properly set.

## 5. ML Service Unavailable

Make sure the ML service is running on the configured host and port:

```text
http://localhost:8000
```

The actual ML endpoint depends on the service implementation.

---

# ✅ Setup Checklist

* [ ] Install Java 21
* [ ] Install Node.js and npm
* [ ] Install MySQL Server
* [ ] Install Git
* [ ] Clone the LeafDoc repository
* [ ] Create the MySQL database
* [ ] Configure database credentials
* [ ] Configure JWT secret
* [ ] Configure email settings
* [ ] Configure ML service settings
* [ ] Start the Spring Boot backend
* [ ] Install frontend dependencies
* [ ] Start the React frontend
* [ ] Verify frontend–backend communication

---

## 🌿 Project Information

**Project Name:** LeafDoc

**Backend:** Spring Boot + Java 21

**Frontend:** React + TypeScript + Vite

**Database:** MySQL

**Backend Base URL:** `http://localhost:8080/api`

**Frontend URL:** `http://localhost:5173`

**ML Service Port:** `8000`

**Project Type:** Full-Stack Web Application
