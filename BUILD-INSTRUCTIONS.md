# NexFit Backend - Build Instructions

## ✅ Build and Run Instructions

### 1. **Build without Tests** (Recommended for first build)
```bash
./mvnw clean install -DskipTests
```

### 2. **Run the Application**

#### Option A: Using Docker (Recommended)
```bash
# Start all services with Docker Compose
docker-compose up -d

# The application will be available at:
# http://localhost:8080/api/v1/swagger-ui.html
```

#### Option B: Manual with Local Databases
```bash
# 1. Start PostgreSQL
brew services start postgresql
createdb trainerhub
psql -c "CREATE USER trainerhub WITH PASSWORD 'trainerhub123';"
psql -c "GRANT ALL PRIVILEGES ON DATABASE trainerhub TO trainerhub;"

# 2. Start MongoDB
brew services start mongodb-community

# 3. Start Redis
brew services start redis

# 4. Run the application
./mvnw spring-boot:run
```

### 3. **Test the Application**
```bash
# Check health endpoint
curl http://localhost:8080/api/v1/actuator/health

# Access Swagger UI
open http://localhost:8080/api/v1/swagger-ui.html
```

## 🔧 Troubleshooting

### Issue: Test Failures
If tests fail due to external dependencies (MongoDB, Redis), you can:

1. **Skip tests during build:**
```bash
./mvnw clean install -DskipTests
```

2. **Run with test containers (requires Docker):**
```bash
# Make sure Docker is running
docker info

# Run tests
./mvnw test
```

### Issue: Database Connection Errors
Make sure all required services are running:

```bash
# Check PostgreSQL
pg_isready -h localhost -p 5432

# Check MongoDB
mongosh --eval "db.runCommand({ ping: 1 })"

# Check Redis
redis-cli ping
```

### Issue: Port Already in Use
If port 8080 is already in use:

```bash
# Run on different port
SERVER_PORT=8081 ./mvnw spring-boot:run
```

## 📦 Creating JAR for Production
```bash
# Build production JAR
./mvnw clean package -DskipTests -P production

# Run JAR file
java -jar target/NexFit-0.0.1-SNAPSHOT.jar
```

## 🐳 Docker Build
```bash
# Build Docker image
docker build -t trainerhub-backend .

# Run Docker container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  trainerhub-backend
```

## 🧪 Running Specific Tests

### Unit Tests Only
```bash
./mvnw test -Dtest="*ServiceTest"
```

### Integration Tests with TestContainers
```bash
# Requires Docker to be running
./mvnw verify
```

### Skip All Tests
```bash
./mvnw clean install -DskipTests
```

## 📝 Environment Variables

Create `.env` file from template:
```bash
cp .env.example .env
```

Required variables:
- `STRIPE_API_KEY` - Your Stripe secret key
- `EMAIL_USERNAME` - Gmail address for sending emails
- `EMAIL_PASSWORD` - Gmail app-specific password

## 🚀 Quick Start (Minimal Setup)

For the fastest setup without external dependencies:

```bash
# 1. Build without tests
./mvnw clean install -DskipTests

# 2. Start with Docker Compose
docker-compose up -d

# 3. Verify it's running
curl http://localhost:8080/api/v1/actuator/health
```

The application will automatically create database schemas on startup.
