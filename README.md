# Wofuf - Minecraft Server Management Platform

[![License: ISC](https://img.shields.io/badge/License-ISC-blue.svg)](https://opensource.org/licenses/ISC)

Wofuf is a comprehensive Minecraft server management platform built with Domain-Driven Design (DDD) principles. It provides a modern web interface for managing players, forums, and server statistics with real-time data collection and event-driven architecture.

## 🌟 Features

- **Player Management**: Real-time player statistics, skin management, and activity tracking
- **Forum System**: Full-featured discussion forums with posts, comments, and voting
- **User Authentication**: JWT-based authentication with role-based access control
- **Event-Driven Architecture**: Reliable event publishing using Eventuate Tram
- **Microservices Design**: Modular architecture with separate services for different domains
- **Modern Tech Stack**: Kotlin/Spring Boot backend with Vue.js/TypeScript frontend

## 🏗️ Architecture

### Backend Architecture
```
Wofuf (Root)
├── modules-users/          # User management & authentication
├── modules-players/        # Player data & statistics
├── modules-forum/          # Forum posts & comments
├── infra-discovery/        # Service discovery (Eureka)
├── infra-gateway/          # API Gateway (Spring Cloud Gateway)
├── shared-auth/            # Shared authentication components
└── shared/                 # Common domain & infrastructure code
```

### Data Architecture
- **Primary Database**: MySQL with separate databases per service
- **Cache**: Redis for session management and caching
- **Message Queue**: Kafka for event-driven communication
- **Service Discovery**: Eureka for microservice registration

### Technology Stack

#### Backend
- **Framework**: Spring Boot 4.x
- **Language**: Kotlin
- **ORM**: MyBatis with Spring Data JPA
- **Event Bus**: Eventuate Tram
- **Security**: Spring Security with JWT
- **API**: RESTful APIs with Spring Web MVC

#### Frontend
- **Framework**: Vue 3 with Composition API
- **Build Tool**: Vite
- **UI Library**: PrimeVue
- **Styling**: Tailwind CSS
- **Language**: TypeScript

#### Infrastructure
- **Database**: MySQL 8.x
- **Cache**: Redis 7.x
- **Message Queue**: Kafka 3.x
- **Container**: Docker & Docker Compose
- **Build**: Gradle (Kotlin DSL)

## 🚀 Quick Start

### Prerequisites
- **Java**: JDK 21 or later
- **Node.js**: v20.19.0 or later
- **Docker**: v24.0 or later
- **Docker Compose**: v2.0 or later

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/YaeSaraki/Wofuf.git
   cd Wofuf
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose up -d
   ```

3. **Backend setup**
   ```bash
   # Build all modules
   ./gradlew build

   # Start the application
   ./gradlew bootRun
   ```

4. **Frontend setup**
   ```bash
   cd WofuF
   npm install
   npm run dev
   ```

5. **Access the application**
   - **Frontend**: http://localhost:5173
   - **Backend API**: http://localhost:8005
   - **API Gateway**: http://localhost:8080

### Database Configuration
The application uses the following default database configuration:
- **Host**: localhost:3307
- **Database**: Woffo_db
- **Username**: Woffo_db_user
- **Password**: password

## 📁 Project Structure

```
Wofuf/
├── WofuF/                  # Vue.js Frontend
├── Wofuf-modules/          # Backend Business Modules
│   ├── Wofuf-forum/        # Forum Management
│   ├── Wofuf-players/      # Player Management
│   └── Wofuf-users/        # User Management
├── Wofuf-infra/            # Infrastructure Services
│   ├── Wofuf-discovery/    # Service Discovery
│   └── Wofuf-gateway/      # API Gateway
├── Wofuf-shared/           # Shared Components
│   ├── Wofuf-auth/         # Authentication
│   └── Wofuf-shared/       # Common Code
├── document/               # Documentation
│   ├── Agent/              # AI Agent Guidelines
│   ├── diagrams/           # Architecture Diagrams
│   ├── images/             # Screenshots & Images
│   └── UseCase.md          # Use Case Generation Guide
├── docker-compose.yml      # Local Development Stack
└── build.gradle.kts        # Build Configuration
```

## 🛠️ Development

### Backend Development

#### Building
```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :modules-forum:build

# Clean build
./gradlew clean build
```

#### Running
```bash
# Run specific service
./gradlew :modules-players:bootRun

# Run with custom profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

#### Testing
```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew :modules-forum:test

# Run with coverage
./gradlew test jacocoTestReport
```

### Frontend Development

#### Development Server
```bash
cd WofuF
npm run dev          # Start dev server
npm run build        # Production build
npm run preview      # Preview production build
```

#### Code Quality
```bash
npm run lint         # ESLint check
npm run format       # Prettier formatting
npm run type-check   # TypeScript type checking
```

### Code Generation

This project includes AI agent guidelines for automated code generation:

- **Use Case Generation**: See `document/agents/UseCase.md`
- **General Guidelines**: See `AGENTS.md`
- **Architecture Patterns**: See `document/diagrams/`

## 📚 API Documentation

### Authentication
The API uses JWT tokens for authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

### Key Endpoints

#### Players API
- `GET /api/v1/players/playerNameOrUuid/{nameOrUuid}` - Get player by name/UUID
- `GET /api/v1/players/random` - Get random players
- `GET /api/v1/players/{playerId}/skin` - Get player skin

#### Forum API
- `GET /api/v1/forum/posts` - Get posts
- `POST /api/v1/forum/posts` - Create post
- `GET /api/v1/forum/posts/{slug}/comments` - Get post comments
- `POST /api/v1/forum/comments/{commentId}/replies` - Reply to comment

#### User API
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/register` - User registration
- `GET /api/v1/auth/me` - Get current user

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Development Workflow
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Make your changes following our coding standards
4. Add tests for new functionality
5. Ensure all tests pass: `./gradlew test`
6. Submit a pull request

### Coding Standards
- **Backend**: Follow Kotlin coding conventions
- **Frontend**: Follow Vue.js and TypeScript best practices
- **Commits**: Use conventional commit format
- **Documentation**: Update relevant docs for changes

### Code Generation
When adding new features, consider using our AI agent guidelines:
- Review `AGENTS.md` for general patterns
- Follow `document/agents/UseCase.md` for new use cases
- Maintain consistency with existing code structure

## 📄 Documentation

- **Architecture Diagrams**: `document/diagrams/`
- **API Documentation**: `document/diagrams/api.mermaid`
- **Use Case Guidelines**: `document/agents/UseCase.md`
- **Event Architecture**: `document/agents/EventuateTramIntegration.md`

## 🔧 Configuration

### Application Configuration
Key configuration files:
- `src/main/resources/application.yml` - Main configuration
- `docker-compose.yml` - Local development services

### Environment Variables
```bash
# Database
MYSQL_HOST=localhost
MYSQL_PORT=3307
MYSQL_DATABASE=Woffo_db
MYSQL_USER=Woffo_db_user
MYSQL_PASSWORD=password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6380

# Kafka
KAFKA_HOST=localhost
KAFKA_PORT=9092

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=3600000
```

## 🐛 Troubleshooting

### Common Issues

**Database Connection Issues**
```bash
# Check if MySQL container is running
docker ps | grep mysql

# View MySQL logs
docker logs Woffo_db
```

**Redis Connection Issues**
```bash
# Check Redis connectivity
docker exec -it Woffo_redis redis-cli ping
```

**Build Issues**
```bash
# Clean and rebuild
./gradlew clean build

# Check Gradle version compatibility
./gradlew --version
```

## 📈 Performance

### Monitoring
- **Health Checks**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Info**: `/actuator/info`

### Optimization Tips
- Enable Redis caching for frequently accessed data
- Use database indexes on commonly queried fields
- Implement pagination for large result sets
- Monitor Kafka consumer lag

## 🔒 Security

- JWT tokens with configurable expiration
- Password hashing with secure algorithms
- Input validation and sanitization
- CORS configuration for frontend integration
- Rate limiting considerations

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/YaeSaraki/Wofuf/issues)
- **Discussions**: [GitHub Discussions](https://github.com/YaeSaraki/Wofuf/discussions)
- **Email**: ikaraswork@iCloud.com

## 🙏 Acknowledgments

- **Khalil Stemmler**: Original DDD forum architecture inspiration
- **Spring Community**: Excellent framework and documentation
- **Vue.js Community**: Modern frontend development tools

---

## 📜 License

ISC License - see [LICENSE](LICENSE.md) for details.

### Copyright Notice

- Copyright (c) 2026, [YaeSaraki](https://github.com/YaeSaraki)
- Copyright (c) 2019, [Khalil Stemmler](https://khalilstemmler.com)

Project architecture and some code logic based on Khalil Stemmler's ddd-forum. See [NOTICE](NOTICE.md) for details.

**Please retain all copyright notices when using this project.**
