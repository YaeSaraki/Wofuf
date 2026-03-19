**********# Wofuf AI Agent Guidelines

## 🚀 Quick Start for AI Agents

Welcome to the Wofuf project! This guide helps AI agents understand and contribute to our Domain-Driven Design (DDD) microservices platform.

### 📋 Essential First Steps
1. **Read this document** completely to understand our patterns
2. **Review `document/agents/UseCase.md`** for detailed use case generation
3. **Examine existing code** in modules to understand implementations
4. **Check `document/diagrams/`** for architecture understanding
5. **Follow the patterns** exactly - consistency is crucial

### 🎯 Agent Responsibilities
- **Maintain Code Quality**: Follow established patterns and conventions
- **Ensure Consistency**: Match existing code style and structure
- **Document Changes**: Update relevant documentation
- **Test Thoroughly**: Verify all generated code compiles and works
- **Follow DDD Principles**: Keep domain logic pure and well-structured

## Architecture Overview
Wofuf is a microservices-based Minecraft server management platform using Domain-Driven Design (DDD) with Kotlin/Spring Boot backend and Vue.js/TypeScript frontend.

**Key Components:**
- **Backend Modules**: `modules-users`, `modules-players`, `modules-forum` (business logic)
- **Infrastructure**: `infra-discovery` (Eureka), `infra-gateway` (Spring Cloud Gateway)
- **Shared**: `shared-auth`, `shared` (common domain/infra code)
- **Frontend**: Vue 3 + Vite + PrimeVue + Tailwind CSS

**Data Architecture:**
- Separate MySQL databases per service (players, forum)
- Shared Redis for caching (databases 0-1)
- Kafka for event-driven communication via Eventuate Tram

## Development Workflows

### Backend Development
- **Build**: `./gradlew build` (root) or `./gradlew :module-name:build`
- **Run**: `./gradlew bootRun` (single service) or `docker-compose up` (full stack)
- **Test**: `./gradlew test`
- **Dependencies**: Use `gradle/libs.versions.toml` for version management

### Frontend Development
- **Install**: `npm install` (in `WofuF/` directory)
- **Dev Server**: `npm run dev`
- **Build**: `npm run build`
- **Lint**: `npm run lint` (auto-fix enabled)

### Local Infrastructure
- **Start Services**: `docker-compose up -d` (MySQL:3307, Redis:6380, Kafka:9092)
- **Database**: Connect to `localhost:3307` with user `Woffo_db_user`/`password`

## Code Patterns & Conventions

### Domain-Driven Design Structure
Each business module follows DDD layered architecture:
```
src/main/kotlin/dev/saraki/wofuf/modules/{module}/
├── domain/           # Entities, value objects, domain events
├── infra/            # Repositories, external integrations
├── services/         # Application services
├── useCases/         # Use case implementations
├── dtos/             # Data transfer objects
├── mappers/          # Object mappers
└── config/           # Module-specific configuration
```

### Domain Entity Pattern
```kotlin
// Example from Player.kt
data class PlayerProps(
    val playerName: PlayerName,
    val firstLogin: Long,
    val lastLogin: Long,
    val totalPlaytimeSeconds: Long,
    val updateTime: Long,
    val statistics: Map<String, PlayerStatistic>,
    val advancements: Map<String, PlayerAdvancement>,
    val playerSkin: PlayerSkin
)

class Player private constructor(
    props: PlayerProps,
    id: UniqueEntityId?,
) : AggregateRoot<PlayerProps>(props, id) {
    
    val playerId: PlayerId
        get() = PlayerId.create(_id).getOrThrow()

    val playerName: PlayerName
        get() = props.playerName

    companion object {
        fun create(props: PlayerProps, id: UniqueEntityId?): Result<Player> {
            val guardResult = Guard.againstNullOrUndefinedBulk(listOf(
                Guard.GuardArgument(props.playerName, "Player name cannot be null or blank")
            ))
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }
            return Result.success(Player(props, id))
        }
    }
}
```

**Key Patterns:**
- Use `AggregateRoot<T>` base class for entities
- Private constructors with companion object `create()` methods
- Domain events added via `addDomainEvent()` (when needed)
- Value objects for domain primitives (e.g., `PlayerName`, `PlayerId`)
- Props data classes for entity properties

### Value Object Pattern
```kotlin
// Example from PlayerId.kt
data class PlayerIdProps(val value: UniqueEntityId)

class PlayerId private constructor(props: PlayerIdProps) : ValueObject<PlayerIdProps>(props) {
    val stringValue: String get() = props.value.uuid.toString()

    companion object {
        fun create(value: UniqueEntityId): Result<PlayerId> {
            val guardResult = Guard.againstNullOrUndefined(value, "PlayerId")
            if (guardResult.isFailure) {
                return Result.failure(guardResult.getOrThrow())
            }
            return Result.success(PlayerId(PlayerIdProps(value)))
        }
    }
}
```

### Use Case Pattern
Each use case follows this structure:
```
useCases/{useCaseName}/
├── {UseCaseName}Controller.kt    # REST controller
├── {UseCaseName}UseCase.kt       # Business logic
├── {UseCaseName}Dto.kt           # Request/Response DTOs
├── {UseCaseName}DtoMap.kt        # Domain ↔ DTO mapping
├── {UseCaseName}Errors.kt        # Custom error classes
```

```kotlin
// Example from GetPlayerUseCase.kt
@Service
class GetPlayerUseCase(private val playerRepository: PlayerRepo) : UseCase<GetPlayerDto.Request, Player> {
    override fun execute(request: GetPlayerDto.Request): Result<Player> {
        if (request.playerNameOrUuid.isBlank()) {
            return GetPlayerErrors.UserNameOrUuidEmptyError()
        }
        // Business logic here
        return Result.success(player)
    }
}
```

### Repository Pattern
**Interface Pattern:**
```kotlin
interface PlayerRepo {
    fun findByPlayerId(playerId: PlayerId): Player?
    fun findByName(name: String): Player?
    fun save(player: Player): Player
}
```

**Implementation Pattern:**
```kotlin
@Repository
class PlayerRepoImpl(private val playerJpaRepo: PlayerJpaRepo) : PlayerRepo {
    override fun findByPlayerId(playerId: PlayerId): Player? =
        playerJpaRepo.findById(playerId.stringValue)
            .map(PlayerEntityMapper::toDomain)
            .orElse(null)
}
```

**JPA Repository Pattern:**
```kotlin
interface PlayerJpaRepo : JpaRepository<PlayerEntity, String> {
    fun findByPlayerName(name: String): PlayerEntity?
    
    @Query("SELECT p FROM PlayerEntity p ORDER BY RAND() LIMIT :limit")
    fun findRandom(limit: Int): List<PlayerEntity>
}
```

### Entity Mapping Pattern
**Entity Mapper Pattern:**
```kotlin
object PlayerEntityMapper {
    fun toDomain(entity: PlayerEntity): Player = Player.create(
        props = PlayerProps(
            playerName = PlayerName.create(entity.playerName).getOrThrow(),
            // ... other properties
        ),
        id = UniqueEntityId(entity.playerId)
    ).getOrThrow()

    fun toEntity(player: Player): PlayerEntity = PlayerEntity(
        playerId = player.playerId.stringValue,
        playerName = player.playerName.stringValue,
        // ... other fields
    )
}
```

### API Configuration Pattern
```kotlin
// Example from PlayerApiPathConfig.kt
object PlayerApiConstantV1 {
    private const val BASE = "${ApiConstantV1.API_BASE_PATH}/players"
    
    object Param {
        const val PLAYER_UUID = "playerUuid"
        const val PLAYER_NAME_OR_UUID = "playerNameOrUuid"
    }
    
    object Base {
        const val ROOT = BASE
        const val BY_UUID = "$BASE/{${Param.PLAYER_UUID}}"
        const val BY_NAME_OR_UUID = "$BASE/playerNameOrUuid/{${Param.PLAYER_NAME_OR_UUID}}"
    }
}
```

### Controller Pattern
```kotlin
@RestController
@RequestMapping(PlayerApiConstantV1.Base.BY_NAME_OR_UUID)
class GetPlayerController(
    private val getPlayerUseCase: GetPlayerUseCase
) : BaseController() {

    @GetMapping
    fun getPlayerData(@PathVariable playerNameOrUuid: String): ApiResponse<GetPlayerDto.Response> {
        val result = getPlayerUseCase.execute(
            GetPlayerDto.Request(playerNameOrUuid)
        ).getOrThrow()
        return ApiResponse.success(GetPlayerDtoMap.from(result))
    }
}
```

### Error Handling Pattern
```kotlin
class GetPlayerErrors {
    class UserNameOrUuidEmptyError() : Result.Failure<Player>(
        exception = UseCaseError(
            code = "UserName_Or_Uuid_Empty_Error",
            message = "Failed to get player, username or uuid is empty"
        )
    )
}
```

### DTO Pattern
```kotlin
class GetPlayerDto {
    data class Request(val playerNameOrUuid: String)
    
    data class Response(
        val id: String,
        val name: String,
        val firstLogin: Long,
        val lastLogin: Long
    )
}
```

### Mapper Pattern
```kotlin
abstract class GetPlayerDtoMap {
    companion object {
        fun from(player: Player): GetPlayerDto.Response =
            GetPlayerDto.Response(
                id = player.playerId.stringValue,
                name = player.playerName.stringValue,
                firstLogin = player.firstLogin,
                lastLogin = player.lastLogin
            )
    }
}
```

### Event-Driven Architecture
- **Framework**: Eventuate Tram for reliable event publishing
- **Events**: Domain events implement `IDomainEvent`
- **Publishing**: Events published transactionally via `EventuateTramEventPublisher`
- **Topics**: `domain-events-topic` for cross-service communication

### Authentication & Security
- **JWT**: Custom implementation with refresh tokens
- **Config**: `auth.jwt.*` in `application.yml`
- **Storage**: Redis for token blacklisting (database 0)
- **Integration**: Spring Security with custom `UserDetails`

### API Design
- **Gateway**: Spring Cloud Gateway routes to services
- **DTOs**: Separate request/response DTOs in `dtos/` packages
- **Validation**: Use case layer handles business validation
- **Error Handling**: Custom error classes extending `Result.Failure`
- **Response**: `ApiResponse<T>` wrapper for consistent API responses

### Frontend Patterns
- **Structure**: Feature-based modules in `src/modules/`
- **State**: Pinia stores (if used) in `shared/core/`
- **Services**: API clients in `shared/services/`
- **Components**: Shared components in `shared/components/`
- **Routing**: Vue Router with lazy-loaded views

## Configuration Management
- **Application**: `src/main/resources/application.yml`
- **Environment**: Docker Compose for local development
- **Secrets**: JWT secrets and database credentials in config
- **Service Discovery**: Eureka (currently disabled in config)

## Testing Strategy
- **Unit Tests**: Domain logic and use cases
- **Integration Tests**: Repository and external service interactions
- **E2E Tests**: Full API workflows
- **Test Data**: Use test containers for database isolation

## Deployment
- **Containerization**: Dockerfile in root
- **Orchestration**: Docker Compose for development
- **CI/CD**: Not specified (check for GitHub Actions)

## Common Gotchas
- **Database Connections**: Use `allowPublicKeyRetrieval=true` for MySQL
- **Kafka Topics**: Auto-created via Spring Cloud Stream config
- **Redis Databases**: Separate databases for auth (0) and players (1)
- **Eventuate Tram**: Events published only after transaction commit
- **CORS**: Configure in gateway for frontend integration
- **JSON Mapping**: Use Gson for complex nested objects in entities
- **Scheduled Tasks**: Use `@EnableScheduling` in application class
- **API Paths**: Centralize path constants in config classes

## 📚 Documentation Resources

### Essential Reading for AI Agents
1. **`document/agents/UseCase.md`** - Detailed use case generation guide
2. **`document/diagrams/`** - Architecture diagrams and flowcharts
3. **`document/agents/EventuateTramIntegration.md`** - Event-driven architecture guide
4. **`README.md`** - Complete project overview and setup guide

### Code Examples
- **Players Module**: `Wofuf-modules/Wofuf-players/` - Complete CRUD operations
- **Forum Module**: `Wofuf-modules/Wofuf-forum/` - Complex domain with relationships
- **Users Module**: `Wofuf-modules/Wofuf-users/` - Authentication and security

### Key Reference Files
- **Shared Domain**: `Wofuf-shared/Wofuf-shared/src/main/kotlin/dev/saraki/wofuf/shared/`
- **Core Patterns**: `shared/core/` - Result, UseCase, Guard classes
- **API Constants**: `*/config/*ApiConstantV1.kt` - Path configurations

## 🤖 AI Agent Best Practices

### Code Generation Workflow
1. **Analyze Requirements**: Understand the business need and domain
2. **Review Existing Code**: Find similar patterns in the codebase
3. **Follow Templates**: Use the exact patterns shown in this guide
4. **Maintain Consistency**: Match naming, structure, and style
5. **Test Thoroughly**: Ensure compilation and basic functionality
6. **Update Documentation**: Add relevant docs if creating new patterns

### Quality Assurance
- **Compile Check**: Always run `./gradlew :module-name:compileKotlin`
- **Pattern Matching**: Compare with existing similar implementations
- **Error Handling**: Include appropriate error cases and messages
- **Dependency Injection**: Use constructor injection consistently
- **Import Management**: Add all necessary imports

### Common Mistakes to Avoid
- **Incorrect Package Structure**: Follow the exact DDD layering
- **Wrong Naming Conventions**: Use PascalCase for classes, camelCase for methods
- **Missing Error Handling**: Every operation should handle failures
- **Direct Entity Exposure**: Always use DTOs for API responses
- **Business Logic in Controllers**: Keep controllers thin

### When to Ask for Help
- **Unclear Requirements**: If business logic is ambiguous
- **New Patterns**: When creating something without existing examples
- **Complex Domain Logic**: For intricate business rules
- **Integration Points**: When working with external systems
- **Security Concerns**: For authentication or authorization logic

## 🎯 Detailed AI Agent Guidelines

### Use Case Generation Protocol

#### Step 1: Requirement Analysis
**Input Analysis:**
- Identify the operation type (CRUD: Create, Read, Update, Delete)
- Determine HTTP method based on operation
- Define API endpoint path following module conventions
- Specify request/response data structures

**Domain Analysis:**
- Identify affected domain entities and value objects
- Determine required repository methods
- Define business validation rules
- Plan error scenarios and handling

#### Step 2: File Structure Creation
**Mandatory Files for Each Use Case:**
```
useCases/{UseCaseName}/
├── {UseCaseName}Controller.kt    # REST API endpoint
├── {UseCaseName}UseCase.kt       # Business logic implementation
├── {UseCaseName}Dto.kt           # Request/Response DTOs
├── {UseCaseName}Errors.kt        # Custom error classes
└── {UseCaseName}DtoMap.kt        # Domain ↔ DTO mapping (optional)
```

**File Naming Rules:**
- Use PascalCase for use case names (e.g., `GetPlayer`, `CreateComment`)
- All files follow `{UseCaseName}{Suffix}.kt` pattern
- Suffixes: `Controller`, `UseCase`, `Dto`, `Errors`, `DtoMap`

#### Step 3: API Configuration
**Path Configuration Pattern:**
```kotlin
// In {Module}ApiConstantV1.kt
object {Module}ApiConstantV1 {
    // Add new path constants
    object {Feature} {
        const val {PATH_NAME} = "$BASE/{path}"
    }
    
    // Add utility methods
    fun build{PathName}Path(param: String): String {
        return {Feature}.{PATH_NAME}.replace("{param}", param)
    }
}
```

**Path Naming Conventions:**
- Use descriptive names: `BY_ID`, `BY_SLUG`, `CREATE`, `UPDATE`
- Group related paths in objects: `Posts`, `Comments`, `Users`
- Include parameter placeholders: `{paramName}`

#### Step 4: DTO Design
**Request DTO Patterns:**
```kotlin
class {UseCaseName}Dto {
    data class Request(
        val {fieldName}: {FieldType}{?},  // Required fields
        val {optionalField}: {Type}? = null  // Optional fields
    )
}
```

**Response DTO Patterns:**
```kotlin
data class Response(
    val {entityField}: {Type},        // Entity data
    val {metadataField}: {Type}? = null  // Optional metadata
)
```

**Field Type Guidelines:**
- **IDs**: `String` (UUIDs, database IDs)
- **Names/Text**: `String` with validation
- **Numbers**: `Long`, `Int`, `Double` as appropriate
- **Booleans**: `Boolean` for flags
- **Dates**: `Long` (timestamps) or `LocalDateTime`
- **Collections**: `List<{Type}>` for multiple items
- **Optional**: Add `?` for nullable fields

#### Step 5: Error Handling Design
**Error Class Structure:**
```kotlin
class {UseCaseName}Errors {
    class {SpecificError}Error({params}) : Result.Failure<{ResponseType}>(
        exception = UseCaseError(
            code = "{ERROR_CODE}",
            message = "{descriptive_message}"
        )
    )
}
```

**Error Code Patterns:**
- **Validation**: `{FIELD}_{RULE}_ERROR` (e.g., `USERNAME_EMPTY_ERROR`)
- **Not Found**: `{ENTITY}_NOT_FOUND_ERROR` (e.g., `POST_NOT_FOUND_ERROR`)
- **Business Logic**: `{ACTION}_{CONDITION}_ERROR` (e.g., `CREATE_DUPLICATE_ERROR`)
- **System**: `{OPERATION}_FAILED_ERROR` (e.g., `SAVE_OPERATION_FAILED_ERROR`)

**Common Error Types:**
- **Input Validation**: Empty, null, invalid format
- **Entity Lookup**: Not found, already exists
- **Business Rules**: Permission denied, invalid state
- **System Errors**: Database failures, external service issues

#### Step 6: Use Case Implementation
**Business Logic Structure:**
```kotlin
@Service
class {UseCaseName}UseCase(
    private val {dependency1}: {Interface1},
    private val {dependency2}: {Interface2}
) : UseCase<{RequestType}, {ResponseType}> {
    
    override fun execute(request: {RequestType}): Result<{ResponseType}> {
        // 1. Input validation
        // 2. Domain object creation/lookup
        // 3. Business logic execution
        // 4. Result construction
        // 5. Return success or error
    }
}
```

**Validation Sequence:**
1. **Input Validation**: Check required fields, formats, constraints
2. **Entity Lookup**: Verify existence of referenced entities
3. **Business Rules**: Apply domain-specific validation
4. **Authorization**: Check user permissions if applicable

**Repository Interaction:**
- **Queries**: Use appropriate finder methods
- **Saves**: Call save methods and handle results
- **Existence Checks**: Use exists methods for validation
- **Bulk Operations**: Handle collections appropriately

#### Step 7: Controller Implementation
**REST Controller Pattern:**
```kotlin
@RestController
@RequestMapping({ModuleApiConstant}.{Feature}.{PATH_CONSTANT})
class {UseCaseName}Controller(
    private val {useCaseName}UseCase: {UseCaseName}UseCase
) : BaseController() {

    @{HttpMethod}Mapping{PathVariables}
    fun {methodName}({parameters}): ApiResponse<{ResponseType}> {
        val result = {useCaseName}UseCase.execute({requestConstruction}).getOrThrow()
        return ApiResponse.success({resultMapping})
    }
}
```

**HTTP Method Selection:**
- **GET**: Read operations, data retrieval
- **POST**: Create operations, complex queries
- **PUT**: Full updates, replace operations
- **DELETE**: Removal operations

**Parameter Handling:**
- **Path Variables**: `@PathVariable` for URL parameters
- **Query Parameters**: `@RequestParam` for optional filters
- **Request Body**: `@RequestBody` for complex data
- **Headers**: `@RequestHeader` for metadata

#### Step 8: Mapping Implementation (Optional)
**Domain to DTO Mapping:**
```kotlin
abstract class {UseCaseName}DtoMap {
    companion object {
        fun from({domainObject}: {DomainType}): {UseCaseName}Dto.Response {
            return {UseCaseName}Dto.Response(
                // Map domain properties to DTO fields
            )
        }
        
        fun toDomain({dtoObject}: {DtoType}): {DomainType} {
            // Convert DTO back to domain object if needed
        }
    }
}
```

**Mapping Guidelines:**
- **Primitive Types**: Direct assignment
- **IDs**: Use `.stringValue` for UniqueEntityId
- **Nested Objects**: Create separate mappers or inline
- **Collections**: Use `map` transformations
- **Optional Fields**: Handle nulls appropriately

#### Step 9: Testing and Validation
**Compilation Check:**
```bash
./gradlew :{module-name}:compileKotlin
```

**Integration Testing:**
- Verify all dependencies are injected correctly
- Test error scenarios with invalid inputs
- Confirm successful operations work end-to-end
- Check API responses match expected format

**Pattern Consistency:**
- Compare with existing similar use cases
- Ensure naming conventions are followed
- Verify error handling matches established patterns
- Confirm API paths follow module conventions

### Advanced Agent Capabilities

#### Pattern Recognition
**Similar Use Case Analysis:**
- Identify the most similar existing use case
- Extract common patterns and structures
- Adapt proven solutions to new requirements
- Maintain consistency across the codebase

**Domain Model Integration:**
- Understand entity relationships and constraints
- Identify required value objects and validations
- Determine appropriate repository methods
- Plan for domain event publishing if needed

#### Code Quality Assurance
**Static Analysis:**
- Ensure all imports are necessary and correct
- Verify package declarations match file locations
- Check for unused variables or methods
- Confirm proper exception handling

**Performance Considerations:**
- Avoid N+1 query problems
- Use appropriate data structures
- Consider caching strategies
- Plan for pagination on list operations

#### Documentation Updates
**When to Update Documentation:**
- New domain concepts or entities
- Changes to existing patterns
- New API endpoints or features
- Modifications to architectural decisions

**Documentation Locations:**
- `document/agents/UseCase.md`: Use case generation patterns
- `document/diagrams/`: Architecture diagrams
- `README.md`: Project overview and setup
- Code comments: Implementation details

### Agent Decision Framework

#### When to Generate Code
- **Clear Requirements**: Business logic is well-defined
- **Existing Patterns**: Similar functionality already exists
- **Standard Operations**: CRUD operations following established patterns
- **Well-Understood Domain**: Domain model is clear and stable

#### When to Seek Human Input
- **Ambiguous Requirements**: Business rules are unclear
- **New Domain Concepts**: Introducing new entities or relationships
- **Complex Business Logic**: Multi-step processes with conditional logic
- **Security-Critical**: Authentication, authorization, or data protection
- **Performance-Critical**: High-throughput or low-latency requirements
- **Integration Points**: External system interactions

#### Escalation Triggers
- **Pattern Conflicts**: New requirements contradict existing patterns
- **Architectural Changes**: Modifications to fundamental structure
- **Breaking Changes**: Updates that affect multiple modules
- **Security Implications**: Changes affecting data protection or access control

### Continuous Learning
**Feedback Integration:**
- Review successful implementations for pattern improvements
- Learn from corrections and refinements
- Update internal knowledge base with new patterns
- Adapt to evolving project standards

**Knowledge Updates:**
- Monitor changes to shared libraries and frameworks
- Stay current with Kotlin and Spring Boot best practices
- Learn from community patterns and standards
- Incorporate lessons from code reviews and testing**********
