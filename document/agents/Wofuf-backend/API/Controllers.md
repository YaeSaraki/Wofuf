# Controller Patterns

This document describes the REST controller patterns used in the Wofuf project.

## Basic Controller Structure

### Standard Controller Template
```kotlin
@RestController
@RequestMapping({ModuleApiConstant}.Base.{PATH_CONSTANT})
class {UseCaseName}Controller(
    private val {useCaseName}UseCase: {UseCaseName}UseCase
) : BaseController() {

    @{HttpMethod}Mapping{PathVariables}
    fun {methodName}({parameters}): ApiResponse<{UseCaseName}Dto.Response> {
        val result = {useCaseName}UseCase.execute(
            {UseCaseName}Dto.Request({requestParams})
        ).getOrThrow()
        return ApiResponse.success({mappingLogic})
    }
}
```

## HTTP Method Patterns

### GET Controllers

#### Get Single Entity
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

#### Get List with Query Parameters
```kotlin
@RestController
@RequestMapping(ForumApiConstantV1.Posts.ROOT)
class GetPostsController(
    private val getPostsUseCase: GetPostsUseCase
) : BaseController() {

    @GetMapping
    fun getPosts(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) sort: String?
    ): ApiResponse<GetPostsDto.Response> {
        val result = getPostsUseCase.execute(
            GetPostsDto.Request(page, size, category, sort)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
```

### POST Controllers

#### Create Entity
```kotlin
@RestController
@RequestMapping(ForumApiConstantV1.Posts.ROOT)
class CreatePostController(
    private val createPostUseCase: CreatePostUseCase
) : BaseController() {

    @PostMapping
    fun createPost(@RequestBody request: CreatePostDto.Request): ApiResponse<CreatePostDto.Response> {
        val result = createPostUseCase.execute(request).getOrThrow()
        return ApiResponse.success(result)
    }
}
```

#### Complex Create with Authentication
```kotlin
@RestController
@RequestMapping(ForumApiConstantV1.Comments.REPLIES)
class ReplyToCommentController(
    private val replyToCommentUseCase: ReplyToCommentUseCase
) : BaseController() {

    @PostMapping
    fun replyToComment(
        @PathVariable parentCommentId: String,
        @RequestBody request: ReplyToCommentRequest
    ): ApiResponse<ReplyToCommentDto.Response> {
        val result = replyToCommentUseCase.execute(
            ReplyToCommentDto.Request(
                postSlug = request.postSlug,
                userId = request.userId,
                comment = request.comment,
                parentCommentId = parentCommentId,
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
```

### PUT Controllers

#### Update Entity
```kotlin
@RestController
@RequestMapping(ForumApiConstantV1.Posts.BY_ID)
class UpdatePostController(
    private val updatePostUseCase: UpdatePostUseCase
) : BaseController() {

    @PutMapping
    fun updatePost(
        @PathVariable postId: String,
        @RequestBody request: UpdatePostDto.Request
    ): ApiResponse<UpdatePostDto.Response> {
        val result = updatePostUseCase.execute(
            UpdatePostDto.Request(
                id = postId,
                title = request.title,
                content = request.content,
                tags = request.tags
            )
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
```

### DELETE Controllers

#### Delete Entity
```kotlin
@RestController
@RequestMapping(ForumApiConstantV1.Posts.BY_ID)
class DeletePostController(
    private val deletePostUseCase: DeletePostUseCase
) : BaseController() {

    @DeleteMapping
    fun deletePost(@PathVariable postId: String): ApiResponse<DeletePostDto.Response> {
        val result = deletePostUseCase.execute(
            DeletePostDto.Request(postId)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
```

## Parameter Handling

### Path Variables
```kotlin
@GetMapping("/{id}")
fun getById(@PathVariable id: String): ApiResponse<EntityDto.Response> {
    // Handle single path variable
}

@GetMapping("/{parentId}/children/{childId}")
fun getNested(
    @PathVariable parentId: String,
    @PathVariable childId: String
): ApiResponse<EntityDto.Response> {
    // Handle multiple path variables
}
```

### Query Parameters
```kotlin
@GetMapping
fun search(
    @RequestParam(required = false) query: String?,
    @RequestParam(defaultValue = "1") page: Int,
    @RequestParam(defaultValue = "20") size: Int,
    @RequestParam(required = false) sort: String?
): ApiResponse<SearchDto.Response> {
    // Handle query parameters with defaults
}
```

### Request Body
```kotlin
@PostMapping
fun create(@RequestBody @Valid request: CreateDto.Request): ApiResponse<CreateDto.Response> {
    // Handle JSON request body with validation
}

@PostMapping("/upload")
fun upload(
    @RequestParam("file") file: MultipartFile,
    @RequestParam("metadata") metadata: String
): ApiResponse<UploadDto.Response> {
    // Handle file upload with metadata
}
```

### Headers
```kotlin
@GetMapping
fun getWithHeaders(
    @RequestHeader("X-API-Version") apiVersion: String,
    @RequestHeader("Authorization") auth: String
): ApiResponse<EntityDto.Response> {
    // Handle custom headers
}
```

## Response Handling

### Success Responses
```kotlin
// Simple success
return ApiResponse.success(result)

// With custom message
return ApiResponse.success(result, "Operation completed successfully")

// With metadata
return ApiResponse.success(result, metadata = mapOf("version" to "1.0"))
```

### Error Responses (via BaseController)
```kotlin
// Override methods from BaseController
fun notFound(message: String) = ResponseEntity.notFound().build()

fun badRequest(message: String) = ResponseEntity.badRequest().build()

fun internalServerError(message: String) = ResponseEntity.status(500).build()

fun forbidden(message: String) = ResponseEntity.status(403).build()
```

## Authentication and Authorization

### JWT Authentication
```kotlin
@RestController
@RequestMapping("/api/v1/profile")
class ProfileController(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : BaseController() {

    @GetMapping
    fun getProfile(@AuthenticationPrincipal user: User): ApiResponse<ProfileDto.Response> {
        val result = getProfileUseCase.execute(
            GetProfileDto.Request(user.id)
        ).getOrThrow()
        return ApiResponse.success(result)
    }

    @PutMapping
    @PreAuthorize("hasRole('USER')")
    fun updateProfile(
        @AuthenticationPrincipal user: User,
        @RequestBody request: UpdateProfileDto.Request
    ): ApiResponse<UpdateProfileDto.Response> {
        // Ensure user can only update their own profile
        if (user.id != request.userId) {
            return forbidden("Cannot update other user's profile")
        }

        val result = updateProfileUseCase.execute(request).getOrThrow()
        return ApiResponse.success(result)
    }
}
```

## Validation

### Request Validation
```kotlin
@PostMapping
fun create(@RequestBody @Valid request: CreateEntityDto.Request): ApiResponse<CreateEntityDto.Response> {
    // Spring Boot automatically validates @Valid annotated requests
    val result = createEntityUseCase.execute(request).getOrThrow()
    return ApiResponse.success(result)
}

// Custom validation in controller
@PostMapping
fun create(@RequestBody request: CreateEntityDto.Request): ApiResponse<CreateEntityDto.Response> {
    if (request.name.isBlank()) {
        return badRequest("Name cannot be empty")
    }

    if (request.email != null && !isValidEmail(request.email)) {
        return badRequest("Invalid email format")
    }

    val result = createEntityUseCase.execute(request).getOrThrow()
    return ApiResponse.success(result)
}
```

## Error Handling

### Global Exception Handling
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler : BaseController() {

    @ExceptionHandler(UseCaseError::class)
    fun handleUseCaseError(e: UseCaseError): ResponseEntity<ApiResponse<Nothing>> {
        val status = when (e.code) {
            "NOT_FOUND" -> HttpStatus.NOT_FOUND
            "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST
            "PERMISSION_DENIED" -> HttpStatus.FORBIDDEN
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }

        return ResponseEntity(
            ApiResponse.error(e.message, e.code),
            status
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unexpected error", e)
        return ResponseEntity(
            ApiResponse.error("Internal server error"),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}
```

## Content Negotiation

### Multiple Response Formats
```kotlin
@GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE])
fun getEntity(
    @PathVariable id: String,
    @RequestHeader(HttpHeaders.ACCEPT) accept: String
): ResponseEntity<Any> {
    val entity = getEntityUseCase.execute(GetEntityDto.Request(id)).getOrThrow()

    return when {
        accept.contains(MediaType.APPLICATION_XML_VALUE) -> {
            val xmlEntity = convertToXml(entity)
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(xmlEntity)
        }
        else -> {
            ResponseEntity.ok(entity)
        }
    }
}
```

## Caching

### Response Caching
```kotlin
@GetMapping("/public")
@Cacheable("publicData")
fun getPublicData(): ApiResponse<PublicDataDto.Response> {
    // Cached for public data
}

@GetMapping("/user/{userId}")
@Cacheable(value = ["userData"], key = "#userId")
fun getUserData(@PathVariable userId: String): ApiResponse<UserDataDto.Response> {
    // Cached per user
}

@PostMapping
@CacheEvict(value = ["userData"], key = "#request.userId")
fun updateUserData(@RequestBody request: UpdateUserDataDto.Request): ApiResponse<UpdateUserDataDto.Response> {
    // Evict cache after update
}
```

## Testing Controllers

### Unit Test Example
```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class GetPlayerControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var getPlayerUseCase: GetPlayerUseCase

    @Test
    fun `should return player when found`() {
        // Given
        val playerName = "testPlayer"
        val expectedResponse = GetPlayerDto.Response(
            id = "player-123",
            name = "testPlayer",
            firstLogin = 1000000L,
            lastLogin = 2000000L,
            totalPlaytimeSeconds = 3600L,
            updateTime = 2000000L
        )

        given(getPlayerUseCase.execute(any())).willReturn(Result.success(expectedResponse))

        // When & Then
        mockMvc.perform(
            get("/api/v1/players/playerNameOrUuid/$playerName")
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.data.id").value("player-123"))
        .andExpect(jsonPath("$.data.name").value("testPlayer"))
    }

    @Test
    fun `should return 404 when player not found`() {
        // Given
        val playerName = "nonexistent"
        given(getPlayerUseCase.execute(any())).willReturn(
            Result.failure(GetPlayerErrors.GetPlayerError())
        )

        // When & Then
        mockMvc.perform(
            get("/api/v1/players/playerNameOrUuid/$playerName")
        )
        .andExpect(status().isNotFound)
    }
}
```

### Integration Test Example
```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreatePostIntegrationTest {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun `should create post successfully`() {
        // Given
        val request = CreatePostDto.Request(
            title = "Test Post",
            content = "Test content",
            authorId = "user-123"
        )

        // When
        val response = testRestTemplate.postForEntity(
            "/api/v1/forum/posts",
            request,
            CreatePostDto.Response::class.java
        )

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.success).isTrue()
        assertThat(response.body?.postId).isNotNull()
    }
}
```

Controller patterns ensure:
- **Consistent API Design**: Standardized REST endpoints
- **Proper HTTP Semantics**: Correct use of HTTP methods and status codes
- **Error Handling**: Comprehensive error responses
- **Security**: Proper authentication and authorization
- **Performance**: Appropriate caching and optimization
- **Testability**: Easy to unit and integration test</content>
<parameter name="filePath">/Users/saraki/Documents/project/Wofuf/document/agents/API/Controllers.md
