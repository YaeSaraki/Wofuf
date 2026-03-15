# Data Transfer Object Patterns

This document describes the DTO patterns used for API data transfer in the Wofuf project.

## DTO Structure

### Basic DTO Template
```kotlin
class {UseCaseName}Dto {
    data class Request(
        val {fieldName}: {FieldType},
        val {optionalField}: {Type}? = null
    )

    data class Response(
        val {fieldName}: {FieldType},
        val {optionalField}: {Type}? = null
    )
}
```

## Request DTO Patterns

### Simple Request DTOs

#### Single Parameter Request
```kotlin
data class Request(val id: String)

data class Request(val slug: String)

data class Request(val email: String)
```

#### Create Request DTOs
```kotlin
data class Request(
    val name: String,
    val description: String? = null,
    val category: String = "general"
)

data class Request(
    val title: String,
    val content: String,
    val authorId: String,
    val tags: List<String> = emptyList()
)
```

#### Update Request DTOs
```kotlin
data class Request(
    val name: String? = null,
    val description: String? = null,
    val category: String? = null
)

// Partial update - only provided fields are updated
data class Request(
    val title: String? = null,
    val content: String? = null,
    val tags: List<String>? = null,
    val published: Boolean? = null
)
```

### Complex Request DTOs

#### Search/Filter Request
```kotlin
data class Request(
    val query: String? = null,
    val category: String? = null,
    val status: String? = null,
    val tags: List<String>? = null,
    val authorId: String? = null,
    val dateFrom: Long? = null,
    val dateTo: Long? = null,
    val page: Int = 1,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val sortDirection: String = "desc"
)
```

#### Nested Object Request
```kotlin
data class CreatePostRequest(
    val title: String,
    val content: String,
    val author: AuthorInfo,
    val settings: PostSettings,
    val metadata: Map<String, Any> = emptyMap()
)

data class AuthorInfo(
    val id: String,
    val name: String,
    val avatar: String? = null
)

data class PostSettings(
    val isPublished: Boolean = false,
    val allowComments: Boolean = true,
    val category: String = "general",
    val tags: List<String> = emptyList()
)
```

#### File Upload Request
```kotlin
data class UploadRequest(
    val fileName: String,
    val contentType: String,
    val size: Long,
    val metadata: FileMetadata? = null
)

data class FileMetadata(
    val title: String? = null,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val category: String? = null
)
```

## Response DTO Patterns

### Single Entity Response

#### Basic Entity Response
```kotlin
data class Response(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val version: Int = 1
)
```

#### Rich Entity Response
```kotlin
data class Response(
    val id: String,
    val basicInfo: BasicInfo,
    val statistics: Statistics,
    val metadata: Metadata,
    val timestamps: Timestamps
)

data class BasicInfo(
    val name: String,
    val displayName: String?,
    val description: String?,
    val category: String
)

data class Statistics(
    val viewCount: Long = 0,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val shareCount: Int = 0
)

data class Metadata(
    val tags: List<String> = emptyList(),
    val customFields: Map<String, Any> = emptyMap(),
    val permissions: List<String> = emptyList()
)

data class Timestamps(
    val createdAt: Long,
    val updatedAt: Long,
    val publishedAt: Long? = null
)
```

### List/Collection Responses

#### Paginated Response
```kotlin
data class Response(
    val items: List<EntityDto>,
    val pagination: PaginationInfo,
    val metadata: ResponseMetadata? = null
)

data class PaginationInfo(
    val page: Int,
    val size: Int,
    val totalItems: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
    val first: Boolean,
    val last: Boolean
)

data class ResponseMetadata(
    val query: String? = null,
    val filters: Map<String, Any> = emptyMap(),
    val sortBy: String? = null,
    val sortDirection: String? = null,
    val executionTime: Long? = null
)
```

#### Simple List Response
```kotlin
data class Response(
    val items: List<EntitySummaryDto>,
    val total: Int,
    val hasMore: Boolean = false
)

data class EntitySummaryDto(
    val id: String,
    val name: String,
    val status: String,
    val updatedAt: Long
)
```

### Success/Action Responses

#### Create Response
```kotlin
data class Response(
    val id: String,
    val success: Boolean = true,
    val message: String = "Created successfully",
    val createdAt: Long = System.currentTimeMillis()
)
```

#### Update/Delete Response
```kotlin
data class Response(
    val success: Boolean = true,
    val message: String = "Operation completed successfully",
    val affectedRows: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)
```

#### Bulk Operation Response
```kotlin
data class Response(
    val success: Boolean = true,
    val message: String,
    val processed: Int,
    val successful: Int,
    val failed: Int,
    val errors: List<OperationError>? = null,
    val results: List<OperationResult>? = null
)

data class OperationError(
    val id: String,
    val error: String,
    val code: String? = null
)

data class OperationResult(
    val id: String,
    val status: String,
    val data: Any? = null
)
```

## DTO Validation

### Bean Validation Annotations
```kotlin
data class CreateUserRequest(
    @field:NotBlank(message = "Username cannot be empty")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    val username: String,

    @field:NotBlank(message = "Email cannot be empty")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password cannot be empty")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,

    @field:Size(max = 200, message = "Bio cannot exceed 200 characters")
    val bio: String? = null
)
```

### Custom Validation
```kotlin
data class CreatePostRequest(
    val title: String,
    val content: String,
    val tags: List<@Valid @Size(max = 50) String>? = null
) {
    @AssertTrue(message = "Title and content cannot both be empty")
    fun isValid(): Boolean {
        return title.isNotBlank() || content.isNotBlank()
    }

    @AssertTrue(message = "Tags must be unique")
    fun hasUniqueTags(): Boolean {
        return tags?.let { it.size == it.distinct().size } ?: true
    }
}
```

## DTO Mapping Patterns

### Domain to DTO Mapping

#### Simple Mapping
```kotlin
abstract class GetUserDtoMap {
    companion object {
        fun from(user: User): GetUserDto.Response {
            return GetUserDto.Response(
                id = user.userId.stringValue,
                username = user.username.value,
                email = user.email.value,
                bio = user.bio?.value,
                avatar = user.avatar?.url,
                createdAt = user.createdAt.toEpochMilli(),
                updatedAt = user.updatedAt.toEpochMilli()
            )
        }
    }
}
```

#### Complex Mapping with Nested Objects
```kotlin
abstract class GetPostDtoMap {
    companion object {
        fun from(post: Post): GetPostDto.Response {
            return GetPostDto.Response(
                id = post.postId.stringValue,
                basicInfo = BasicInfoDto(
                    title = post.title.value,
                    content = post.text?.value,
                    type = post.type.name,
                    slug = post.slug.value
                ),
                author = AuthorDto(
                    id = post.memberId.stringValue,
                    name = "Author Name", // Would be fetched separately
                    avatar = null
                ),
                statistics = StatisticsDto(
                    points = post.points,
                    commentCount = post.totalNumComments ?: 0,
                    viewCount = 0 // Would be tracked separately
                ),
                metadata = MetadataDto(
                    tags = emptyList(), // Would be fetched separately
                    createdAt = post.dateTimePosted.toEpochMilli(),
                    updatedAt = post.dateTimePosted.toEpochMilli()
                )
            )
        }
    }
}
```

### DTO to Domain Mapping

#### Create Operation Mapping
```kotlin
abstract class CreateUserDtoMap {
    companion object {
        fun toDomain(dto: CreateUserDto.Request): Result<User> {
            return User.create(
                props = UserProps(
                    username = Username.create(dto.username).getOrThrow(),
                    email = Email.create(dto.email).getOrThrow(),
                    password = Password.create(dto.password).getOrThrow(),
                    bio = dto.bio?.let { Bio.create(it).getOrThrow() }
                ),
                id = null // Let the system generate ID
            )
        }
    }
}
```

#### Update Operation Mapping
```kotlin
abstract class UpdateUserDtoMap {
    companion object {
        fun toDomain(dto: UpdateUserDto.Request, existingUser: User): Result<User> {
            // Apply only provided updates
            val updatedProps = existingUser.props.copy(
                bio = dto.bio?.let { Bio.create(it).getOrThrow() } ?: existingUser.bio
            )

            return User.create(updatedProps, existingUser.id)
        }
    }
}
```

## Collection Mapping

### List Mapping
```kotlin
fun mapToDtoList(domainList: List<DomainEntity>): List<EntityDto> {
    return domainList.map { EntityDto.from(it) }
}

fun mapToDomainList(dtoList: List<EntityDto>): List<Result<DomainEntity>> {
    return dtoList.map { EntityDto.toDomain(it) }
}
```

### Page Mapping
```kotlin
fun mapToDtoPage(domainPage: Page<DomainEntity>): Page<EntityDto> {
    val dtoContent = domainPage.content.map { EntityDto.from(it) }
    return PageImpl(
        dtoContent,
        domainPage.pageable,
        domainPage.totalElements
    )
}
```

## DTO Versioning

### Versioned DTOs
```kotlin
// V1 DTO
data class UserResponseV1(
    val id: String,
    val username: String,
    val email: String,
    val createdAt: Long
)

// V2 DTO with additional fields
data class UserResponseV2(
    val id: String,
    val username: String,
    val email: String,
    val bio: String? = null,
    val avatar: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

// Version selector
enum class ApiVersion {
    V1, V2
}

fun mapUserToDto(user: User, version: ApiVersion = ApiVersion.V2): Any {
    return when (version) {
        ApiVersion.V1 -> UserResponseV1(
            id = user.userId.stringValue,
            username = user.username.value,
            email = user.email.value,
            createdAt = user.createdAt.toEpochMilli()
        )
        ApiVersion.V2 -> UserResponseV2(
            id = user.userId.stringValue,
            username = user.username.value,
            email = user.email.value,
            bio = user.bio?.value,
            avatar = user.avatar?.url,
            createdAt = user.createdAt.toEpochMilli(),
            updatedAt = user.updatedAt.toEpochMilli()
        )
    }
}
```

## Testing DTOs

### Unit Test for DTOs
```kotlin
class CreateUserDtoTest {

    @Test
    fun `should validate required fields`() {
        val validator = Validation.buildDefaultValidatorFactory().validator

        // Valid request
        val validRequest = CreateUserDto.Request(
            username = "testuser",
            email = "test@example.com",
            password = "password123"
        )
        assertTrue(validator.validate(validRequest).isEmpty())

        // Invalid request - empty username
        val invalidRequest = CreateUserDto.Request(
            username = "",
            email = "test@example.com",
            password = "password123"
        )
        assertFalse(validator.validate(invalidRequest).isEmpty())
    }

    @Test
    fun `should map to domain correctly`() {
        // Given
        val dto = CreateUserDto.Request(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            bio = "Test bio"
        )

        // When
        val domainResult = CreateUserDtoMap.toDomain(dto)

        // Then
        assertTrue(domainResult.isSuccess)
        val user = domainResult.getOrThrow()
        assertEquals("testuser", user.username.value)
        assertEquals("test@example.com", user.email.value)
        assertEquals("Test bio", user.bio?.value)
    }
}
```

### Serialization Test
```kotlin
class UserDtoSerializationTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should serialize and deserialize correctly`() {
        // Given
        val originalDto = GetUserDto.Response(
            id = "user-123",
            username = "testuser",
            email = "test@example.com",
            bio = "Test bio",
            createdAt = 1640995200000L,
            updatedAt = 1640995200000L
        )

        // When
        val json = objectMapper.writeValueAsString(originalDto)
        val deserializedDto = objectMapper.readValue(json, GetUserDto.Response::class.java)

        // Then
        assertEquals(originalDto, deserializedDto)
    }
}
```

DTO patterns ensure:
- **Clean API Contracts**: Well-defined request/response structures
- **Data Validation**: Input validation at API boundaries
- **Type Safety**: Strongly typed data transfer
- **Versioning Support**: API evolution without breaking changes
- **Serialization**: Proper JSON/XML conversion
- **Documentation**: Self-documenting API interfaces</content>
<parameter name="filePath">/Users/saraki/Documents/project/Wofuf/document/agents/API/DTOs.md
