# 🏥 Swaasthya - Healthcare Management App

A comprehensive healthcare management application built with Android (Kotlin) and Go backend, featuring Firebase authentication, doctor discovery, and appointment booking capabilities.

## 📱 Features

### Authentication
- **Email/Password Authentication** - Secure user registration and login
- **Google Sign-In** - One-click authentication with Google accounts
- **Firebase Integration** - Robust authentication backend
- **Session Management** - Automatic token refresh and logout functionality

### Doctor Discovery
- **Doctor Listing** - Browse available doctors with pagination
- **Search & Filter** - Find doctors by name or specialization
- **Doctor Profiles** - View detailed information including experience and contact
- **Available Slots** - See doctor availability by day and time

### User Experience
- **Modern UI** - Material Design 3 with healthcare-themed colors
- **Responsive Design** - Optimized for mobile devices
- **Offline Support** - Cached data for better performance
- **Pull-to-Refresh** - Easy data updates

## 🏗️ Architecture

### Frontend (Android)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt
- **Navigation**: Navigation Compose
- **State Management**: StateFlow/Flow
- **Networking**: Retrofit + OkHttp
- **Authentication**: Firebase Auth

### Backend (Go)
- **Language**: Go
- **Framework**: Gin
- **Database**: PostgreSQL
- **Authentication**: Firebase Admin SDK
- **Containerization**: Docker
- **API**: RESTful endpoints

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest version)
- Go 1.21+
- Docker & Docker Compose
- Firebase project setup
- Android device or emulator

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Swaasthya
   ```

2. **Start the database**
   ```bash
   cd backend
   docker-compose up -d postgres
   ```

3. **Run database migrations**
   ```bash
   # Database will be initialized automatically with init.sql
   ```

4. **Start the backend server**
   ```bash
   ./start.sh
   # Or manually:
   go run main.go
   ```

   The API will be available at `http://localhost:8080`

### Frontend Setup

1. **Open in Android Studio**
   - Open the project root directory
   - Sync Gradle files
   - Wait for dependencies to download

2. **Firebase Configuration**
   - Place your `google-services.json` in `app/` directory
   - Update the web client ID in `AuthRepository.kt` if needed

3. **Network Configuration**
   - For physical device testing, update IP address in `FirebaseModule.kt`
   - Default: `http://192.168.74.120:8080/` (update to your laptop's IP)

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

## 📡 API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/refresh` - Refresh token

### Doctors
- `GET /api/mobile/doctors` - List doctors with pagination
- `GET /api/mobile/search/doctors` - Search doctors
- `GET /api/doctors/:id` - Get doctor details

### Appointments
- `POST /api/mobile/appointments` - Create appointment
- `GET /api/mobile/appointments` - Get user appointments
- `PUT /api/appointments/:id` - Update appointment
- `DELETE /api/appointments/:id` - Cancel appointment

## 🔧 Configuration

### Environment Variables
```bash
# Backend (.env)
DATABASE_URL=postgres://user:pass@localhost:5432/hospital_db
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_PRIVATE_KEY=your-private-key
FIREBASE_CLIENT_EMAIL=your-client-email
```

### Network Security
The app includes network security configuration for development:
- Allows cleartext traffic to local development servers
- Configured for physical device testing
- **Note**: Remove cleartext traffic in production builds

## 📱 Screenshots

### Authentication Flow
- Login screen with email/password and Google Sign-In
- Sign-up screen with validation
- Logout confirmation dialog

### Doctor Discovery
- Doctor listing with search and filter
- Doctor cards with specialization and experience
- Available time slots display

## 🛠️ Development

### Project Structure
```
Swaasthya/
├── app/                          # Android application
│   ├── src/main/java/com/example/swaasthya/
│   │   ├── data/                 # Data layer
│   │   │   ├── api/              # API interfaces
│   │   │   ├── model/            # Data models
│   │   │   └── repository/       # Repository implementations
│   │   ├── di/                   # Dependency injection
│   │   ├── presentation/         # UI layer
│   │   │   ├── screen/           # Composable screens
│   │   │   └── viewmodel/        # ViewModels
│   │   └── ui/theme/             # UI theme and colors
│   └── src/main/res/             # Resources
├── backend/                      # Go backend
│   ├── handlers/                 # HTTP handlers
│   ├── models/                   # Data models
│   ├── database/                 # Database connection
│   └── middleware/               # Middleware functions
└── docs/                         # Documentation
```

### Key Dependencies

#### Android
- `compose-bom` - Jetpack Compose
- `firebase-bom` - Firebase services
- `hilt-android` - Dependency injection
- `retrofit` - HTTP client
- `navigation-compose` - Navigation

#### Backend
- `github.com/gin-gonic/gin` - Web framework
- `github.com/lib/pq` - PostgreSQL driver
- `firebase.google.com/go/v4` - Firebase Admin SDK

## 🧪 Testing

### Backend Testing
```bash
cd backend
go test ./...
```

### Frontend Testing
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## 🚀 Deployment

### Backend Deployment
1. Build Docker image:
   ```bash
   docker build -t swaasthya-backend .
   ```

2. Deploy with Docker Compose:
   ```bash
   docker-compose up -d
   ```

### Frontend Deployment
1. Generate release APK:
   ```bash
   ./gradlew assembleRelease
   ```

2. Sign and upload to Play Store

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- **Kotlin**: Follow official Kotlin coding conventions
- **Go**: Use `gofmt` and follow Go best practices
- **Commits**: Use conventional commit messages

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Firebase for authentication services
- Google for Material Design components
- Jetpack Compose team for modern UI framework
- Go community for excellent web framework

## 📞 Support

For support, email support@swaasthya.com or create an issue in this repository.

## 🔮 Roadmap

- [ ] Appointment booking system
- [ ] Push notifications
- [ ] Video consultation
- [ ] Prescription management
- [ ] Health records
- [ ] Multi-language support
- [ ] Dark mode
- [ ] Offline mode enhancements

---

**Made with ❤️ for better healthcare access**
