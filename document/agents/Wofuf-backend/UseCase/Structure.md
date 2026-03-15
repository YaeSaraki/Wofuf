# Use Case Structure Guidelines

## File Organization Standards

### Directory Structure
Each use case must follow this exact directory structure:
```
src/main/kotlin/dev/saraki/wofuf/modules/{module}/useCases/{useCaseName}/
├── {UseCaseName}Controller.kt    # REST API endpoint handler
├── {UseCaseName}UseCase.kt       # Business logic implementation
├── {UseCaseName}Dto.kt           # Request/Response data structures
├── {UseCaseName}Errors.kt        # Custom error definitions
└── {UseCaseName}DtoMap.kt        # Domain ↔ DTO mapping (optional)
```

### Package Structure
```
dev.saraki.wofuf.modules.{module}.useCases.{useCaseName}
```

## Naming Conventions

### Use Case Names
- **Format**: `{Action}{Subject}` (e.g., `GetPlayer`, `CreateComment`, `ReplyToComment`)
- **Case**: PascalCase
- **Actions**: Get, Create, Update, Delete, List, Search, etc.
- **Subjects**: Domain entities or concepts

### File Naming
- **Pattern**: `{UseCaseName}{Suffix}.kt`
- **Suffixes**:
  - `Controller` - REST controller class
  - `UseCase` - Business logic class
  - `Dto` - Data transfer objects
  - `Errors` - Error definitions
  - `DtoMap` - Mapping utilities

### Class Naming
- **Controllers**: `{UseCaseName}Controller`
- **Use Cases**: `{UseCaseName}UseCase`
- **DTOs**: `{UseCaseName}Dto` (containing Request/Response classes)
- **Errors**: `{UseCaseName}Errors` (containing error classes)
- **Mappers**: `{UseCaseName}DtoMap`

### Method Naming
- **Controller Methods**: camelCase, descriptive actions (e.g., `getPlayer`, `createComment`)
- **Use Case Methods**: `execute()` (standard interface method)
- **Repository Methods**: `findBy{Property}()`, `save()`, `delete()`, etc.
- **Mapper Methods**: `from()` for domain-to-DTO, `toDomain()` for DTO-to-domain

## File Content Standards

### Controller File Structure
```kotlin
package dev.saraki.wofuf.modules.{module}.useCases.{useCaseName}

import {necessary imports}

@RestController
@RequestMapping({ModuleApiConstant}.{Feature}.{PATH_CONSTANT})
class {UseCaseName}Controller(
    private val {useCaseName}UseCase: {UseCaseName}UseCase
) : BaseController() {

    @{HttpMethod}Mapping{PathVariables}
    fun {methodName}({parameters}): ApiResponse<{UseCaseName}Dto.Response> {
        // Implementation
    }
}
```

### Use Case File Structure
```kotlin
package dev.saraki.wofuf.modules.{module}.useCases.{useCaseName}

import {necessary imports}

@Service
class {UseCaseName}UseCase(
    private val {dependency1}: {Interface1},
    private val {dependency2}: {Interface2}
) : UseCase<{UseCaseName}Dto.Request, {UseCaseName}Dto.Response> {

    override fun execute(request: {UseCaseName}Dto.Request): Result<{UseCaseName}Dto.Response> {
        // Implementation
    }
}
```

### DTO File Structure
```kotlin
package dev.saraki.wofuf.modules.{module}.useCases.{useCaseName}

class {UseCaseName}Dto {
    data class Request(
        val {fieldName}: {FieldType},
        // ... additional fields
    )

    data class Response(
        val {fieldName}: {FieldType},
        // ... additional fields
    )
}
```

### Errors File Structure
```kotlin
package dev.saraki.wofuf.modules.{module}.useCases.{useCaseName}

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class {UseCaseName}Errors {
    class {SpecificError}Error({params}) : Result.Failure<{UseCaseName}Dto.Response>(
        exception = UseCaseError(
            code = "{ERROR_CODE}",
            message = "{error_message}"
        )
    )
}
```

### Mapper File Structure (Optional)
```kotlin
package dev.saraki.wofuf.modules.{module}.useCases.{useCaseName}

import {domain imports}

abstract class {UseCaseName}DtoMap {
    companion object {
        fun from({domainObject}: {DomainType}): {UseCaseName}Dto.Response {
            return {UseCaseName}Dto.Response(
                // mapping logic
            )
        }
    }
}
```

## Import Organization

### Standard Import Order
1. **Kotlin/Java standard library**
2. **Spring Framework imports**
3. **Project shared imports** (shared.*)
4. **Module domain imports** (domain.*)
5. **Module infrastructure imports** (infra.*)
6. **Current package imports** (relative)

### Import Examples
```kotlin
// Kotlin standard
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*

// Shared imports
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase

// Domain imports
import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug

// Infrastructure imports
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo

// Local imports
import GetPostBySlugDto
import GetPostBySlugErrors
```

## Code Style Standards

### Kotlin Specific
- **Indentation**: 4 spaces
- **Line Length**: Maximum 120 characters
- **Braces**: Opening brace on same line
- **Semicolons**: Not used (except when required)
- **Null Safety**: Use nullable types and safe calls appropriately

### Annotations
- **Spring Annotations**: Place on separate lines above class/method declarations
- **Custom Annotations**: Follow Spring annotation patterns
- **Suppress Annotations**: Use sparingly and document why

### Comments
- **Class Comments**: Describe purpose and responsibilities
- **Method Comments**: Explain parameters, return values, and side effects
- **Inline Comments**: Explain complex business logic
- **TODO Comments**: Mark areas needing future work

## Module-Specific Patterns

### Players Module
- **Common Operations**: Get by name/UUID, get random players
- **Data Complexity**: Complex nested objects (statistics, advancements)
- **Caching**: Heavy Redis usage for performance

### Forum Module
- **Entity Relationships**: Hierarchical comments and replies
- **User Integration**: Member/User association required
- **Content Validation**: Text sanitization and length limits

### Users Module
- **Authentication**: JWT token management
- **Security**: Password hashing and role validation
- **Session Management**: Login/logout with refresh tokens

## Quality Assurance

### File Checklist
- [ ] Correct package declaration
- [ ] Proper imports (organized and necessary)
- [ ] Consistent naming conventions
- [ ] Appropriate annotations
- [ ] Complete error handling
- [ ] Meaningful comments

### Integration Checklist
- [ ] Dependencies properly injected
- [ ] API paths correctly configured
- [ ] DTOs properly mapped
- [ ] Error responses consistent
- [ ] Business logic validated</content>
<parameter name="filePath">/Users/saraki/Documents/project/Wofuf/document/agents/UseCase/Structure.md
