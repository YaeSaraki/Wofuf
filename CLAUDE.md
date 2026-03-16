# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Wofuf is a Minecraft server management platform with a microservices architecture using Domain-Driven Design (DDD). The project consists of a Kotlin/Spring Boot backend and a Vue.js/TypeScript frontend.

## Frontend Structure (WofuF/)

### Architecture
```
WofuF/src/
├── modules/              # Feature modules
│   ├── app/              # App shell and layout
│   ├── auth/             # Authentication module
│   ├── forum/            # Forum functionality
│   ├── players/          # Player statistics and management
│   └── serverStats/      # Server statistics
├── views/                # Route-level components
├── shared/
│   ├── assets/           # Global assets and styles
│   ├── components/       # Reusable components
│   ├── services/         # API services
│   ├── infra/            # Infrastructure (router, etc.)
│   └── utils/            # Utility functions
└── main.ts               # Application entry point
```

### Key Frontend Patterns

#### Module Structure
Each module follows a consistent pattern:
- `components/` - Vue components (forms, displays, etc.)
- `services/` - API service layer
- `dtos/` - Data transfer objects/TypeScript interfaces
- `config/` - Module configuration
- `utils/` - Module-specific utilities
- `index.ts` - Module registration and exports

#### Routing
- Vue Router with lazy-loaded routes
- Route definitions in `src/shared/infra/router/index.ts`
- Views in `src/views/` correspond to routes

#### State Management
- Services handle API communication and state
- Pinia stores may be used (check `shared/core/`)

#### Styling
- PrimeVue component library with Aura theme
- Tailwind CSS for utility classes
- Global styles in `src/shared/assets/`

## Backend Patterns

### DDD Architecture
```
modules/{module}/
├── domain/           # Entities, value objects, events
├── infra/            # Repositories, external integrations
├── services/         # Application services
├── useCases/         # Use case implementations
├── dtos/             # Data transfer objects
├── mappers/          # Object mappers
└── config/           # Module-specific configuration
```

### Key Backend Patterns

#### Use Case Pattern
Each use case follows this structure:
```
useCases/{UseCaseName}/
├── {UseCaseName}Controller.kt    # REST controller
├── {UseCaseName}UseCase.kt       # Business logic
├── {UseCaseName}Dto.kt           # Request/Response DTOs
├── {UseCaseName}Errors.kt        # Custom error classes
└── {UseCaseName}DtoMap.kt        # Domain ↔ DTO mapping
```

#### Entity Pattern
```kotlin
class Entity private constructor(
    props: EntityProps,
    id: UniqueEntityId?,
) : AggregateRoot<EntityProps>(props, id)
```

#### Repository Pattern
- Interface in domain layer
- Implementation in infra layer using JPA/Hibernate
- Transactional event publishing with Eventuate Tram

## Common Commands

### Frontend (WofuF/)
```bash
npm run dev           # Start development server
npm run build         # Production build
npm run preview       # Preview production build
npm run lint          # ESLint check with auto-fix
npm run format        # Prettier formatting
npm run type-check    # TypeScript type checking
```

### Backend (root)
```bash
./gradlew build                      # Build all modules
./gradlew :module-name:build         # Build specific module
./gradlew bootRun                    # Run application
./gradlew test                       # Run all tests
./gradlew :module-name:bootRun       # Run specific service
docker-compose up -d                 # Start infrastructure services
```

## Development Guidelines

### Frontend Development
1. **Components**: Use Vue 3 Composition API with TypeScript
2. **Services**: Implement API clients using Axios
3. **Styling**: Use Tailwind CSS classes with PrimeVue components
4. **Routing**: Add new routes to `src/shared/infra/router/index.ts`
5. **State**: Use services for API state management

### Backend Development
1. **Use Cases**: Follow the DDD use case pattern exactly
2. **Entities**: Use `AggregateRoot<T>` with private constructors
3. **Value Objects**: Create for domain primitives
4. **Events**: Use Eventuate Tram for domain events
5. **Error Handling**: Use `Result.Failure` with custom error classes

### AI Agent Guidelines
When generating code:
1. Follow existing patterns in the codebase exactly
2. Maintain consistency with naming conventions
3. Include proper error handling for all operations
4. Use DTOs for all API requests/responses
5. Keep domain logic in domain layer, not controllers

### Code Quality
- **Backend**: Run `./gradlew :module-name:compileKotlin` to verify compilation
- **Frontend**: Run `npm run type-check` for TypeScript validation
- **Format**: Use Prettier for frontend, spotless for backend
- **Lint**: ESLint for Vue/TypeScript, ktlint for Kotlin

## Important Configuration

### Frontend
- `WofuF/package.json` - Dependencies and scripts
- `WofuF/tsconfig.json` - TypeScript configuration
- `WofuF/vite.config.ts` - Vite build configuration
- `WofuF/.prettierrc.json` - Code formatting rules

### Backend
- `src/main/resources/application.yml` - Main application configuration
- `docker-compose.yml` - Local development services
- `gradle/libs.versions.toml` - Dependency versions

### Environment
```bash
# Database
MYSQL_HOST=localhost:3307
MYSQL_DATABASE=Woffo_db
MYSQL_USER=Woffo_db_user
MYSQL_PASSWORD=password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6380

# JWT
JWT_SECRET=your-secret-key
```

## Testing
- **Frontend**: No test framework configured yet
- **Backend**: JUnit 5 with KotlinTest
- **E2E**: Not currently configured

## Common Issues and Solutions

### Frontend Issues
- **Module not found**: Check `tsconfig.json` path mappings
- **Type errors**: Run `npm run type-check` to identify issues
- **Styling**: Ensure PrimeVue theme is properly configured

### Backend Issues
- **Database connection**: Verify MySQL is running on port 3307
- **Kafka issues**: Check Kafka container is running
- **Redis connection**: Verify Redis is accessible on port 6380
- **Eventuate Tram**: Ensure events are published transactionally

## Security Considerations
- JWT tokens with refresh mechanism
- Password hashing with secure algorithms
- Input validation and sanitization
- CORS configuration for frontend integration
- Rate limiting on API endpoints

## API Integration
Frontend services should:
1. Use Axios for HTTP requests
2. Handle JWT authentication tokens
3. Implement error handling for API failures
4. Use TypeScript interfaces for request/response types
5. Follow RESTful conventions for endpoint design