# Hospital Management Backend API

A Go-based REST API for hospital management system with PostgreSQL database and Firebase authentication.

## Tech Stack

- **Go 1.21** - Programming language
- **Gin** - Web framework
- **PostgreSQL** - Database
- **Firebase Admin SDK** - Authentication
- **Docker** - Containerization

## Features

- Firebase JWT authentication
- Doctor management
- Appointment booking system
- RESTful API endpoints
- CORS support
- Database migrations

## Quick Start

### Prerequisites

- Go 1.21+
- Docker and Docker Compose
- Firebase project with authentication enabled

### Setup

1. **Clone and navigate to backend directory**
   ```bash
   cd backend
   ```

2. **Start PostgreSQL database**
   ```bash
   docker-compose up -d postgres
   ```

3. **Configure environment variables**
   ```bash
   cp config.env .env
   # Edit .env with your Firebase project ID
   ```

4. **Install dependencies**
   ```bash
   go mod tidy
   ```

5. **Run the server**
   ```bash
   go run main.go
   ```

The API will be available at `http://localhost:8080`

## API Endpoints

### Public Endpoints (No Authentication)

- `GET /health` - Health check
- `GET /api/doctors` - List all doctors
- `GET /api/doctors/{id}` - Get doctor by ID

### Protected Endpoints (Require Firebase Token)

- `POST /api/appointments` - Create appointment
- `GET /api/appointments` - Get user appointments
- `PUT /api/appointments/{id}` - Update appointment
- `DELETE /api/appointments/{id}` - Cancel appointment

## Authentication

The API uses Firebase JWT tokens for authentication. Include the token in the Authorization header:

```
Authorization: Bearer <firebase_jwt_token>
```

## Database Schema

The database includes three main tables:

- **users** - User accounts (linked to Firebase)
- **doctors** - Doctor profiles and specializations
- **appointments** - Appointment bookings

## Development

### Running Tests
```bash
go test ./...
```

### Database Migrations
The database schema is automatically created when you start the PostgreSQL container using the `init.sql` file.

### API Testing
Use tools like Postman or curl to test the API endpoints:

```bash
# Health check
curl http://localhost:8080/health

# Get doctors
curl http://localhost:8080/api/doctors
```

## Production Deployment

1. Set `GIN_MODE=release` in environment
2. Use proper Firebase service account key
3. Configure production database
4. Set up proper CORS origins
5. Use HTTPS in production

## Project Structure

```
backend/
├── main.go                 # Application entry point
├── docker-compose.yml      # Docker services
├── init.sql               # Database initialization
├── config.env             # Environment template
├── database/
│   └── connection.go      # Database connection
├── models/
│   └── models.go          # Data models
├── handlers/
│   └── handlers.go        # API handlers
└── middleware/
    └── auth.go            # Authentication middleware
```


