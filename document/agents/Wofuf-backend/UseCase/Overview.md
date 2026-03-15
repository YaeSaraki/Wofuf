# Use Case Generation Overview

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

## Complete Use Case Generation Workflow

### Step 1: Requirement Analysis
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

### Step 2: File Structure Creation
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

### Step 3: API Configuration
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

### Step 4: Implementation Order
1. **Create DTOs**: Define Request/Response structures
2. **Create Errors**: Define all possible error conditions
3. **Create Use Case**: Implement business logic
4. **Create Controller**: Define REST endpoint
5. **Create Mapper**: Implement domain ↔ DTO conversion (if needed)

### Step 5: Testing and Validation
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

## Agent Decision Framework

### When to Generate Code
- **Clear Requirements**: Business logic is well-defined
- **Existing Patterns**: Similar functionality already exists
- **Standard Operations**: CRUD operations following established patterns
- **Well-Understood Domain**: Domain model is clear and stable

### When to Seek Human Input
- **Ambiguous Requirements**: Business rules are unclear
- **New Domain Concepts**: Introducing new entities or relationships
- **Complex Business Logic**: Multi-step processes with conditional logic
- **Security-Critical**: Authentication, authorization, or data protection
- **Performance-Critical**: High-throughput or low-latency requirements
- **Integration Points**: External system interactions

### Escalation Triggers
- **Pattern Conflicts**: New requirements contradict existing patterns
- **Architectural Changes**: Modifications to fundamental structure
- **Breaking Changes**: Updates that affect multiple modules
- **Security Implications**: Changes affecting data protection or access control

## Quality Assurance Checklist

### Code Quality
- [ ] Compiles without errors
- [ ] Follows naming conventions
- [ ] Includes proper error handling
- [ ] Uses dependency injection correctly
- [ ] Has appropriate imports

### Architecture Compliance
- [ ] Respects DDD layer boundaries
- [ ] Uses DTOs for API responses
- [ ] Implements proper validation
- [ ] Follows established patterns
- [ ] Maintains domain purity

### Documentation
- [ ] Includes meaningful comments
- [ ] Documents business logic assumptions
- [ ] References related domain concepts
- [ ] Updates API documentation if needed</content>
<parameter name="filePath">/Users/saraki/Documents/project/Wofuf/document/agents/UseCase/Overview.md
