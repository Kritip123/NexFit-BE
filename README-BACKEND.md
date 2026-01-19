# TrainerHub Backend - Spring Boot Application

## 📋 Overview

TrainerHub is a comprehensive fitness trainer marketplace application built for the Australian market. The backend is built with Spring Boot following MVC architecture with proper design patterns, scalability considerations, and comprehensive API coverage.

## 🚀 Technology Stack

- **Framework**: Spring Boot 4.0.0
- **Language**: Java 17
- **Databases**: 
  - PostgreSQL (User data, Trainer profiles, Reviews)
  - MongoDB (Bookings, Availability, Notifications)
  - Redis (Session management, Distributed locking)
- **Authentication**: JWT with Spring Security
- **Payment**: Stripe API
- **Email**: Spring Mail with Gmail SMTP
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven

## 🏗️ Architecture

```
src/main/java/org/example/trainerhub/
├── config/           # Configuration classes (Security, CORS, etc.)
├── controller/       # REST API controllers
├── service/          # Business logic layer
│   └── impl/        # Service implementations
├── repository/       # Data access layer
├── entity/          # JPA and MongoDB entities
├── model/           
│   ├── dto/         # Data Transfer Objects
│   ├── request/     # Request models
│   └── response/    # Response models
├── exception/       # Custom exceptions and handlers
├── security/        # JWT and authentication
├── util/           # Utility classes
└── mapper/         # Entity-DTO mappers
```

## 📊 Database Schemas

### PostgreSQL Schema

#### Users Table
```sql
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    avatar TEXT,
    role VARCHAR(50) DEFAULT 'USER',
    is_active BOOLEAN DEFAULT true,
    email_verified BOOLEAN DEFAULT false,
    reset_password_token VARCHAR(255),
    reset_password_token_expiry TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Trainers Table
```sql
CREATE TABLE trainers (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    profile_image TEXT,
    cover_image TEXT,
    experience INTEGER,
    rating DECIMAL(3,2) DEFAULT 0,
    review_count INTEGER DEFAULT 0,
    hourly_rate DECIMAL(10,2) NOT NULL,
    bio TEXT,
    instagram_id VARCHAR(100),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100) DEFAULT 'Australia',
    zip_code VARCHAR(10),
    gym_affiliation VARCHAR(255),
    total_clients INTEGER DEFAULT 0,
    transformations INTEGER DEFAULT 0,
    sessions_completed INTEGER DEFAULT 0,
    years_active INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    is_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Reviews Table
```sql
CREATE TABLE reviews (
    id VARCHAR(255) PRIMARY KEY,
    trainer_id VARCHAR(255) REFERENCES trainers(id),
    user_id VARCHAR(255) REFERENCES users(id),
    booking_id VARCHAR(255) UNIQUE NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### MongoDB Collections

#### Bookings Collection
```javascript
{
  _id: ObjectId,
  trainerId: String,
  trainerName: String,
  trainerImage: String,
  trainerPhone: String,
  userId: String,
  userName: String,
  userEmail: String,
  userPhone: String,
  date: ISODate,
  timeSlot: String,
  duration: Number,
  amount: Decimal,
  status: String, // PENDING_PAYMENT, CONFIRMED, COMPLETED, CANCELLED, REFUNDED
  paymentId: String,
  paymentIntentId: String,
  location: {
    address: String,
    city: String,
    state: String,
    latitude: Number,
    longitude: Number
  },
  notes: String,
  version: Number, // For optimistic locking
  cancelledAt: ISODate,
  cancellationReason: String,
  cancelledBy: String,
  createdAt: ISODate,
  updatedAt: ISODate
}
```

#### TrainerAvailability Collection
```javascript
{
  _id: ObjectId,
  trainerId: String,
  weeklySchedule: {
    monday: [{startTime: String, endTime: String, available: Boolean}],
    tuesday: [...],
    // ... other days
  },
  dateOverrides: Map, // Specific date availability
  blockedDates: [ISODate], // Vacation/unavailable dates
}
```

#### Notifications Collection
```javascript
{
  _id: ObjectId,
  userId: String,
  type: String, // BOOKING_CONFIRMED, BOOKING_REMINDER, etc.
  title: String,
  message: String,
  data: Object,
  read: Boolean,
  readAt: ISODate,
  createdAt: ISODate
}
```

## 🔌 API Endpoints

### Authentication (6 endpoints)
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/logout` - User logout
- `POST /api/v1/auth/refresh-token` - Refresh JWT token
- `POST /api/v1/auth/forgot-password` - Send password reset email
- `POST /api/v1/auth/reset-password` - Reset password with token

### User Profile (4 endpoints)
- `GET /api/v1/users/me` - Get current user profile
- `PUT /api/v1/users/me` - Update user profile
- `POST /api/v1/users/me/avatar` - Upload profile avatar
- `DELETE /api/v1/users/me` - Delete user account

### Trainers (5 endpoints)
- `GET /api/v1/trainers` - Get all trainers with filters
- `GET /api/v1/trainers/:id` - Get trainer by ID
- `GET /api/v1/trainers/:id/availability` - Get trainer availability
- `GET /api/v1/trainers/:id/reviews` - Get trainer reviews
- `GET /api/v1/trainers/:id/portfolio` - Get trainer portfolio

### Bookings (5 endpoints)
- `POST /api/v1/bookings` - Create new booking
- `GET /api/v1/bookings` - Get user's bookings
- `GET /api/v1/bookings/:id` - Get booking details
- `PUT /api/v1/bookings/:id/cancel` - Cancel booking
- `PUT /api/v1/bookings/:id/reschedule` - Reschedule booking

### Payments (5 endpoints)
- `POST /api/v1/payments/create-order` - Create payment order
- `POST /api/v1/payments/verify` - Verify payment
- `GET /api/v1/payments/history` - Get payment history
- `GET /api/v1/payments/:id` - Get payment details
- `POST /api/v1/payments/:id/refund` - Request refund

## 🔧 Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose
- Stripe account for payment processing
- Gmail account for email notifications

### Environment Variables
Create a `.env` file in the project root:

```bash
STRIPE_API_KEY=your_stripe_secret_key
STRIPE_WEBHOOK_SECRET=your_stripe_webhook_secret
EMAIL_USERNAME=your_email@gmail.com
EMAIL_PASSWORD=your_app_specific_password
```

### Local Development Setup

#### Option 1: Using Docker Compose (Recommended)

1. **Clone the repository**
```bash
git clone <repository-url>
cd TrainerHub
```

2. **Start all services**
```bash
docker-compose up -d
```

This will start:
- PostgreSQL on port 5432
- MongoDB on port 27017
- Redis on port 6379
- Spring Boot application on port 8080

3. **Check application health**
```bash
curl http://localhost:8080/api/v1/actuator/health
```

#### Option 2: Manual Setup

1. **Install and start databases**
```bash
# PostgreSQL
brew install postgresql
brew services start postgresql
createdb trainerhub
createuser trainerhub

# MongoDB
brew install mongodb-community
brew services start mongodb-community

# Redis
brew install redis
brew services start redis
```

2. **Configure database credentials**
Update `application.properties` with your local database credentials

3. **Build and run the application**
```bash
mvn clean install
mvn spring-boot:run
```

### Database Initialization

The application uses Hibernate's auto-update for PostgreSQL schemas. MongoDB collections are created automatically.

To seed initial data (optional):
```bash
# Connect to PostgreSQL
psql -U trainerhub -d trainerhub

# Run seed script (create one based on your needs)
\i database/seed.sql
```

## 🧪 Testing

### Run unit tests
```bash
mvn test
```

### Run integration tests
```bash
mvn verify
```

### API Testing with Swagger
Access Swagger UI at: http://localhost:8080/api/v1/swagger-ui.html

## 🔒 Security Features

- JWT-based authentication with refresh tokens
- Password encryption using BCrypt
- Role-based access control (USER, TRAINER, ADMIN)
- CORS configuration for frontend integration
- Distributed locking for concurrent booking prevention
- Request validation and sanitization

## 📈 Scalability Considerations

1. **Microservices Ready**: The booking service can be easily extracted as a separate microservice
2. **Caching**: Redis caching for frequently accessed data
3. **Database Optimization**: Proper indexing on frequently queried fields
4. **Async Processing**: Email notifications are sent asynchronously
5. **Connection Pooling**: Configured for optimal database connections
6. **Optimistic Locking**: Prevents race conditions in booking updates

## 🚀 Production Deployment

### Build for production
```bash
mvn clean package -P production
```

### Docker deployment
```bash
docker build -t trainerhub-backend .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e DATABASE_URL=$DATABASE_URL \
  -e MONGODB_URI=$MONGODB_URI \
  trainerhub-backend
```

### Health Monitoring
- Health endpoint: `/api/v1/actuator/health`
- Metrics endpoint: `/api/v1/actuator/metrics`
- Info endpoint: `/api/v1/actuator/info`

## 📝 API Response Format

### Success Response
```json
{
  "data": {},
  "message": "Success",
  "timestamp": "2024-03-15T10:30:00Z"
}
```

### Error Response
```json
{
  "timestamp": "2024-03-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "validationErrors": {
    "field": "error message"
  }
}
```

### Pagination Response
```json
{
  "data": [],
  "pagination": {
    "page": 0,
    "limit": 10,
    "total": 100,
    "totalPages": 10,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is proprietary and confidential.

## 📞 Support

For support, email support@trainerhub.com.au
