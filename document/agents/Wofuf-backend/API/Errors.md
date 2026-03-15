# Error Handling Patterns

This document describes the error handling patterns used in the Wofuf project's API layer.

## Error Response Structure

### Standard API Error Response
```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorInfo? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ErrorInfo(
    val code: String,
    val message: String,
    val details: Map<String, Any>? = null
)
```

## Use Case Error Patterns

### Base Error Classes
```kotlin
// Base error class
open class UseCaseError(
    val code: String,
    override val message: String
) : Exception(message)

// Result.Failure wrapper
class ResultFailure<T>(
    val error: UseCaseError
) : Result.Failure<T>(exception = error)
```

### Validation Errors
```kotlin
class FieldRequiredError(val fieldName: String) : Result.Failure<Dto.Response>(
    exception = UseCaseError(
        code = "${fieldName.uppercase()}_REQUIRED",
        message = "$fieldName is required"
    )
)

class FieldInvalidError(val fieldName: String, val value: Any?) : Result.Failure<Dto.Response>(
    exception = UseCaseError(
        code = "${fieldName.uppercase()}_INVALID",
        message = "$fieldName value '$value' is invalid"
    )
)

class FieldTooLongError(val fieldName: String, val maxLength: Int) : Result.Failure<Dto.Response>(
    exception = UseCaseError(
        code = "${fieldName.uppercase()}_TOO_LONG",
        message = "$fieldName cannot exceed $maxLength characters"
    )
)
```

### Not Found Errors
```kotlin
class EntityNotFoundError(val entityType: String, val id: String) : Result.Failure<Dto.Response>(
    exception = UseCaseError(
        code = "${entityType.uppercase()}_NOT_FOUND",
        message = "$entityType with id '$id' not found"
    )
)

class ResourceNotFoundError(val resource: String, val identifier: String) : Result.Failure<Dto.Response>(
    exception = UseCaseError(
        code = "RESOURCE_NOT_FOUND",
        message = "$resource '$identifier' not found"
    )
)
```

### Business Logic Errors
```kotlin
class PermissionDeniedError(val action: String, val resource: String) : Result.Failure<Dto.Response>(
    exception = UseCaseError(
        code = "PERMISSION_DENIED",
        message = "Permission denied for action '$action' on resource '$resource'"
    )
)

class BusinessRuleViolationError(val rule: String) : Result.Failure<Dto.Response>(
    exception = UseCaseError(
        code = "BUSINESS_RULE_VIOLATION",
        message = "Business rule violation: $rule"
    )
)

class InsufficientFundsError(val required: BigDecimal, val available: BigDecimal) : Result.Failure<Dto.Response>(
    exception = UseCaseError(
        code = "INSUFFICIENT_FUNDS",
        message = "Insufficient funds. Required: $required, Available: $available"
    )
)
```

### System Errors
```kotlin
class OperationFailedError(val operation: String, val reason: String? = null) : Result.Failure<Dto.Response>(
    exception = UseCaseError(
        code = "OPERATION_FAILED",
        message = "Operation '$operation' failed${reason?.let { ": $it" } ?: ""}"
    )
)

class ExternalServiceError(val service: String, val reason: String? = null) : Result.Failure<Dto.Response>(
    exception = UseCaseError(
        code = "EXTERNAL_SERVICE_ERROR",
        message = "External service '$service' error${reason?.let { ": $it" } ?: ""}"
    )
)

class DatabaseError(val operation: String) : Result.Failure<Dto.Response>(
    exception = UseCaseError(
        code = "DATABASE_ERROR",
        message = "Database operation '$operation' failed"
    )
)
```

## Controller Error Handling

### Global Exception Handler
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler : BaseController() {

    @ExceptionHandler(UseCaseError::class)
    fun handleUseCaseError(e: UseCaseError): ResponseEntity<ApiResponse<Nothing>> {
        val status = getHttpStatusForError(e.code)
        val errorInfo = ErrorInfo(
            code = e.code,
            message = e.message,
            details = getErrorDetails(e)
        )

        return ResponseEntity(
            ApiResponse.error(errorInfo),
            status
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unexpected error", e)

        val errorInfo = ErrorInfo(
            code = "INTERNAL_SERVER_ERROR",
            message = "An unexpected error occurred",
            details = if (isDevelopment()) mapOf("stackTrace" to e.stackTraceToString()) else null
        )

        return ResponseEntity(
            ApiResponse.error(errorInfo),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }

    private fun getHttpStatusForError(errorCode: String): HttpStatus {
        return when {
            errorCode.endsWith("_NOT_FOUND") -> HttpStatus.NOT_FOUND
            errorCode.endsWith("_REQUIRED") || errorCode.endsWith("_INVALID") -> HttpStatus.BAD_REQUEST
            errorCode == "PERMISSION_DENIED" -> HttpStatus.FORBIDDEN
            errorCode == "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED
            errorCode.endsWith("_EXISTS") || errorCode == "BUSINESS_RULE_VIOLATION" -> HttpStatus.CONFLICT
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }
    }

    private fun getErrorDetails(error: UseCaseError): Map<String, Any>? {
        return when (error) {
            is FieldInvalidError -> mapOf("field" to error.fieldName, "value" to error.value)
            is EntityNotFoundError -> mapOf("entityType" to error.entityType, "id" to error.id)
            is InsufficientFundsError -> mapOf("required" to error.required, "available" to error.available)
            else -> null
        }
    }

    private fun isDevelopment(): Boolean {
        return System.getProperty("spring.profiles.active", "").contains("dev")
    }
}
```

### Controller-Specific Error Handling
```kotlin
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val createUserUseCase: CreateUserUseCase
) : BaseController() {

    @PostMapping
    fun createUser(@RequestBody @Valid request: CreateUserDto.Request): ApiResponse<CreateUserDto.Response> {
        return try {
            val result = createUserUseCase.execute(request).getOrThrow()
            ApiResponse.success(result)
        } catch (e: UseCaseError) {
            // Let global handler deal with UseCaseError
            throw e
        } catch (e: Exception) {
            logger.error("Unexpected error in createUser", e)
            throw InternalServerError("Failed to create user")
        }
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): ApiResponse<GetUserDto.Response> {
        if (id.isBlank()) {
            return badRequest("User ID cannot be empty")
        }

        return try {
            val result = getUserUseCase.execute(GetUserDto.Request(id)).getOrThrow()
            ApiResponse.success(result)
        } catch (e: EntityNotFoundError) {
            notFound("User not found")
        } catch (e: Exception) {
            logger.error("Error getting user $id", e)
            internalServerError("Failed to get user")
        }
    }
}
```

## Base Controller Utilities

### Base Controller Class
```kotlin
abstract class BaseController {

    protected val logger = LoggerFactory.getLogger(javaClass)

    protected fun <T> success(data: T, message: String? = null): ApiResponse<T> {
        return ApiResponse.success(data, message)
    }

    protected fun badRequest(message: String): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.badRequest().body(
            ApiResponse.error(ErrorInfo("BAD_REQUEST", message))
        )
    }

    protected fun notFound(message: String = "Resource not found"): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiResponse.error(ErrorInfo("NOT_FOUND", message))
        )
    }

    protected fun forbidden(message: String = "Access forbidden"): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ApiResponse.error(ErrorInfo("FORBIDDEN", message))
        )
    }

    protected fun internalServerError(message: String = "Internal server error"): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse.error(ErrorInfo("INTERNAL_SERVER_ERROR", message))
        )
    }

    protected fun conflict(message: String): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ApiResponse.error(ErrorInfo("CONFLICT", message))
        )
    }
}
```

## Validation Error Handling

### Bean Validation Integration
```kotlin
@RestControllerAdvice
class ValidationExceptionHandler : BaseController() {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errors = e.bindingResult.fieldErrors.associate { error ->
            error.field to error.defaultMessage
        }

        val errorInfo = ErrorInfo(
            code = "VALIDATION_ERROR",
            message = "Validation failed",
            details = mapOf("fieldErrors" to errors)
        )

        return ResponseEntity.badRequest().body(ApiResponse.error(errorInfo))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolations(e: ConstraintViolationException): ResponseEntity<ApiResponse<Nothing>> {
        val errors = e.constraintViolations.associate { violation ->
            violation.propertyPath.toString() to violation.message
        }

        val errorInfo = ErrorInfo(
            code = "VALIDATION_ERROR",
            message = "Constraint violations",
            details = mapOf("constraintErrors" to errors)
        )

        return ResponseEntity.badRequest().body(ApiResponse.error(errorInfo))
    }
}
```

## Custom Exceptions

### Domain-Specific Exceptions
```kotlin
class DuplicateEntityException(
    entityType: String,
    identifier: String
) : UseCaseError(
    code = "DUPLICATE_${entityType.uppercase()}",
    message = "$entityType with identifier '$identifier' already exists"
)

class InvalidStateException(
    entityType: String,
    currentState: String,
    requiredState: String
) : UseCaseError(
    code = "INVALID_STATE",
    message = "$entityType is in state '$currentState' but requires state '$requiredState'"
)

class QuotaExceededException(
    resource: String,
    currentUsage: Int,
    limit: Int
) : UseCaseError(
    code = "QUOTA_EXCEEDED",
    message = "$resource quota exceeded. Current: $currentUsage, Limit: $limit"
)
```

## Error Logging and Monitoring

### Structured Error Logging
```kotlin
@Component
class ErrorLogger {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun logError(error: UseCaseError, context: Map<String, Any> = emptyMap()) {
        val logData = mapOf(
            "errorCode" to error.code,
            "errorMessage" to error.message,
            "timestamp" to System.currentTimeMillis(),
            "context" to context
        )

        when {
            error.code.endsWith("_NOT_FOUND") -> logger.warn("Resource not found: {}", logData)
            error.code == "VALIDATION_ERROR" -> logger.info("Validation error: {}", logData)
            error.code == "PERMISSION_DENIED" -> logger.warn("Permission denied: {}", logData)
            else -> logger.error("Application error: {}", logData, error)
        }
    }
}
```

### Error Metrics
```kotlin
@Component
class ErrorMetrics {

    @Autowired
    lateinit var meterRegistry: MeterRegistry

    fun recordError(error: UseCaseError) {
        meterRegistry.counter(
            "application.errors",
            "code", error.code,
            "type", getErrorType(error.code)
        ).increment()
    }

    private fun getErrorType(errorCode: String): String {
        return when {
            errorCode.endsWith("_NOT_FOUND") -> "not_found"
            errorCode.endsWith("_INVALID") || errorCode.endsWith("_REQUIRED") -> "validation"
            errorCode == "PERMISSION_DENIED" -> "authorization"
            errorCode == "BUSINESS_RULE_VIOLATION" -> "business_logic"
            else -> "system"
        }
    }
}
```

## Testing Error Scenarios

### Unit Test for Error Handling
```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class ErrorHandlingTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `should return validation error for invalid input`() {
        // Given
        val invalidRequest = mapOf("name" to "")

        // When & Then
        mockMvc.perform(
            post("/api/v1/entities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ObjectMapper().writeValueAsString(invalidRequest))
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `should return not found error for non-existent resource`() {
        // Given
        val nonExistentId = "non-existent-id"

        // When & Then
        mockMvc.perform(
            get("/api/v1/entities/$nonExistentId")
        )
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.error.code").value("ENTITY_NOT_FOUND"))
    }
}
```

### Integration Test for Error Scenarios
```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErrorIntegrationTest {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun `should handle database connection errors gracefully`() {
        // Given - simulate database down scenario

        // When
        val response = testRestTemplate.getForEntity("/api/v1/health", String::class.java)

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
        // Verify error response format
    }
}
```

Error handling patterns ensure:
- **Consistent Error Responses**: Standardized error format across all APIs
- **Proper HTTP Status Codes**: Appropriate status codes for different error types
- **Detailed Error Information**: Helpful error messages for debugging
- **Security**: No sensitive information leaked in error responses
- **Monitoring**: Error tracking and alerting capabilities
- **User Experience**: Clear error messages for API consumers</content>
<parameter name="filePath">/Users/saraki/Documents/project/Wofuf/document/agents/API/Errors.md
