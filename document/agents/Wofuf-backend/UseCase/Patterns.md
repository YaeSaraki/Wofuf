# Use Case Implementation Patterns

## Controller Patterns

### Basic Controller Template
```kotlin
@RestController
@RequestMapping({ModuleApiConstant}.{Feature}.{PATH_CONSTANT})
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

### HTTP Method Patterns

#### GET Operations
```kotlin
@GetMapping
fun getEntity(@PathVariable id: String): ApiResponse<GetEntityDto.Response> {
    val result = getEntityUseCase.execute(GetEntityDto.Request(id)).getOrThrow()
    return ApiResponse.success(result)
}

@GetMapping("/search")
fun searchEntities(@RequestParam query: String): ApiResponse<SearchEntitiesDto.Response> {
    val result = searchEntitiesUseCase.execute(SearchEntitiesDto.Request(query)).getOrThrow()
    return ApiResponse.success(result)
}
```

#### POST Operations
```kotlin
@PostMapping
fun createEntity(@RequestBody request: CreateEntityDto.Request): ApiResponse<CreateEntityDto.Response> {
    val result = createEntityUseCase.execute(request).getOrThrow()
    return ApiResponse.success(result)
}
```

#### PUT Operations
```kotlin
@PutMapping("/{id}")
fun updateEntity(
    @PathVariable id: String,
    @RequestBody request: UpdateEntityDto.Request
): ApiResponse<UpdateEntityDto.Response> {
    val result = updateEntityUseCase.execute(
        UpdateEntityDto.Request(id = id, updates = request)
    ).getOrThrow()
    return ApiResponse.success(result)
}
```

#### DELETE Operations
```kotlin
@DeleteMapping("/{id}")
fun deleteEntity(@PathVariable id: String): ApiResponse<DeleteEntityDto.Response> {
    val result = deleteEntityUseCase.execute(DeleteEntityDto.Request(id)).getOrThrow()
    return ApiResponse.success(result)
}
```

## Use Case Implementation Patterns

### Basic Use Case Template
```kotlin
@Service
class {UseCaseName}UseCase(
    private val {repositoryName}: {RepositoryInterface}
) : UseCase<{UseCaseName}Dto.Request, {UseCaseName}Dto.Response> {

    override fun execute(request: {UseCaseName}Dto.Request): Result<{UseCaseName}Dto.Response> {
        // Input validation
        if ({validationCondition}) {
            return {UseCaseName}Errors.{ValidationError}()
        }

        // Business logic
        {businessLogicImplementation}

        // Return success
        return Result.success({UseCaseName}Dto.Response({responseData}))
    }
}
```

### Validation Patterns

#### Input Validation
```kotlin
// String validation
if (request.name.isBlank()) {
    return CreateEntityErrors.NameEmptyError()
}

// ID validation
val entityId = UniqueEntityId.create(request.id)
if (entityId.isFailure) {
    return GetEntityErrors.InvalidIdError()
}

// Domain object validation
val domainObject = DomainObject.create(request.data)
if (domainObject.isFailure) {
    return CreateEntityErrors.InvalidDataError()
}
```

#### Entity Existence Checks
```kotlin
// Single entity lookup
val entity = repository.findById(id) ?: return GetEntityErrors.EntityNotFoundError()

// Multiple entity validation
val entities = repository.findByIds(ids)
if (entities.size != ids.size) {
    return BulkOperationErrors.SomeEntitiesNotFoundError()
}
```

### Business Logic Patterns

#### CRUD Operations
```kotlin
// Create
val newEntity = Entity.create(request.data).getOrThrow()
val savedEntity = repository.save(newEntity)
return Result.success(CreateEntityDto.Response(savedEntity.id))

// Read
val entity = repository.findById(id) ?: return GetEntityErrors.NotFoundError()
return Result.success(GetEntityDto.Response(entity.toDto()))

// Update
val existingEntity = repository.findById(id) ?: return UpdateEntityErrors.NotFoundError()
val updatedEntity = existingEntity.update(request.updates).getOrThrow()
repository.save(updatedEntity)
return Result.success(UpdateEntityDto.Response(updatedEntity.toDto()))

// Delete
val entity = repository.findById(id) ?: return DeleteEntityErrors.NotFoundError()
repository.delete(entity)
return Result.success(DeleteEntityDto.Response(success = true))
```

#### Complex Business Logic
```kotlin
// Multi-step operations
val user = userRepo.findById(request.userId) ?: return OperationErrors.UserNotFoundError()
val target = targetRepo.findById(request.targetId) ?: return OperationErrors.TargetNotFoundError()

// Business rule validation
if (!user.canPerformAction(target)) {
    return OperationErrors.PermissionDeniedError()
}

// Execute business logic
val result = businessService.performAction(user, target, request.data)
if (result.isFailure) {
    return OperationErrors.OperationFailedError()
}

return Result.success(OperationDto.Response(result.getOrThrow()))
```

## DTO Patterns

### Request DTO Patterns

#### Simple Requests
```kotlin
data class Request(
    val id: String
)

data class Request(
    val name: String,
    val description: String? = null
)
```

#### Complex Requests
```kotlin
data class Request(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val settings: PostSettings? = null
)

data class PostSettings(
    val isPublished: Boolean = false,
    val allowComments: Boolean = true,
    val category: String? = null
)
```

#### Search/Filter Requests
```kotlin
data class Request(
    val query: String? = null,
    val category: String? = null,
    val status: String? = null,
    val page: Int = 1,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val sortDirection: String = "desc"
)
```

### Response DTO Patterns

#### Single Entity Response
```kotlin
data class Response(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long
)
```

#### List Response
```kotlin
data class Response(
    val items: List<EntityDto>,
    val total: Int,
    val page: Int,
    val size: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)
```

#### Success Confirmation
```kotlin
data class Response(
    val success: Boolean = true,
    val message: String? = null,
    val id: String? = null
)
```

## Error Handling Patterns

### Error Class Templates

#### Validation Errors
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
```

#### Not Found Errors
```kotlin
class EntityNotFoundError(val entityType: String, val id: String) : Result.Failure<Dto.Response>(
    exception = UseCaseError(
        code = "${entityType.uppercase()}_NOT_FOUND",
        message = "$entityType with id '$id' not found"
    )
)
```

#### Business Logic Errors
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
```

#### System Errors
```kotlin
class OperationFailedError(val operation: String, val reason: String? = null) : Result.Failure<Dto.Response>(
    exception = UseCaseError(
        code = "OPERATION_FAILED",
        message = "Operation '$operation' failed${reason?.let { ": $it" } ?: ""}"
    )
)
```

## Repository Interaction Patterns

### Query Patterns
```kotlin
// Find by ID
val entity = repository.findById(id)

// Find by unique property
val entity = repository.findByUniqueProperty(value)

// Find with conditions
val entities = repository.findByCriteria(criteria)

// Find with pagination
val page = repository.findAll(PageRequest.of(page, size, sort))

// Check existence
val exists = repository.existsById(id)
```

### Save Patterns
```kotlin
// Save new entity
val savedEntity = repository.save(newEntity)

// Save updated entity
val updatedEntity = repository.save(existingEntity)

// Bulk save
val savedEntities = repository.saveAll(entities)
```

### Delete Patterns
```kotlin
// Delete by entity
repository.delete(entity)

// Delete by ID
repository.deleteById(id)

// Bulk delete
repository.deleteAll(entities)
```

## Mapping Patterns

### Domain to DTO Mapping

#### Simple Mapping
```kotlin
fun from(domain: DomainEntity): Dto.Response {
    return Dto.Response(
        id = domain.id.stringValue,
        name = domain.name.value,
        createdAt = domain.createdAt.toEpochMilli()
    )
}
```

#### Complex Mapping
```kotlin
fun from(domain: ComplexEntity): Dto.Response {
    return Dto.Response(
        id = domain.id.stringValue,
        basicInfo = BasicInfoDto(
            name = domain.name.value,
            description = domain.description?.value
        ),
        metadata = MetadataDto(
            tags = domain.tags.map { it.value },
            categories = domain.categories.map { fromCategory(it) },
            stats = domain.stats.toStatsDto()
        ),
        timestamps = TimestampsDto(
            createdAt = domain.createdAt.toEpochMilli(),
            updatedAt = domain.updatedAt.toEpochMilli()
        )
    )
}
```

#### Collection Mapping
```kotlin
fun fromList(domains: List<DomainEntity>): List<Dto.Response> {
    return domains.map { from(it) }
}

fun fromPage(domainPage: Page<DomainEntity>): Dto.PageResponse {
    return Dto.PageResponse(
        items = domainPage.content.map { from(it) },
        total = domainPage.totalElements.toInt(),
        page = domainPage.number,
        size = domainPage.size,
        hasNext = domainPage.hasNext(),
        hasPrevious = domainPage.hasPrevious()
    )
}
```

### DTO to Domain Mapping (When Needed)
```kotlin
fun toDomain(dto: Dto.Request): Result<DomainEntity> {
    return DomainEntity.create(
        name = Name.create(dto.name).getOrThrow(),
        description = dto.description?.let { Description.create(it).getOrThrow() }
    )
}
```

## Configuration Patterns

### API Path Configuration
```kotlin
object ModuleApiConstantV1 {
    private const val BASE = "${ApiConstantV1.API_BASE_PATH}/module"

    object Feature {
        const val BY_ID = "$BASE/{id}"
        const val BY_SLUG = "$BASE/slug/{slug}"
        const val SEARCH = "$BASE/search"
        const val BULK = "$BASE/bulk"
    }

    object Param {
        const val ID = "id"
        const val SLUG = "slug"
    }

    fun buildPath(id: String): String = Feature.BY_ID.replace("{${Param.ID}}", id)
    fun buildSlugPath(slug: String): String = Feature.BY_SLUG.replace("{${Param.SLUG}}", slug)
}
```

### Repository Configuration
```kotlin
@Configuration
class RepositoryConfig {

    @Bean
    fun entityManagerFactory(dataSource: DataSource): LocalContainerEntityManagerFactoryBean {
        return LocalContainerEntityManagerFactoryBean().apply {
            this.dataSource = dataSource
            setPackagesToScan("dev.saraki.wofuf.modules.module.domain")
            jpaVendorAdapter = HibernateJpaVendorAdapter()
        }
    }

    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory): JpaTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}
```

## Testing Patterns

### Unit Test Structure
```kotlin
@SpringBootTest
class UseCaseTest {

    @Autowired
    lateinit var useCase: UseCaseUnderTest

    @MockBean
    lateinit var repository: RepositoryInterface

    @Test
    fun `should return success when valid input`() {
        // Given
        val request = validRequest()
        given(repository.findById(any())).willReturn(entity)

        // When
        val result = useCase.execute(request)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEqualTo(expectedResponse)
    }

    @Test
    fun `should return error when entity not found`() {
        // Given
        val request = requestWithInvalidId()
        given(repository.findById(any())).willReturn(null)

        // When
        val result = useCase.execute(request)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrThrow().code).isEqualTo("ENTITY_NOT_FOUND")
    }
}
```

### Integration Test Structure
```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ControllerIntegrationTest {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun `should return entity when valid id`() {
        // Given
        val entityId = createTestEntity()

        // When
        val response = testRestTemplate.getForEntity("/api/v1/entities/$entityId", Response::class.java)

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
    }
}
```</content>
<parameter name="filePath">/Users/saraki/Documents/project/Wofuf/document/agents/UseCase/Patterns.md
