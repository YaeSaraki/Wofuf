# Use Case Generation Guidelines

## Overview
This document provides detailed guidelines for AI agents to automatically generate use cases following the Wofuf project's Domain-Driven Design (DDD) patterns and Kotlin/Spring Boot conventions.

## Use Case Structure Template

### File Organization
Each use case follows this standardized structure:
```
useCases/{useCaseName}/
├── {UseCaseName}Controller.kt    # REST API endpoint
├── {UseCaseName}UseCase.kt       # Business logic implementation
├── {UseCaseName}Dto.kt           # Request/Response data transfer objects
├── {UseCaseName}DtoMap.kt        # Domain ↔ DTO mapping logic
└── {UseCaseName}Errors.kt        # Custom error classes
```

### Naming Conventions
- **Use Case Name**: PascalCase, descriptive action + subject (e.g., `GetPlayer`, `CreateComment`, `ReplyToComment`)
- **File Names**: `{UseCaseName}{Suffix}.kt` where suffix is `Controller`, `UseCase`, `Dto`, `DtoMap`, `Errors`
- **Class Names**: Follow file naming pattern
- **Method Names**: camelCase, descriptive actions

## Controller Generation Rules

### Basic Template
```kotlin
@RestController
@RequestMapping({ModuleApiConstant}.Base.{PATH_CONSTANT})
class {UseCaseName}Controller(
    private val {useCaseName}UseCase: {UseCaseName}UseCase
) : BaseController() {

    @{HttpMethod}Mapping{PathVariable}
    fun {methodName}({parameters}): ApiResponse<{UseCaseName}Dto.Response> {
        val result = {useCaseName}UseCase.execute(
            {UseCaseName}Dto.Request({requestParams})
        ).getOrThrow()
        return ApiResponse.success({mappingLogic})
    }
}
```

### HTTP Method Selection
- **GET**: Read operations (`getPlayer`, `getComments`)
- **POST**: Create operations (`createPost`, `replyToComment`)
- **PUT**: Update operations (`updateProfile`, `editComment`)
- **DELETE**: Delete operations (`deletePost`, `removeComment`)

### Path Variable Patterns
- **By ID**: `@GetMapping("/{id}")` with `@PathVariable id: String`
- **By Slug**: `@GetMapping("/slug/{slug}")` with `@PathVariable slug: String`
- **Query Params**: `@RequestParam` for optional filters
- **Request Body**: `@RequestBody` for complex data

### Response Mapping
- **Direct Response**: `ApiResponse.success(result)` for simple responses
- **With Mapping**: `ApiResponse.success({UseCaseName}DtoMap.from(result))` for domain object responses

## Use Case Implementation Rules

### Basic Template
```kotlin
@Service
class {UseCaseName}UseCase(
    private val {repositoryName}: {RepositoryInterface}
) : UseCase<{UseCaseName}Dto.Request, {UseCaseName}Dto.Response> {
    override fun execute(request: {UseCaseName}Dto.Request): Result<{UseCaseName}Dto.Response> {
        // Input validation
        if ({validationCondition}) {
            return {UseCaseName}Errors.{SpecificError}()
        }

        // Business logic
        {businessLogic}

        // Return success
        return Result.success({UseCaseName}Dto.Response({responseData}))
    }
}
```

### Validation Patterns
- **Required Fields**: Check for blank/null values
- **Domain Object Creation**: Use `.create()` methods with error handling
- **Entity Existence**: Query repositories and handle not found cases
- **Business Rules**: Custom validation logic

### Repository Interaction
- **Single Entity**: `repository.findBy{Property}({value}) ?: return {Error}()`
- **Multiple Entities**: `repository.find{Method}({params})`
- **Save Operations**: `repository.save(entity)`
- **Existence Checks**: `repository.exists({id})`

### Error Handling Flow
```kotlin
// Pattern for validation errors
if (validationResult.isFailure) {
    return {UseCaseName}Errors.{ValidationError}()
}

// Pattern for not found errors
val entity = repository.findById(id) ?: return {UseCaseName}Errors.{NotFoundError}()

// Pattern for business logic errors
if (businessRuleViolated) {
    return {UseCaseName}Errors.{BusinessError}()
}
```

## DTO Generation Rules

### Request DTO Template
```kotlin
class {UseCaseName}Dto {
    data class Request(
        val {fieldName}: {FieldType}{?},
        // ... additional fields
    )

    data class Response(
        val {fieldName}: {FieldType}{?},
        // ... additional fields
    )
}
```

### Field Type Mapping
- **String IDs**: `String` (UUIDs, slugs)
- **Numbers**: `Long`, `Int`, `Double`
- **Booleans**: `Boolean`
- **Optional Fields**: Add `?` suffix
- **Lists**: `List<{Type}>`
- **Complex Objects**: Create separate DTO classes

### Common Request Patterns
- **By ID**: `Request(val id: String)`
- **Create Operations**: `Request(val data: CreateDataDto)`
- **Query Operations**: `Request(val filters: QueryFiltersDto)`
- **Update Operations**: `Request(val id: String, val updates: UpdateDataDto)`

### Common Response Patterns
- **Single Entity**: `Response(val entity: EntityDto)`
- **List Results**: `Response(val items: List<EntityDto>)`
- **Success Confirmation**: `Response(val success: Boolean = true)`
- **Paginated Results**: `Response(val items: List<EntityDto>, val total: Int, val page: Int)`

## Error Class Generation Rules

### Basic Template
```kotlin
class {UseCaseName}Errors {
    class {ErrorName}Error({constructorParams}) : Result.Failure<{UseCaseName}Dto.Response>(
        exception = UseCaseError(
            code = "{ERROR_CODE}",
            message = "{error_message}"
        )
    )
}
```

### Error Code Patterns
- **Validation Errors**: `{FIELD}_{CONDITION}_ERROR` (e.g., `USER_ID_EMPTY_ERROR`)
- **Not Found Errors**: `{ENTITY}_NOT_FOUND_ERROR` (e.g., `POST_NOT_FOUND_ERROR`)
- **Business Logic Errors**: `{RULE}_VIOLATION_ERROR` (e.g., `DUPLICATE_SLUG_ERROR`)
- **System Errors**: `{OPERATION}_FAILED_ERROR` (e.g., `SAVE_OPERATION_FAILED_ERROR`)

### Common Error Types
- **Empty/Null Fields**: `{FieldName}EmptyError`, `{FieldName}NullError`
- **Not Found**: `{EntityName}NotFoundError`
- **Invalid Format**: `{FieldName}InvalidError`
- **Already Exists**: `{EntityName}AlreadyExistsError`
- **Permission Denied**: `{OperationName}NotAllowedError`

### Error Message Guidelines
- **Clear and Specific**: Describe exactly what went wrong
- **User-Friendly**: Avoid technical jargon
- **Actionable**: Suggest what the user can do
- **Consistent**: Follow existing error message patterns

## Mapper Generation Rules

### Basic Template
```kotlin
abstract class {UseCaseName}DtoMap {
    companion object {
        fun from({domainObject}: {DomainType}): {UseCaseName}Dto.Response =
            {UseCaseName}Dto.Response(
                {fieldMapping}
            )

        fun toDomain({dtoObject}: {DtoType}): {DomainType} {
            // Conversion logic
        }
    }
}
```

### Mapping Patterns
- **Direct Field Mapping**: `{dtoField} = domain.{domainField}`
- **ID Conversions**: `{id} = domain.{entityId}.stringValue`
- **Enum Conversions**: `{enumField} = {EnumClass}.valueOf(domain.{enumField})`
- **Nested Object Mapping**: `{nestedField} = {NestedMapper}.from(domain.{nestedObject})`

### Common Mapping Scenarios
- **Entity to DTO**: Extract primitive values and IDs
- **DTO to Domain**: Create domain objects using `.create()` methods
- **List Mapping**: `list.map { {Mapper}.from(it) }`
- **Optional Fields**: Use `?.let { mapping } ?: defaultValue`

## Complete Use Case Generation Workflow

### Step 1: Analyze Requirements
1. **Identify Operation Type**: CRUD operation (Create, Read, Update, Delete)
2. **Determine HTTP Method**: Based on operation type
3. **Define API Path**: Follow module path conventions
4. **Specify Input/Output**: Request parameters and response structure

### Step 2: Design Domain Logic
1. **Identify Domain Objects**: Entities, value objects involved
2. **Determine Repository Methods**: What data access is needed
3. **Define Business Rules**: Validation and business logic requirements
4. **Plan Error Scenarios**: What can go wrong and how to handle it

### Step 3: Generate Files in Order
1. **Create DTOs**: Define Request/Response structures
2. **Create Errors**: Define all possible error conditions
3. **Create Use Case**: Implement business logic
4. **Create Controller**: Define REST endpoint
5. **Create Mapper**: Implement domain ↔ DTO conversion (if needed)

### Step 4: Update API Configuration
1. **Add Path Constants**: Update `{Module}ApiConstantV1.kt`
2. **Add Utility Methods**: Create path builder functions if needed

### Step 5: Integration Testing
1. **Compile Check**: Ensure all code compiles
2. **Dependency Injection**: Verify all dependencies are available
3. **API Testing**: Test the endpoint with various inputs
4. **Error Handling**: Verify error responses work correctly

## Module-Specific Patterns

### Players Module Patterns
- **Entity Focus**: Player data with statistics, advancements, skin
- **Common Operations**: Get by name/UUID, get random players
- **Caching**: Heavy use of Redis for performance
- **Data Complexity**: Complex nested objects (statistics, advancements)

### Forum Module Patterns
- **Entity Relationships**: Post → Comments → Replies (hierarchical)
- **User Integration**: Member/User association required
- **Content Validation**: Text sanitization and length limits
- **Voting System**: Upvote/downvote functionality

### Users Module Patterns
- **Authentication**: JWT token management
- **Security**: Password hashing, role-based access
- **Profile Management**: User details and preferences
- **Session Management**: Login/logout with refresh tokens

## Best Practices

### Code Quality
- **Consistent Naming**: Follow established naming conventions
- **Error Handling**: Use Result<T> for all operations
- **Validation**: Validate inputs at use case level
- **Documentation**: Add meaningful comments and documentation

### Performance Considerations
- **Efficient Queries**: Use appropriate repository methods
- **Caching Strategy**: Leverage Redis for frequently accessed data
- **Pagination**: Implement for list operations
- **Lazy Loading**: Avoid N+1 query problems

### Security Considerations
- **Input Sanitization**: Clean user inputs
- **Authorization**: Check user permissions
- **Data Exposure**: Avoid exposing sensitive information
- **Rate Limiting**: Consider implementing rate limits

### Maintainability
- **Single Responsibility**: Each class has one clear purpose
- **Dependency Injection**: Use constructor injection
- **Interface Segregation**: Depend on abstractions
- **Testability**: Design for easy unit testing

## Common Pitfalls to Avoid

### Architecture Violations
- **Business Logic in Controllers**: Keep controllers thin
- **Domain Logic in Infrastructure**: Keep domain layer pure
- **Direct Entity Exposure**: Use DTOs for API responses

### Error Handling Mistakes
- **Swallowing Exceptions**: Always handle or propagate errors
- **Generic Error Messages**: Provide specific error information
- **Missing Validation**: Validate all inputs thoroughly

### Performance Issues
- **N+1 Queries**: Use appropriate fetch strategies
- **Memory Leaks**: Clean up resources properly
- **Blocking Operations**: Use async where appropriate

### Security Vulnerabilities
- **SQL Injection**: Use parameterized queries
- **XSS Attacks**: Sanitize user inputs
- **Information Disclosure**: Don't expose internal details</content>
<parameter name="filePath">/Users/saraki/Documents/project/Wofuf/document/UseCase.md
